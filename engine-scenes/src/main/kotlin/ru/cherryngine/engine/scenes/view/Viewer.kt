package ru.cherryngine.engine.scenes.view

import ru.cherryngine.engine.scenes.Module

interface Viewer {
    fun show(viewable: Viewable) = Unit
    fun hide(viewable: Viewable) = Unit
}