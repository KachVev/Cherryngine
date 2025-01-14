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

    @ModulePrototype
    class Model (
        @Parameter override val gameObject: GameObject,
    ) : Module, Viewable {
        private val entity = EngineEntity(EntityType.ITEM_DISPLAY)

        override fun showFor(viewer: Viewer) {
            when (viewer) {
                is ClientModule -> {
                    entity.position = gameObject.transform.global.translation
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
                else -> viewer.show(this)
            }
        }
    }
}