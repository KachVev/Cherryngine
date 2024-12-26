package ru.cherryngine.engine.scenes.view

import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module

interface Viewable {
    fun showFor(module: Module) = Unit
    fun hideFor(module: Module) = Unit
}