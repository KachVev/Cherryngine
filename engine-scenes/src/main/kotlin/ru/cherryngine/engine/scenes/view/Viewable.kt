package ru.cherryngine.engine.scenes.view

import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module

interface Viewable : Module {
    fun showFor(viewer: Viewer) = Unit
    fun hideFor(viewer: Viewer) = Unit
}