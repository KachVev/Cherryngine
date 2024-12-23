package ru.cherryngine.impl.demo.modules

import ru.cherryngine.impl.demo.GameObject
import ru.cherryngine.impl.demo.Module
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.rotation.QRot

class Transform(gameObject: GameObject) : Module(gameObject) {

    var scale = Vec3D(0.0)
    var position = Vec3D(0.0)
    var rotation = QRot(1.0, 0.0, 0.0, 0.0)

    override fun enable() {

    }

    override fun destroy() {
    }
}