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

@Prototype
class DebugRenderer(
    @Parameter override val gameObject: GameObject
) : Module, Viewable {

    val entity = EngineEntity(EntityType.CREEPER)

    val onTick: (SceneTickEvent) -> Unit = entry@ { event ->
        val module = event.scene.getModules(ClientModule::class)
        entity.position = gameObject.transform.translation
        module.forEach {
            if (it.gameObject != gameObject) showFor(it)
            else hideFor(it)
        }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is SceneTickEvent -> onTick(event)
        }
    }

    override fun showFor(viewer: Viewer) {
        when (viewer) {
            is ClientModule -> entity.show(viewer.connection)
            else -> viewer.show(this)
        }
    }

    override fun hideFor(viewer: Viewer) {
        when (viewer) {
            is ClientModule -> entity.hide(viewer.connection)
            else -> viewer.hide(this)
        }
    }
}