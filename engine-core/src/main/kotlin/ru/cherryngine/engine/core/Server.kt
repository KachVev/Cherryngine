package ru.cherryngine.engine.core

import jakarta.annotation.PostConstruct
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
import ru.cherryngine.engine.core.connection.Client
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
    val tagManager: TagManager,
    val engineCoreConfig: EngineCoreConfig,
    val defaultWorldProvider: DefaultWorldProvider,
) {
    private val packetParser = PacketVanilla.CLIENT_PACKET_PARSER
    val defaultTagsPacket: CachedPacket by lazy { CachedPacket(tagManager.packet(registries)) }

    private val stopped = AtomicBoolean(false)
    private val server: ServerSocketChannel = ServerSocketChannel.open(StandardProtocolFamily.INET)
    private var keepAliveId = 0

    private val connections = Collections.newSetFromMap(WeakHashMap<Client, Boolean>())

    val running get() = !stopped.get()

    @PostConstruct
    fun init() {
        val address = InetSocketAddress(engineCoreConfig.address, engineCoreConfig.port)
        server.bind(address)
        println("Server started on: $address")
        Thread.startVirtualThread { this.listenConnections() }

        Thread({
            while (running) tick()
            server.close()
            println("Server stopped")
        }, this::class.simpleName).start()
    }

    private fun listenConnections() {
        while (running) {
            try {
                val channel = server.accept()
                println("Accepted connection from ${channel.remoteAddress}")
                val client = Client(channel, registries)
                connections += client

                Thread.startVirtualThread { playerReadLoop(client) }
                Thread.startVirtualThread { playerWriteLoop(client) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    fun baseOnPacket(client: Client, packet: ClientPacket) {
        when (packet) {
            is ClientLoginStartPacket -> {
                if (TempConsts.COMPRESSION_THRESHOLD > 0) client.startCompression()
                client.sendPacket(LoginSuccessPacket(GameProfile(packet.profileId, packet.username)))
            }

            is ClientLoginAcknowledgedPacket -> {
                val excludeVanilla = true

                client.sendPacket(SelectKnownPacksPacket(listOf(SelectKnownPacksPacket.MINECRAFT_CORE)))

                val flags = listOf(
                    FeatureFlag.REDSTONE_EXPERIMENTS,
                    FeatureFlag.VANILLA,
                    FeatureFlag.TRADE_REBALANCE,
                    FeatureFlag.MINECART_IMPROVEMENTS
                )
                client.sendPacket(UpdateEnabledFeaturesPacket(flags.map(FeatureFlag::name)))

                sequenceOf(
                    registries.chatType(),
                    registries.dimensionType(),
                    registries.biome(),
                    registries.damageType(),
                    registries.trimMaterial(),
                    registries.trimPattern(),
                    registries.bannerPattern(),
                    registries.wolfVariant(),
                    registries.enchantment(),
                    registries.paintingVariant(),
                    registries.jukeboxSong(),
                    registries.instrument(),
                ).forEach { dynamicRegistry ->
                    client.sendPacket(dynamicRegistry.registryDataPacket(registries, excludeVanilla))
                }

                client.sendPacket(defaultTagsPacket)

                client.sendPacket(FinishConfigurationPacket())
            }

            is ClientFinishConfigurationPacket -> {
                val blockHolder = defaultWorldProvider.blockHolder
                val position = defaultWorldProvider.spawnPos.minestomPos(defaultWorldProvider.spawnView)

                val packets: MutableList<ServerPacket.Play> = ArrayList()

                val dimensionTypeId = registries.dimensionType().getId(blockHolder.dimensionType)!!

                packets += JoinGamePacket(
                    -1, false, listOf(), 0,
                    TempConsts.VIEW_DISTANCE, TempConsts.VIEW_DISTANCE,
                    false, true, false,
                    dimensionTypeId, "world",
                    0, GameMode.SURVIVAL, null, false, true,
                    null, 0, 63, false
                )

                packets += SpawnPositionPacket(position, 0f)
                packets += PlayerPositionAndLookPacket(0, position, Vec.ZERO, 0f, 0f, RelativeFlags.NONE)

                packets += UpdateViewDistancePacket(TempConsts.VIEW_DISTANCE)
                packets += UpdateViewPositionPacket(position.chunkX(), position.chunkZ())
                ChunkRange.chunksInRange(
                    position.chunkX(),
                    position.chunkZ(),
                    TempConsts.VIEW_DISTANCE
                ) { x: Int, z: Int ->
                    blockHolder.generatePacket(x, z)?.let { packets += it }
                }

                packets += ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0f)

                client.sendPackets(packets)
            }
        }
    }

    private fun playerReadLoop(client: Client) {
        while (running) {
            try {
                // Чтение и обработка пакетов
                client.read(packetParser)
            } catch (_: EOFException) {
                client.disconnect()
                break
            } catch (e: Throwable) {
                val isExpected = e is SocketException && e.message == "Connection reset"
                if (!isExpected) e.printStackTrace()
                client.disconnect()
                break
            }
        }
    }

    private fun playerWriteLoop(client: Client) {
        while (running) {
            try {
                client.flushSync()
            } catch (_: EOFException) {
                client.disconnect()
                break
            } catch (e: Throwable) {
                val isExpected = e is IOException && e.message == "Broken pipe"
                if (!isExpected) e.printStackTrace()
                client.disconnect()
                break
            }

            if (!client.online) {
                try {
                    client.flushSync()
                    client.channel.close()
                    break
                } catch (_: IOException) {
                    // Отключение
                    break
                }
            }
        }
    }

    private fun tick() {
        val sendKeepAlive = keepAliveId++ % (20 * 20) == 0
        if (sendKeepAlive) connections.forEach {
            if (it.online && (it.connectionState == ConnectionState.CONFIGURATION || it.connectionState == ConnectionState.PLAY)) {
                it.sendPacket(KeepAlivePacket(keepAliveId.toLong()))
            }
        }

        connections.forEach(Client::tickStart)

        connections.forEach { client ->
            client.packets.forEach { packet ->
                baseOnPacket(client, packet)
            }
        }

        Thread.sleep(50) // типа 20 тпс
    }
}
