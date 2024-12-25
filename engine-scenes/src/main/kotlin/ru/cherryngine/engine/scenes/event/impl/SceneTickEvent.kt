package ru.cherryngine.engine.scenes.event.impl

import ru.cherryngine.engine.scenes.Scene
import ru.cherryngine.engine.scenes.event.Event

data class SceneTickEvent(
    val scene: Scene,
) : Event