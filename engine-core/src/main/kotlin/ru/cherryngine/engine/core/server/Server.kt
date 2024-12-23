package ru.cherryngine.engine.core.server

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import net.minestom.server.FeatureFlag
import net.minestom.server.coordinate.ChunkRange
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.RelativeFlags
import net.minestom.server.gamedata.tags.TagManager
import net.minestom.server.network.ConnectionState
import net.minestom.server.network.packet.PacketVanilla
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket
import net.minestom.server.network.packet.server.CachedPacket
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.common.KeepAlivePacket
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket
import net.minestom.server.network.packet.server.configuration.SelectKnownPacksPacket
import net.minestom.server.network.packet.server.configuration.UpdateEnabledFeaturesPacket
import net.minestom.server.network.packet.server.login.LoginSuccessPacket
import net.minestom.server.network.packet.server.play.*
import net.minestom.server.network.player.GameProfile
import net.minestom.server.registry.Registries
import ru.cherryngine.engine.core.*
import java.io.EOFException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.StandardProtocolFamily
import java.nio.channels.ServerSocketChannel
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
class Server(
    val registries: Registries,
    val engineCoreConfig: EngineCoreConfig,
    val clientPacketListener: ClientPacketListener,
) {
    private val packetParser = PacketVanilla.CLIENT_PACKET_PARSER

    private val stopped = AtomicBoolean(false)
    private val server: ServerSocketChannel = ServerSocketChannel.open(StandardProtocolFamily.INET)

    private val connections = Collections.newSetFromMap(WeakHashMap<ClientConnection, Boolean>())

    val running get() = !stopped.get()

    @PostConstruct
    fun init() {
        val address = InetSocketAddress(engineCoreConfig.address, engineCoreConfig.port)
        server.bind(address)
        println("Server started on: $address")
        Thread.startVirtualThread { this.listenConnections() }
    }

    @PreDestroy
    fun stop() {
        stopped.set(true)
    }

    private fun listenConnections() {
        while (running) {
            try {
                val channel = server.accept()
                println("Accepted connection from ${channel.remoteAddress}")
                val clientConnection = ClientConnection(
                    channel,
                    registries,
                    engineCoreConfig.compressionThreshold,
                    clientPacketListener
                )
                connections += clientConnection

                Thread.startVirtualThread { playerReadLoop(clientConnection) }
                Thread.startVirtualThread { playerWriteLoop(clientConnection) }
                Thread.startVirtualThread { playerKeepAliveLoop(clientConnection) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun playerReadLoop(clientConnection: ClientConnection) {
        while (running) {
            try {
                // Чтение и обработка пакетов
                clientConnection.read(packetParser)
            } catch (_: EOFException) {
                clientConnection.disconnect()
                break
            } catch (e: Throwable) {
                val isExpected = e is SocketException && e.message == "Connection reset"
                if (!isExpected) e.printStackTrace()
                clientConnection.disconnect()
                break
            }
        }
    }

    private fun playerWriteLoop(clientConnection: ClientConnection) {
        while (running) {
            try {
                clientConnection.flushSync()
            } catch (_: EOFException) {
                clientConnection.disconnect()
                break
            } catch (e: Throwable) {
                val isExpected = e is IOException && e.message == "Broken pipe"
                if (!isExpected) e.printStackTrace()
                clientConnection.disconnect()
                break
            }

            if (!clientConnection.online) {
                try {
                    clientConnection.flushSync()
                    clientConnection.channel.close()
                    break
                } catch (_: IOException) {
                    // Отключение
                    break
                }
            }
        }
    }

    private fun playerKeepAliveLoop(clientConnection: ClientConnection) {
        while (running) {
            if (!clientConnection.online) break
            // KeepAlive нужен только в состоянии CONFIGURATION и PLAY
            // Если клиент находится в STATUS, то можно обрывать цикл
            when (clientConnection.connectionState) {
                ConnectionState.HANDSHAKE, ConnectionState.LOGIN -> Unit
                ConnectionState.STATUS -> break
                ConnectionState.CONFIGURATION, ConnectionState.PLAY -> {
                    clientConnection.sendPacket(KeepAlivePacket(System.currentTimeMillis()))
                }
            }

            Thread.sleep(20 * 1000)
        }
    }
}