package ru.cherryngine.engine.scenes.modules

import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.rotation.QRot

@ModulePrototype
class TransformModule(
    @Parameter override val gameObject: GameObject,
) : Module {
    var local: Transform = Transform(Vec3D.ZERO, QRot.IDENTITY, Vec3D.ONE)
}