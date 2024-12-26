package ru.cherryngine.engine.scenes.view

import ru.cherryngine.engine.scenes.Module

interface Viewer {
    fun show(module: Module) = Unit
    fun hide(module: Module) = Unit
}