package ru.cherryngine.engine.scenes.modules

import io.micronaut.context.annotation.Parameter
import net.minestom.server.entity.EntityType
import ru.cherryngine.engine.core.world.entity.EngineEntity
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.modules.client.ClientModule
import ru.cherryngine.engine.scenes.view.Viewable
import ru.cherryngine.engine.scenes.view.Viewer
import java.util.UUID

@ModulePrototype
class ModelRenderer(
    @Parameter override val gameObject: GameObject
) : Module, Viewable {

    private val bones: MutableMap<UUID, Model> = HashMap()

    override fun enable() {

    }

    override fun showFor(viewer: Viewer): Boolean {
        return when {
            viewer is ClientModule -> {
                bones.values.forEach { it.show(viewer) }
                true
            }
            else -> viewer.show(this)
        }
    }

    override fun hideFor(viewer: Viewer): Boolean {
        return when {
            viewer is ClientModule -> {
                bones.values.forEach { it.hide(viewer) }
                true
            }
            else -> viewer.hide(this)
        }
    }

    class Model (
    ) {
        val entity = EngineEntity(EntityType.ITEM_DISPLAY)

        fun show(clientModule: ClientModule) {
            entity.show(clientModule.connection)
        }

        fun hide(clientModule: ClientModule) {
            entity.hide(clientModule.connection)
        }

    }
}