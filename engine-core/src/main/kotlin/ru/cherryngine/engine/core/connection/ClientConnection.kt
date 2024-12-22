package ru.cherryngine.engine.core.connection

import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.minestom.server.network.ConnectionState
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.packet.PacketParser
import net.minestom.server.network.packet.PacketReading
import net.minestom.server.network.packet.PacketVanilla
import net.minestom.server.network.packet.PacketWriting
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket
import net.minestom.server.network.packet.server.*
import net.minestom.server.network.packet.server.common.PingResponsePacket
import net.minestom.server.network.packet.server.login.SetCompressionPacket
import net.minestom.server.network.packet.server.play.SystemChatPacket
import net.minestom.server.registry.Registries
import org.jctools.queues.MpscUnboundedXaddArrayQueue
import ru.cherryngine.engine.core.TempConsts
import ru.cherryngine.engine.core.commands.CommandSender
import java.io.EOFException
import java.io.IOException
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.zip.DataFormatException
import javax.crypto.Cipher

class ClientConnection(
    val channel: SocketChannel,
    registries: Registries,
    val compressionThreshold: Int,
) : CommandSender {
    data class EncryptionContext(
        val encrypt: Cipher,
        val decrypt: Cipher,
    )

    @Volatile
    var connectionState: ConnectionState = ConnectionState.HANDSHAKE
        private set

    @Volatile
    private var encryptionContext: EncryptionContext? = null

    @Volatile
    var online: Boolean = true
        private set

    private val readBuffer = NetworkBuffer.resizableBuffer(TempConsts.POOLED_BUFFER_SIZE, registries)
    private val packetQueue = MpscUnboundedXaddArrayQueue<SendablePacket>(1024)

    @Volatile
    private var compressionStarted = false

    fun disconnect() {
        online = false
    }

    fun read(packetParser: PacketParser<ClientPacket>) {
        val writeIndex = readBuffer.writeIndex()
        val length = readBuffer.readChannel(channel)

        encryptionContext?.let {
            readBuffer.cipher(it.decrypt, writeIndex, length.toLong())
        }

        processPackets(readBuffer, packetParser)
    }

    private fun processPackets(readBuffer: NetworkBuffer, packetParser: PacketParser<ClientPacket>) {
        val startingState = connectionState
        val result: PacketReading.Result<ClientPacket> = try {
            PacketReading.readPackets(
                readBuffer,
                packetParser,
                startingState,
                PacketVanilla::nextClientState,
                compressionStarted
            )
        } catch (e: DataFormatException) {
            e.printStackTrace()
            disconnect()
            return
        }

        when (result) {
            is PacketReading.Result.Success -> {
                for (parsedPacket in result.packets) {
                    val packet = parsedPacket.packet
                    val currState = connectionState
                    val nextState = parsedPacket.nextState

                    if (nextState != currState) {
                        connectionState = nextState
                    }

                    when (packet) {
                        is ClientPingRequestPacket -> {
                            sendPacket(PingResponsePacket(packet.number))
                        }

                        is ClientLoginStartPacket -> {
                            if (compressionThreshold > 0) startCompression()
                            incomingPlayPackets.add(packet)
                        }

                        else -> {
                            incomingPlayPackets.add(packet)
                        }
                    }
                }
                readBuffer.compact()
            }

            is PacketReading.Result.Empty -> {
                // Empty
            }

            is PacketReading.Result.Failure -> {
                val requiredCapacity = result.requiredCapacity
                require(requiredCapacity > readBuffer.capacity()) {
                    "New capacity should be greater than the current one: $requiredCapacity <= ${readBuffer.capacity()}"
                }
                readBuffer.resize(requiredCapacity)
            }
        }
    }

    /**
     * Enables compression and adds a new codec to the pipeline.
     *
     * @throws IllegalStateException if encryption is already enabled for this connection
     */
    fun startCompression() {
        require(!compressionStarted) { "Compression is already enabled!" }
        require(compressionThreshold != 0) { "Compression cannot be enabled because the threshold is equal to 0" }
        sendPacket(SetCompressionPacket(compressionThreshold))
    }

    fun sendPacket(packet: SendablePacket) {
        packetQueue.relaxedOffer(packet)
    }

    private fun writeSendable(buffer: NetworkBuffer, sendable: SendablePacket): Boolean {
        val start = buffer.writeIndex()
        val result = writePacketSync(buffer, sendable)
        if (!result) return false
        val length = buffer.writeIndex() - start
        encryptionContext?.let {
            if (length > 0) {
                buffer.cipher(it.encrypt, start, length)
            }
        }
        return true
    }

    private fun writePacketSync(buffer: NetworkBuffer, packet: SendablePacket): Boolean {
        val state = connectionState
        // Write packet
        val start = buffer.writeIndex()
        val compressionThreshold = if (compressionStarted) compressionThreshold else 0
        try {
            when (packet) {
                is ServerPacket -> {
                    PacketWriting.writeFramedPacket(buffer, state, packet, compressionThreshold)
                    return true
                }

                is FramedPacket -> {
                    val body = packet.body()
                    return writeBuffer(buffer, body, 0, body.capacity().toLong())
                }

                is CachedPacket -> {
                    val body = packet.body(state)
                    if (body == null) {
                        PacketWriting.writeFramedPacket(buffer, state, packet.packet(state), compressionThreshold)
                        return true
                    }
                    return writeBuffer(buffer, body, 0, body.capacity().toLong())
                }

                is LazyPacket -> {
                    PacketWriting.writeFramedPacket(buffer, state, packet.packet(), compressionThreshold)
                    return true
                }

                is BufferedPacket -> {
                    val rawBuffer = packet.buffer()
                    val index = packet.index()
                    val length = packet.length()
                    return writeBuffer(buffer, rawBuffer, index, length)
                }
            }
        } catch (_: IndexOutOfBoundsException) {
            buffer.writeIndex(start)
            return false
        }
    }

    private fun writeBuffer(buffer: NetworkBuffer, body: NetworkBuffer, index: Long, length: Long): Boolean {
        if (buffer.writableBytes() < length) {
            // Not enough space in the buffer
            return false
        }
        NetworkBuffer.copy(body, index, buffer, buffer.writeIndex(), length)
        buffer.advanceWrite(length)
        return true
    }

    private var writeLeftover: NetworkBuffer? = null

    @Throws(IOException::class)
    fun flushSync() {
        // Write leftover if any
        writeLeftover?.let { leftover ->
            val success = leftover.writeChannel(channel)
            if (success) {
                writeLeftover = null
                PacketVanilla.PACKET_POOL.add(leftover)
            } else {
                // Failed to write the whole leftover, try again next flush
                return
            }
        }
        // Consume queued packets
        val packetQueue = packetQueue
        if (packetQueue.isEmpty()) {
            try {
                // Can probably be improved by waking up at the end of the tick
                // But this works well enough and without additional state.
                Thread.sleep(1000L / TempConsts.SERVER_TICKS_PER_SECOND / 2)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        if (!channel.isConnected) throw EOFException("Channel is closed")
        val buffer = PacketVanilla.PACKET_POOL.get()
        // Write to buffer
        PacketWriting.writeQueue(buffer, packetQueue, 1) { b, packet ->
            val success = writeSendable(b, packet)
            if (success && packet is SetCompressionPacket) compressionStarted = true
            success
        }
        // Write to channel
        val success = buffer.writeChannel(channel)
        // Keep the buffer if not fully written
        if (success) {
            PacketVanilla.PACKET_POOL.add(buffer)
        } else {
            writeLeftover = buffer
        }
    }

    var packets: List<ClientPacket> = emptyList()
        private set
    private val incomingPlayPackets: ConcurrentLinkedQueue<ClientPacket> = ConcurrentLinkedQueue()

    fun tickStart() {
        packets = incomingPlayPackets.toList()
        incomingPlayPackets.clear()
    }

    fun sendPackets(packets: Collection<SendablePacket>) {
        packets.forEach { sendPacket(it) }
    }

    fun sendPackets(vararg packets: SendablePacket) {
        sendPackets(packets.toList())
    }

    @Suppress("UnstableApiUsage", "DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        sendPacket(SystemChatPacket(message, false))
    }
}