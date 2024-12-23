package ru.cherryngine.impl.demo.modules

import net.minestom.server.coordinate.ChunkRange
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.RelativeFlags
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.*
import net.minestom.server.registry.Registries
import net.minestom.server.world.DimensionType
import ru.cherryngine.engine.core.getId
import ru.cherryngine.engine.core.minestomPos
import ru.cherryngine.engine.core.server.ClientConnection
import ru.cherryngine.impl.demo.GameObject
import ru.cherryngine.impl.demo.Module
import ru.cherryngine.impl.demo.event.EventBus
import ru.cherryngine.impl.demo.event.impl.ClientLoadedEvent
import ru.cherryngine.impl.demo.event.impl.ClientPacketEvent
import ru.cherryngine.lib.math.Vec3D

class ClientModule(val connection: ClientConnection, gameObject: GameObject? = null) : Module(gameObject) {

    val viewDistance = 8

    override fun enable() {

        EventBus.GLOBAL.subscribe(ClientPacketEvent::class.java) {
            if (it.clientConnection != this.connection) return@subscribe
            bus.post(it)
        }

        spawn()
    }

    private fun spawn() {
        gameObject!!.transform.position = Vec3D(169.5, 73.5, 137.5)
        val position = gameObject!!.transform.position.minestomPos()

        val packets: MutableList<ServerPacket.Play> = ArrayList()

        packets += JoinGamePacket(
            -1, false, listOf(), 0,
            viewDistance, viewDistance,
            false, true, false,
            0, "world",
            0, GameMode.SPECTATOR, null, false, true,
            null, 0, 63, false
        )

        packets += SpawnPositionPacket(position, 0f)
        packets += PlayerPositionAndLookPacket(0, position, Vec.ZERO, 0f, 0f, RelativeFlags.NONE)

        packets += UpdateViewDistancePacket(viewDistance)
        packets += UpdateViewPositionPacket(position.chunkX(), position.chunkZ())

        packets += ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0f)

        connection.sendPackets(packets)

        bus.post(ClientLoadedEvent(this))
    }

    override fun destroy() {

    }
}