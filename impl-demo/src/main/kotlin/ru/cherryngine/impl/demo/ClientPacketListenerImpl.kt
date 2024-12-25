package ru.cherryngine.impl.demo

import jakarta.inject.Singleton
import net.minestom.server.FeatureFlag
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.ChunkRange
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.RelativeFlags
import net.minestom.server.gamedata.tags.TagManager
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket
import net.minestom.server.network.packet.client.status.StatusRequestPacket
import net.minestom.server.network.packet.server.CachedPacket
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket
import net.minestom.server.network.packet.server.configuration.SelectKnownPacksPacket
import net.minestom.server.network.packet.server.configuration.UpdateEnabledFeaturesPacket
import net.minestom.server.network.packet.server.login.LoginSuccessPacket
import net.minestom.server.network.packet.server.play.*
import net.minestom.server.network.packet.server.status.ResponsePacket
import net.minestom.server.network.player.GameProfile
import net.minestom.server.registry.Registries
import net.minestom.server.world.DimensionType
import org.intellij.lang.annotations.Language
import ru.cherryngine.engine.core.getId
import ru.cherryngine.engine.core.minestomPos
import ru.cherryngine.engine.core.server.ClientConnection
import ru.cherryngine.engine.core.server.ClientPacketListener
import ru.cherryngine.engine.core.world.BlockHolder
import ru.cherryngine.engine.core.world.PolarChunkSupplier
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.View

@Singleton
class ClientPacketListenerImpl(
    private val registries: Registries,
    private val tagManager: TagManager,
) : ClientPacketListener {
    val defaultTagsPacket: CachedPacket by lazy { CachedPacket(tagManager.packet(registries)) }

    val viewDistance = 8
    val spawnPos = Vec3D(169.5, 73.5, 137.5)
    val spawnView: View = View(0f, 0f)
    val blockHolder: BlockHolder = BlockHolder(
        registries.dimensionType().get(DimensionType.OVERWORLD)!!,
        PolarChunkSupplier(javaClass.getResource("/world.polar")!!.readBytes(), registries)
    )

    data class StatusResponse(
        val versionName: String,
        val versionProtocol: Int,
        val playersMax: Int,
        val playersOnline: Int,
        val descriptionText: String,
        val enforcesSecureChat: Boolean,
        val previewsChat: Boolean,
    ) {
        @Language("JSON")
        fun toJson(): String {
            return """{"version":{"name":"$versionName","protocol":$versionProtocol},"players":{"max":$playersMax,"online":$playersOnline},"description":{"text":"$descriptionText"},"enforcesSecureChat":$enforcesSecureChat,"previewsChat":$previewsChat}"""
        }
    }

    private fun onStatusRequestPacket(clientConnection: ClientConnection, packet: StatusRequestPacket) {
        val jsonResponse = StatusResponse(
            MinecraftServer.VERSION_NAME,
            MinecraftServer.PROTOCOL_VERSION,
            100,
            0,
            "Cherryngine Demo",
            false,
            false
        ).toJson()
        clientConnection.sendPacket(ResponsePacket(jsonResponse))
    }

    private fun onClientLoginStartPacket(clientConnection: ClientConnection, packet: ClientLoginStartPacket) {
        clientConnection.sendPacket(LoginSuccessPacket(GameProfile(packet.profileId, packet.username)))
    }

    private fun onClientLoginAcknowledgedPacket(
        clientConnection: ClientConnection,
        packet: ClientLoginAcknowledgedPacket,
    ) {
        val excludeVanilla = true

        clientConnection.sendPacket(SelectKnownPacksPacket(listOf(SelectKnownPacksPacket.MINECRAFT_CORE)))

        val flags = listOf(
            FeatureFlag.REDSTONE_EXPERIMENTS,
            FeatureFlag.VANILLA,
            FeatureFlag.TRADE_REBALANCE,
            FeatureFlag.MINECART_IMPROVEMENTS
        )
        clientConnection.sendPacket(UpdateEnabledFeaturesPacket(flags.map(FeatureFlag::name)))

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
            clientConnection.sendPacket(dynamicRegistry.registryDataPacket(registries, excludeVanilla))
        }

        clientConnection.sendPacket(defaultTagsPacket)

        clientConnection.sendPacket(FinishConfigurationPacket())
    }

    private fun onClientFinishConfigurationPacket(
        clientConnection: ClientConnection,
        packet: ClientFinishConfigurationPacket,
    ) {
        val blockHolder = blockHolder
        val position = spawnPos.minestomPos(spawnView)

        val packets: MutableList<ServerPacket.Play> = ArrayList()

        val dimensionTypeId = registries.dimensionType().getId(blockHolder.dimensionType)!!

        packets += JoinGamePacket(
            -1, false, listOf(), 0,
            viewDistance, viewDistance,
            false, true, false,
            dimensionTypeId, "world",
            0, GameMode.CREATIVE, null, false, true,
            null, 0, 63, false
        )

        packets += SpawnPositionPacket(position, 0f)
        packets += PlayerPositionAndLookPacket(0, position, Vec.ZERO, 0f, 0f, RelativeFlags.NONE)

        packets += UpdateViewDistancePacket(viewDistance)
        packets += UpdateViewPositionPacket(position.chunkX(), position.chunkZ())
        ChunkRange.chunksInRange(
            position.chunkX(),
            position.chunkZ(),
            viewDistance
        ) { x: Int, z: Int ->
            blockHolder.generatePacket(x, z)?.let { packets += it }
        }

        packets += ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0f)

        clientConnection.sendPackets(packets)
    }

    override fun onPacketReceived(
        clientConnection: ClientConnection,
        packet: ClientPacket,
    ) {
        when (packet) {
            is StatusRequestPacket -> onStatusRequestPacket(clientConnection, packet)
            is ClientLoginStartPacket -> onClientLoginStartPacket(clientConnection, packet)
            is ClientLoginAcknowledgedPacket -> onClientLoginAcknowledgedPacket(clientConnection, packet)
            is ClientFinishConfigurationPacket -> onClientFinishConfigurationPacket(clientConnection, packet)
        }
    }
}