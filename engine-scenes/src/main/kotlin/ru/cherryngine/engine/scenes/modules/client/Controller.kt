package ru.cherryngine.engine.scenes.modules.client

import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.rotation.QRot

interface Controller {
    fun setPos(vec: Vec3D)
    fun setRot(rot: QRot)
}