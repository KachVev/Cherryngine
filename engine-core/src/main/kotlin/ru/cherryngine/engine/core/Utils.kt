package ru.cherryngine.engine.core

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.registry.DynamicRegistry
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.View
import ru.cherryngine.lib.math.rotation.AxisSequence
import ru.cherryngine.lib.math.rotation.QRot

fun <T : Any> DynamicRegistry<T>.getId(value: T): Int? = getKey(value)?.let { getId(it) }

fun Vec3D.minestomVec() = Vec(x, y, z)
fun Vec3D.minestomPos() = Pos(x, y, z)
fun Vec3D.minestomPos(yaw: Float, pitch: Float) = Pos(x, y, z, yaw, pitch)
fun Vec3D.minestomPos(view: View) = Pos(x, y, z, view.yaw, view.pitch)

fun View.minestomPos(pos: Vec3D) = Pos(pos.x, pos.y, pos.z, yaw, pitch)

fun Point.asVec3D() = Vec3D(x(), y(), z())

fun Pos.asView() = View(yaw, pitch)
fun Pos.asQRot() = asView().getRotation()