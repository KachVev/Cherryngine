package ru.cherryngine.engine.scenes.modules

import io.micronaut.context.annotation.Parameter
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.Scene
import ru.cherryngine.engine.scenes.event.Event
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
        val viewersList = map.computeIfAbsent(viewable) { LinkedList<Viewer>() }

        if (canBeSeen(viewer, viewable)) {
            if (!viewersList.contains(viewer) && viewable.showFor(viewer)) {
                viewersList.add(viewer)
            }
        } else {
            if (viewersList.contains(viewer) && viewable.hideFor(viewer)) {
                viewersList.remove(viewer)
            }
        }
    }

    fun canBeSeen(viewer: Viewer, viewable: Viewable): Boolean {
        return viewer.gameObject != viewable.gameObject
    }

    override fun onEvent(event: Event) {
        when (event) {
            is Scene.Events.Tick.End -> {
                scene.getModules(Viewable::class).forEach { viewable ->
                    scene.getModules(Viewer::class).forEach { viewer ->
                        synchronize(viewer, viewable)
                    }
                }
            }
        }
    }

}