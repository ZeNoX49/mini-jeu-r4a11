package fr.app.mini_jeu_r4a11.utils

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

data class Vec3(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f
) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float) = Vec3(x / scalar, y / scalar, z / scalar)

    fun projection(focalLength: Float): Vec2 {
        val dz = max(this.z, 1e-9f)
        return Vec2(focalLength * this.x / dz, focalLength * this.y / dz)
    }

    fun rotationX(pitch: Float): Vec3 {
        val y1 = cos(pitch) * this.y - sin(pitch) * this.z
        val z1 = sin(pitch) * this.y + cos(pitch) * this.z
        return Vec3(this.x, y1, z1)
    }

    fun rotationY(yaw: Float): Vec3 {
        val x1 = cos(yaw) * this.x + sin(yaw) * this.z
        val z1 = -sin(yaw) * this.x + cos(yaw) * this.z
        return Vec3(x1, this.y, z1)
    }

    fun length(): Float {
        return Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }

    fun normalize(): Vec3 {
        val n = this.length()
        if (n == 0.0f) {
            return Vec3()
        }
        return Vec3(this.x / n, this.y / n, this.z / n)
    }
}