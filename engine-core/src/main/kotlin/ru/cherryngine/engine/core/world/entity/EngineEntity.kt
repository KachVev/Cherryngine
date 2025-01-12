package ru.cherryngine.engine.core.world.entity

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataHolder
import net.minestom.server.entity.metadata.EntityMeta
import ru.cherryngine.engine.core.*
import ru.cherryngine.engine.core.server.ClientConnection
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.View
import kotlin.reflect.KClass

class EngineEntity(
    entityType: EntityType,
) {
    val protocolEntity = ProtocolEntity(entityType)

    var rotation: View
        get() {
            return protocolEntity.position.asView()
        }
        set(value) {
            protocolEntity.position = value.minestomPos(protocolEntity.position.asVec3D())
        }

    var position: Vec3D
        get() {
            return protocolEntity.position.asVec3D()
        }
        set(value) {
            protocolEntity.position = value.minestomPos()
        }

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
        val metaPacket = protocolEntity.metaPacket
        viewers.forEach { it.sendPacket(metaPacket) }
    }

    fun teleport(position: Pos? = null) {
        if (position != null) this.position = position.asVec3D()
        protocolEntity.teleportPacket.let {
            viewers.forEach(ClientConnection::sendPackets)
        }
    }

    fun updatePositionAndRotation(position: Vec3D? = null, rotation: View? = null) {
        val positionChanged = position?.let {
            if (it != protocolEntity.position.asVec3D()) {
                this.position = it
                true
            } else false
        } ?: false

        val rotationChanged = rotation?.let {
            if (it != protocolEntity.position.asView()) {
                this.rotation = it
                true
            } else false
        } ?: false

        when {
            positionChanged && rotationChanged -> protocolEntity.positionAndRotationPacket
            positionChanged -> protocolEntity.positionPacket
            rotationChanged -> protocolEntity.rotationPacket
            else -> null
        }?.let {
            viewers.forEach(ClientConnection::sendPackets)
        }
    }

    fun show(viewer: ClientConnection) {
        this.viewers.add(viewer)
        val spawnPacket = protocolEntity.spawnPacket
        val metaPacket = protocolEntity.metaPacket
        viewer.sendPacket(spawnPacket)
        viewer.sendPacket(metaPacket)
    }

    fun hide(viewer: ClientConnection) {
        val destroyPacket = protocolEntity.destroyPacket
        viewers.forEach {
            it.sendPacket(destroyPacket)
        }
        this.viewers.removeAll(viewers)
    }

    fun remove() {
        val destroyPacket = protocolEntity.destroyPacket
        viewers.forEach { it.sendPacket(destroyPacket) }
        viewers.clear()
    }
}