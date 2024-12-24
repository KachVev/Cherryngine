package ru.cherryngine.engine.scenes

import ru.cherryngine.engine.scenes.event.EventBus

interface Module {
    val gameObject: GameObject

    val bus: EventBus
        get() = gameObject.bus

    fun enable() = Unit
    fun destroy() = Unit
}