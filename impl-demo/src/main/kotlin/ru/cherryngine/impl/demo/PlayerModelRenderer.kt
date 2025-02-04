package ru.cherryngine.impl.demo

import io.micronaut.context.annotation.Parameter
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import ru.cherryngine.engine.core.asVec3D
import ru.cherryngine.engine.core.minestomVec
import ru.cherryngine.engine.core.world.entity.EngineEntity
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.Scene
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.modules.client.ClientModule
import ru.cherryngine.engine.scenes.view.Viewable
import ru.cherryngine.engine.scenes.view.Viewer
import ru.cherryngine.lib.math.Vec3D

@ModulePrototype
class PlayerModelRenderer(
    @Parameter override val gameObject: GameObject
) : Viewable {

    val head = EngineEntity(EntityType.ITEM_DISPLAY).apply {
        editEntityMeta {
            it.set(MetadataDef.ItemDisplay.DISPLAYED_ITEM, ItemStack.of(Material.OBSERVER))
        }
    }

    val body = EngineEntity(EntityType.ITEM_DISPLAY).apply {
        editEntityMeta {
            it.set(MetadataDef.ItemDisplay.DISPLAYED_ITEM, ItemStack.of(Material.MELON))
        }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is Scene.Events.Tick.Physic -> {
                onUpdate()
            }
        }
    }

    fun onUpdate() {
        gameObject.transform.global.apply {
            val bodyScale = scale.times(.7, 1.8 * .75, .7)
            body.updatePositionAndRotation(translation + bodyScale.times(.0, .5, .0))
            body.editEntityMeta {
                it.set(
                    MetadataDef.ItemDisplay.SCALE,
                    bodyScale.minestomVec()
                )
            }

            val headScale = (scale * 1.0)
            head.updatePositionAndRotation(translation + bodyScale.times(.0, 1.0, .0) + headScale.times(.0, .5, .0) - Vec3D(0.0, .1, .0))
            head.editEntityMeta {
                it.set(
                    MetadataDef.ItemDisplay.SCALE,
                    headScale.minestomVec()
                )
                val view = rotation
                it.set(
                    MetadataDef.ItemDisplay.ROTATION_LEFT,
                    floatArrayOf(view.x.toFloat(), view.y.toFloat(), view.z.toFloat(), view.w.toFloat())
                )
            }
        }
    }


    override fun showFor(viewer: Viewer): Boolean {
        return when (viewer) {
            is ClientModule -> {
                onUpdate()
                head.show(viewer.connection)
                body.show(viewer.connection)
                true
            }
            else -> viewer.show(this)
        }
    }

    override fun hideFor(viewer: Viewer): Boolean {
        return when (viewer) {
            is ClientModule -> {
                head.hide(viewer.connection)
                body.hide(viewer.connection)
                true
            }
            else -> viewer.show(this)
        }
    }

}