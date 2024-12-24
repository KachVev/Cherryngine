package ru.cherryngine.engine.scenes.modules

import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.RelativeFlags
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.*
import ru.cherryngine.engine.core.minestomPos
import ru.cherryngine.engine.core.server.ClientConnection
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.event.EventBus
import ru.cherryngine.engine.scenes.event.impl.ClientLoadedEvent
import ru.cherryngine.engine.scenes.event.impl.ClientPacketEvent
import ru.cherryngine.lib.math.Vec3D

@Prototype
class ClientModule(
    @Parameter override val gameObject: GameObject,
    @Parameter val connection: ClientConnection,
) : Module {

    val viewDistance = 8

    override fun enable() {

        EventBus.GLOBAL.subscribe(ClientPacketEvent::class.java) {
            if (it.clientConnection != this.connection) return@subscribe
            bus.post(it)
        }

        spawn()
    }

    private fun spawn() {
        gameObject.transform.translation = Vec3D(169.5, 73.5, 137.5)
        val position = gameObject.transform.translation.minestomPos()

        val packets: MutableList<ServerPacket.Play> = ArrayList()

        packets += JoinGamePacket(
            -1, false, listOf(), 0,
            viewDistance, viewDistance,
            false, true, false,
            0, "world",
            0, GameMode.SURVIVAL, null, false, true,
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