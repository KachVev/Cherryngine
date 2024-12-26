package ru.cherryngine.engine.scenes

import ru.cherryngine.engine.scenes.event.Event

interface Module {
    val gameObject: GameObject

    val scene: Scene
        get() = gameObject.scene

    fun enable() = Unit
    fun destroy() = Unit
    fun onEvent(event: Event) = Unit
}