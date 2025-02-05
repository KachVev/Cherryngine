package ru.cherryngine.impl.demo

import io.micronaut.context.annotation.Parameter
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.modules.client.ClientModule
import ru.cherryngine.lib.math.Vec3D

@ModulePrototype
class Health(
    @Parameter override val gameObject: GameObject,
    @Parameter val maxHealth: Double
) : Module{

    var health = maxHealth

    fun damage(amount: Double) {
        health  = (health - amount).coerceAtLeast(0.0)
        if (health <= 0.0) kill()
    }

    fun kill() {
        gameObject.getModule(ClientModule::class)?.let {
            it.teleport(Vec3D(169.5, 73.5, 137.5))
            health = maxHealth
        }
    }
}