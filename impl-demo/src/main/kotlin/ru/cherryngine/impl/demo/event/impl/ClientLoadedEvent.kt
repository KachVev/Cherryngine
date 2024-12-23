package ru.cherryngine.impl.demo.event.impl

import ru.cherryngine.impl.demo.modules.ClientModule

data class ClientLoadedEvent(val clientModule: ClientModule)