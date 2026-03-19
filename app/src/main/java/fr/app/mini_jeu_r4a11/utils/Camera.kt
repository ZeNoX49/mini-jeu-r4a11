package fr.app.mini_jeu_r4a11.utils

import kotlin.math.cos
import kotlin.math.sin

class Camera (
    var target: Vec3 = Vec3(0.0f, 0.0f, 0.0f),
    var distance: Float = 25.0f,
    var pitch: Float = 0.5f,
    var yaw: Float = 0.0f,
    var focalLength: Float = 0.225f
) {
    fun getLookAtDirection(): Vec3 {
        return Vec3(
            -sin(this.yaw) * cos(this.pitch),
            sin(this.pitch),
            cos(this.yaw) * cos(this.pitch)
        )
    }

    fun getForwardDirection(): Vec3 {
        return Vec3(-sin(this.yaw), 0.0f, cos(this.yaw))
    }

    fun getRightDirection(): Vec3 {
        return Vec3(cos(this.yaw), 0.0f, sin(this.yaw))
    }
}