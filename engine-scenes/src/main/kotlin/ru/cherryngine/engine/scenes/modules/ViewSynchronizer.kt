package ru.cherryngine.engine.scenes.modules

import com.google.gson.internal.LinkedTreeMap
import io.micronaut.context.annotation.Parameter
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.event.impl.SceneTickEvent
import ru.cherryngine.engine.scenes.view.Viewable
import ru.cherryngine.engine.scenes.view.Viewer
import java.util.LinkedHashMap
import java.util.LinkedList

@ModulePrototype
class ViewSynchronizer(
    @Parameter override val gameObject: GameObject
) : Synchronizer {

    private val map: MutableMap<Viewable, MutableList<Viewer>> = LinkedHashMap()

    fun synchronize(viewer: Viewer, viewable: Viewable) {
        if (canBeSeen(viewer, viewable)) {
            map.computeIfAbsent(viewable) { LinkedList() }.let {
                if (!it.contains(viewer)) {
                    viewable.showFor(viewer)
                    it.add(viewer)
                }
            }
        } else {
            map[viewable]?.let {
                if (it.contains(viewer)) {
                    viewable.hideFor(viewer)
                    it.remove(viewer)
                }
            }
        }
    }

    fun canBeSeen(viewer: Viewer, viewable: Viewable): Boolean {
        return viewer.gameObject != viewable.gameObject
    }

    override fun onEvent(event: Event) {
        when (event) {
            is SceneTickEvent -> {
                scene.getModules(Viewable::class).forEach { viewable ->
                    scene.getModules(Viewer::class).forEach { viewer ->
                        synchronize(viewer, viewable)
                    }
                }
            }
        }
    }

}