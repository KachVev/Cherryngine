package ru.cherryngine.impl.demo

import io.micronaut.context.annotation.Parameter
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import ru.cherryngine.engine.core.minestomVec
import ru.cherryngine.engine.core.world.entity.EngineEntity
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.Scene
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.modules.client.ClientModule
import ru.cherryngine.engine.scenes.view.Viewable
import ru.cherryngine.engine.scenes.view.Viewer

@ModulePrototype
class Projectile(
    @Parameter override val gameObject: GameObject
) : Viewable{

    val entity = EngineEntity(EntityType.ITEM_DISPLAY).apply {
        editEntityMeta {
            it.set(MetadataDef.ItemDisplay.DISPLAYED_ITEM, ItemStack.of(Material.FIRE_CHARGE))
            it.set(
                MetadataDef.ItemDisplay.POSITION_ROTATION_INTERPOLATION_DURATION,
                2
            )
        }
    }

    var damage = 100L

    override fun onEvent(event: Event) {
        when (event) {
            is Scene.Events.Tick.Physic -> {
                gameObject.transform.translation += gameObject.transform.rotation.asView().direction() * 1.0
                entity.updatePositionAndRotation(gameObject.transform.global.translation, gameObject.transform.global.rotation.asView())
                if (damage-- <= 0) gameObject.destroy()
            }
        }
    }

    override fun showFor(viewer: Viewer): Boolean {
        return when (viewer) {
            is ClientModule -> {
                entity.updatePositionAndRotation(gameObject.transform.global.translation)
                entity.show(viewer.connection)
                true
            }
            else -> viewer.show(this)
        }
    }

    override fun hideFor(viewer: Viewer): Boolean {
        return when (viewer) {
            is ClientModule -> {
                entity.hide(viewer.connection)
                true
            }
            else -> viewer.show(this)
        }
    }

}