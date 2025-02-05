package ru.cherryngine.engine.scenes.modules.physics

import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.Scene
import ru.cherryngine.engine.scenes.event.Event

interface Physics : Module {
    override fun onEvent(event: Event) {
        when (event) {
            is Scene.Events.Tick.Physics -> update()
        }
    }

    fun update() {
        updateColliders()
    }

    fun updateColliders()

}