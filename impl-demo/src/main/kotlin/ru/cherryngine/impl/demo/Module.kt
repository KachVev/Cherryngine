package ru.cherryngine.impl.demo

import ru.cherryngine.impl.demo.event.EventBus

abstract class Module(var gameObject: GameObject?) {

    val bus: EventBus
        get() = gameObject!!.bus

    abstract fun enable()
    abstract fun destroy()
}