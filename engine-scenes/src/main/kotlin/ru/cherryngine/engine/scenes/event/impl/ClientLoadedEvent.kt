package ru.cherryngine.engine.scenes.event.impl

import ru.cherryngine.engine.scenes.modules.ClientModule

data class ClientLoadedEvent(
    val clientModule: ClientModule
)