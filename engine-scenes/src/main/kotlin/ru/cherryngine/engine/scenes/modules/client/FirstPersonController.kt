package ru.cherryngine.engine.scenes.modules.client

import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket
import ru.cherryngine.engine.core.asQRot
import ru.cherryngine.engine.core.asVec3D
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.event.impl.ClientPacketEvent
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.View
import ru.cherryngine.lib.math.rotation.QRot

@Prototype
class FirstPersonController(
    @Parameter override val gameObject: GameObject,
    @Parameter val clientModule: ClientModule
) : Module, Controller, Camera {

    val onPacket: (ClientPacketEvent) -> Unit = entry@ {
        if (it.clientConnection != clientModule.connection) return@entry
        when (it.packet) {
            is ClientPlayerPositionPacket -> setPos(it.packet.position.asVec3D())
            is ClientPlayerPositionAndRotationPacket -> {
                setPos(it.packet.position.asVec3D())
                gameObject.transform.rotation = it.packet.position.asQRot()
            }
            is ClientPlayerRotationPacket -> {
                gameObject.transform.rotation = View(it.packet.yaw, it.packet.pitch).getRotation()
            }
        }
    }

    override fun setPos(vec: Vec3D) {
        gameObject.transform.translation = vec
    }

    override fun setRot(rot: QRot) {
        gameObject.transform.rotation = rot
    }

    override fun enable() {
        bus.subscribe(ClientPacketEvent::class.java, onPacket)
    }

    override fun destroy() {
        bus.unsubscribe(ClientPacketEvent::class.java, onPacket)
    }

}