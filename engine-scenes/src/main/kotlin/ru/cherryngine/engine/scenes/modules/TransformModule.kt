package ru.cherryngine.engine.scenes.modules

import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.rotation.QRot

@Prototype
class TransformModule(
    @Parameter override val gameObject: GameObject,
) : Module {
    var translation = Vec3D.ZERO
    var rotation = QRot.IDENTITY
    var scale = Vec3D.ONE

    var transform: Transform
        get() = Transform(translation, rotation, scale)
        set(value) {
            translation = value.translation
            rotation = value.rotation
            scale = value.scale
        }
}