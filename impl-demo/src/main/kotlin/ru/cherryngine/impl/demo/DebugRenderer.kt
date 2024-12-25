package ru.cherryngine.impl.demo

import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.ObjectDataProvider
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.SpawnEntityPacket
import ru.cherryngine.engine.core.minestomPos
import ru.cherryngine.engine.core.server.ClientConnection
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.event.impl.SceneTickEvent
import ru.cherryngine.engine.scenes.modules.client.ClientModule

@Prototype
class DebugRenderer(
    @Parameter override val gameObject: GameObject
) : Module {

    val entity = Entity(EntityType.ZOMBIE)

    val onTick: (SceneTickEvent) -> Unit = entry@ { event ->
        event.scene.gameObjects.values.forEach {
            val module = it.getModule(ClientModule::class)
            if (module != null) {
                sendPos((module as ClientModule).connection)
            }
        }
    }

    fun sendPos(connection: ClientConnection) {
        val packets = HashSet<ServerPacket>()
        packets.add(
            getSpawnPacket(entity)
        )

        connection.sendPackets(packets)
    }

    private fun getSpawnPacket(entity: Entity): SpawnEntityPacket {
        var data = 0
        val velocityX: Short = 0
        val velocityZ: Short = 0
        val velocityY: Short = 0
        if (entity.entityMeta is ObjectDataProvider) {
            data = (entity.entityMeta as ObjectDataProvider).getObjectData()
        }
        val position = gameObject.transform.translation.minestomPos()
        return SpawnEntityPacket(
            entity.entityId, entity.uuid, entity.entityType.id(),
            position, (System.currentTimeMillis() / 8 % 360).toFloat(), data, velocityX, velocityY, velocityZ
        )
    }

    override fun onEvent(event: Event) {
        if (event is SceneTickEvent) {
            onTick(event)
        }
    }
}