package ru.cherryngine.engine.core.world.entity

import net.minestom.server.ServerFlag
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntitySpawnType
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataHolder
import net.minestom.server.entity.RelativeFlags
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket
import net.minestom.server.network.packet.server.play.EntityTeleportPacket
import net.minestom.server.network.packet.server.play.SpawnEntityPacket
import net.minestom.server.network.packet.server.play.SpawnExperienceOrbPacket
import ru.cherryngine.engine.core.server.ClientConnection
import java.util.UUID
import net.minestom.server.entity.Entity as MinestomEntity

class SProtocolEntity(
    val entityType: EntityType,
    val data: Int = 0,
) {
    val entityId: Int = MinestomEntity.generateId()
    val uuid: UUID = UUID.randomUUID()

    val metadata: MetadataHolder = MetadataHolder(null)
    val equipment: SEquipment? = null

    var position: Pos = Pos.ZERO
    var onGround: Boolean = false
    var velocity: Vec = Vec.ZERO

    val packetVelocity: Vec get() = velocity.mul((8000.0 / 20.0))

    fun spawnPacket(): ServerPacket.Play {
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

    fun destroyPacket(): DestroyEntitiesPacket {
        return DestroyEntitiesPacket(entityId)
    }

    fun metaPacket(): EntityMetaDataPacket {
        return EntityMetaDataPacket(entityId, metadata.entries)
    }

    fun teleportPacket(): EntityTeleportPacket {
        return EntityTeleportPacket(entityId, position, Vec.ZERO, RelativeFlags.NONE, onGround)
    }

    fun show(player: ClientConnection) {
        player.sendPacket(spawnPacket())
        val metaEntries = metadata.entries
        if (metaEntries.isNotEmpty()) player.sendPacket(EntityMetaDataPacket(entityId, metaEntries))
        if (equipment != null) player.sendPacket(equipment.packet(entityId))
    }

    fun hide(player: ClientConnection) {
        player.sendPacket(destroyPacket())
    }
}