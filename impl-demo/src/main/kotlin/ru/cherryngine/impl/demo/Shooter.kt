package ru.cherryngine.impl.demo

import io.micronaut.context.annotation.Parameter
import net.minestom.server.network.packet.client.play.ClientAnimationPacket
import ru.cherryngine.engine.core.server.ClientConnection
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.event.impl.ClientPacketEvent
import ru.cherryngine.engine.scenes.modules.client.ClientModule
import ru.cherryngine.lib.math.Vec3D

@ModulePrototype
class Shooter(
    @Parameter override val gameObject: GameObject,
    @Parameter val clientModule: ClientModule
) : Module {

    override fun onEvent(event: Event) {
        when (event) {
            is ClientPacketEvent -> {
                when (val packet = event.packet) {
                    is ClientAnimationPacket -> {
                        shoot(event.clientConnection)
                    }
                }
            }
        }
    }

    fun shoot(clientConnection: ClientConnection) {
        if (clientModule.connection != clientConnection) return
        scene.createGameObject().let {
            it.transform.translation = gameObject.transform.global.translation + Vec3D(0.0, 1.8, 0.0)
            it.transform.rotation = gameObject.transform.global.rotation
            it.getOrCreateModule(Projectile::class)
        }
    }
}