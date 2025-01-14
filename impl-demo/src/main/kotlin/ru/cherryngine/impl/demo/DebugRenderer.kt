package ru.cherryngine.impl.demo

import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import net.minestom.server.entity.EntityType
import ru.cherryngine.engine.core.world.entity.EngineEntity
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.view.Viewable
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.event.impl.SceneTickEvent
import ru.cherryngine.engine.scenes.modules.client.ClientModule
import ru.cherryngine.engine.scenes.view.Viewer
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.View
import ru.cherryngine.lib.math.rotation.AxisSequence
import java.util.LinkedList
import kotlin.math.asin
import kotlin.math.atan2

@Prototype
class DebugRenderer(
    @Parameter override val gameObject: GameObject
) : Module, Viewable {

    val entity = EngineEntity(EntityType.ZOMBIE)

    override fun onEvent(event: Event) {
        when (event) {
            is SceneTickEvent -> {
                gameObject.transform.global.apply {
                    val rot = rotation.toAxisAngleSequence(AxisSequence.YXZ)
                    entity.updatePositionAndRotation(translation, View((-rot.angle1 * (180 / Math.PI)).toFloat(), (rot.angle2 * (180 / Math.PI)).toFloat()))
                }
            }
        }
    }

    override fun showFor(viewer: Viewer) {
        when (viewer) {
            is ClientModule -> {
                entity.updatePositionAndRotation(gameObject.transform.global.translation)
                entity.show(viewer.connection)
            }
            else -> viewer.show(this)
        }
    }

    override fun hideFor(viewer: Viewer) {
        when (viewer) {
            is ClientModule -> {
                entity.hide(viewer.connection)
            }
            else -> viewer.hide(this)
        }
    }
}