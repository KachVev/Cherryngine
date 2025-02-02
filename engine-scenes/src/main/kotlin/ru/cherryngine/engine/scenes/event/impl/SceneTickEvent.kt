package ru.cherryngine.engine.scenes.event.impl

import ru.cherryngine.engine.scenes.event.Event

interface SceneEvents {

    object Start : Event

    object Stop : Event

    interface Tick {

        object Start : Event

        object End : Event

        object Physic : Event

    }

}