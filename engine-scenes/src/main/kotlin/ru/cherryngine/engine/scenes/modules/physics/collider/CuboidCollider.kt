package ru.cherryngine.engine.scenes.modules.physics.collider

import io.micronaut.context.annotation.Parameter
import net.minestom.server.coordinate.Vec
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.view.Viewable
import ru.cherryngine.engine.scenes.view.Viewer
import ru.cherryngine.lib.math.Cuboid
import ru.cherryngine.lib.math.Vec3D

@ModulePrototype
class CuboidCollider(
    @Parameter override val gameObject: GameObject,
    @Parameter var localCuboid: Cuboid
) : Collider {

    val globalCuboid
        get() = gameObject.transform.global.let {
            Cuboid.fromTwoPoints(it.translation + localCuboid.min, it.translation + localCuboid.max)
        }

    override fun isCollide(other: Collider): Boolean {
        return when (other) {
            is CuboidCollider -> {
                globalCuboid.isCollide(other.globalCuboid) || other.globalCuboid.isCollide(globalCuboid)
            }
            else -> other.isCollide(this)
        }
    }

}