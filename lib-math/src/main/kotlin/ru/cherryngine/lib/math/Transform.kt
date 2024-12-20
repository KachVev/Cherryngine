package ru.cherryngine.lib.math

import ru.cherryngine.lib.math.rotation.QRot

data class Transform(
    val translation: Vec3D = Vec3D.ZERO,
    val rotation: QRot = QRot.Companion.IDENTITY,
    val scale: Vec3D = Vec3D.ONE,
) {
    companion object {
        val ZERO = Transform()
    }

    val t get() = translation
    val r get() = rotation
    val s get() = scale

    operator fun times(offset: Transform) = Transform(
        translation + offset.translation.rotate(rotation),
        rotation * offset.rotation,
        scale * offset.scale
    )

    operator fun div(offset: Transform): Transform = Transform(
        (translation - offset.translation).rotate(offset.rotation.inverse()),
        rotation / offset.rotation,
        scale / offset.scale
    )
}
