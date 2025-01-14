package ru.cherryngine.lib.math

import ru.cherryngine.lib.math.rotation.QRot

data class Transform(
    val translation: Vec3D = Vec3D.ZERO,
    val rotation: QRot = QRot.IDENTITY,
    val scale: Vec3D = Vec3D.ONE,
) {
    companion object {
        val ZERO = Transform()
    }

    val t get() = translation
    val r get() = rotation
    val s get() = scale

    operator fun times(offset: Transform): Transform {
        val translation = translation + (offset.translation * scale).rotate(rotation)
        val rotation = rotation * offset.rotation
        val scale = scale * offset.scale
        return Transform(translation, rotation, scale)
    }

    operator fun div(offset: Transform): Transform {
        val scale = scale / offset.scale
        val rotation = rotation / offset.rotation
        val translation = translation - (offset.translation * scale).rotate(rotation)
        return Transform(translation, rotation, scale)
    }
}
