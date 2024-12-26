package ru.cherryngine.engine.core.world.entity

import net.minestom.server.Viewable
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataHolder
import net.minestom.server.entity.metadata.EntityMeta
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta
import ru.cherryngine.engine.core.asVec3D
import ru.cherryngine.engine.core.minestomPos
import ru.cherryngine.engine.core.server.ClientConnection
import ru.cherryngine.lib.math.Vec3D
import kotlin.reflect.KClass

class SEntity(
    entityType: EntityType,
) {
    val protocolEntity = SProtocolEntity(entityType)
    var position: Vec3D = Vec3D.ZERO
        set(value) {
            field = value
            protocolEntity.position = position.minestomPos()
        }

    val viewMemberPosition: Vec3D get() = position

    private val viewers: HashSet<ClientConnection> = hashSetOf()

    fun <T : EntityMeta> editEntityMeta(metaClass: KClass<T>, editor: (T) -> Unit) {
        editEntityMeta {
            val constructor = metaClass.constructors.first()

            @Suppress("USELESS_CAST")
            val meta = constructor.call(null, it) as T
            editor(meta)
        }
    }

    fun editEntityMeta(editor: (MetadataHolder) -> Unit) {
        editor(protocolEntity.metadata)
        val metaPacket = protocolEntity.metaPacket()
        viewers.forEach { it.sendPacket(metaPacket) }
    }

    fun teleport(position: Pos? = null) {
        if (position != null) this.position = position.asVec3D()
        val teleportPacket = protocolEntity.teleportPacket()
        viewers.forEach { it.sendPacket(teleportPacket) }
    }

    fun show(viewers: Set<ClientConnection>) {
        this.viewers.addAll(viewers)
        val spawnPacket = protocolEntity.spawnPacket()
        val metaPacket = protocolEntity.metaPacket()
        viewers.forEach {
            it.sendPacket(spawnPacket)
            it.sendPacket(metaPacket)
        }
    }

    fun hide(viewers: Set<ClientConnection>) {
        val destroyPacket = protocolEntity.destroyPacket()
        viewers.forEach {
            it.sendPacket(destroyPacket)
        }
        this.viewers.removeAll(viewers)
    }

    fun remove() {
        val destroyPacket = protocolEntity.destroyPacket()
        viewers.forEach { it.sendPacket(destroyPacket) }
        viewers.clear()
    }
}