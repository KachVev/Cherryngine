package ru.cherryngine.impl.demo

import io.micronaut.context.annotation.Parameter
import net.kyori.adventure.text.Component
import net.minestom.server.network.packet.client.play.ClientAnimationPacket
import net.minestom.server.network.packet.server.play.ExplosionPacket
import net.minestom.server.network.packet.server.play.HitAnimationPacket
import net.minestom.server.network.packet.server.play.PlayerChatMessagePacket
import net.minestom.server.network.packet.server.play.SystemChatPacket
import ru.cherryngine.engine.core.server.ClientConnection
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.event.impl.ClientPacketEvent
import ru.cherryngine.lib.math.Vec3D

@ModulePrototype
class Shooter(
    @Parameter override val gameObject: GameObject
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
        scene.createGameObject().let {
            it.transform.translation = gameObject.transform.global.translation + Vec3D(0.0, 1.8, 0.0)
            it.transform.rotation = gameObject.transform.global.rotation
            it.getOrCreateModule(Projectile::class)
        }
    }
}