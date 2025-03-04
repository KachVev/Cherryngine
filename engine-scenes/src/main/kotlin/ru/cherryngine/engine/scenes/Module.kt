package ru.cherryngine.engine.scenes

import ru.cherryngine.engine.scenes.event.Event

interface Module {

    val gameObject: GameObject

    val scene: Scene
        get() = gameObject.scene

    fun enable() = Unit
    fun destroy() = Unit
    fun onEvent(event: Event) = Unit

    interface Events {

        interface Destroy {

            data class Pre (
                val module: Module
            ) : Event

            data class Post (
                val module: Module
            ) : Event

        }

        interface Enable {

            data class Pre (
                val module: Module
            ) : Event

            data class Post (
                val module: Module
            ) : Event

        }

    }
}