package ru.cherryngine.engine.core.world.entity

import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntitySpawnType
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataHolder
import net.minestom.server.entity.RelativeFlags
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket
import net.minestom.server.network.packet.server.play.EntityPositionAndRotationPacket
import net.minestom.server.network.packet.server.play.EntityPositionPacket
import net.minestom.server.network.packet.server.play.EntityRotationPacket
import net.minestom.server.network.packet.server.play.EntityTeleportPacket
import net.minestom.server.network.packet.server.play.SpawnEntityPacket
import net.minestom.server.network.packet.server.play.SpawnExperienceOrbPacket
import ru.cherryngine.engine.core.server.ClientConnection
import java.util.UUID
import net.minestom.server.entity.Entity as MinestomEntity

class ProtocolEntity(
    val entityType: EntityType,
    val data: Int = 0,
) {
    val entityId: Int = MinestomEntity.generateId()
    val uuid: UUID = UUID.randomUUID()

    val metadata: MetadataHolder = MetadataHolder(null)
    val equipment: SEquipment? = null

    var position: Pos = Pos.ZERO
        set(value) {
            oldPosition = field
            field = value
        }

    var oldPosition: Pos = position

    var onGround: Boolean = false
    var velocity: Vec = Vec.ZERO

    val packetVelocity: Vec get() = velocity.mul((8000.0 / 20.0))

    val spawnPacket: ServerPacket.Play
        get() {
            return if (entityType.registry().spawnType() == EntitySpawnType.EXPERIENCE_ORB) {
                SpawnExperienceOrbPacket(entityId, position, data.toShort())
            } else {
                val packetVelocity = packetVelocity
                SpawnEntityPacket(
                    entityId,
                    uuid,
                    entityType.id(),
                    position,
                    position.yaw,
                    data,
                    packetVelocity.x.toInt().toShort(),
                    packetVelocity.y.toInt().toShort(),
                    packetVelocity.z.toInt().toShort(),
                )
            }
        }

    val destroyPacket: DestroyEntitiesPacket
        get() = DestroyEntitiesPacket(entityId)

    val metaPacket: EntityMetaDataPacket
        get() = EntityMetaDataPacket(entityId, metadata.entries)


    val teleportPacket: EntityTeleportPacket
        get() = EntityTeleportPacket(entityId, position, Vec.ZERO, RelativeFlags.NONE, onGround)


     /**
      * В следующих трёх пакетах надо сделать чек на дистанцию от oldPosition до position и менять пакеты от этого
     **/

    val positionPacket
        get() = EntityPositionPacket.getPacket(entityId, position, oldPosition, onGround)

    val rotationPacket
        get() = EntityRotationPacket(entityId, position.yaw, position.pitch, onGround)


    val positionAndRotationPacket
        get() = EntityPositionAndRotationPacket.getPacket(entityId, position, oldPosition, onGround)




    fun show(player: ClientConnection) {
        player.sendPacket(spawnPacket)
        val metaEntries = metadata.entries
        if (metaEntries.isNotEmpty()) player.sendPacket(EntityMetaDataPacket(entityId, metaEntries))
        if (equipment != null) player.sendPacket(equipment.packet(entityId))
    }

    fun hide(player: ClientConnection) {
        player.sendPacket(destroyPacket)
    }
}