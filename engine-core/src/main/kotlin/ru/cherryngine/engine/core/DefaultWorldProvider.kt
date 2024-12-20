package ru.cherryngine.engine.core

import ru.cherryngine.engine.core.world.BlockHolder
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.View

interface DefaultWorldProvider {
    val blockHolder: BlockHolder
    val spawnPos: Vec3D
    val spawnView: View
}