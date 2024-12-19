package ru.cherryngine.engine.core

import net.minestom.server.coordinate.Pos
import ru.cherryngine.engine.core.world.BlockHolder

interface DefaultWorldProvider {
    val blockHolder: BlockHolder
    val spawnPos: Pos
}