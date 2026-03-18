package fr.app.mini_jeu_r4a11.utils

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Camera (
    var target: Vec3 = Vec3(0.0f, 0.0f, 1.0f),
    // var distance: Float = 25.0f,
    // var pitch: Float = 0.5f,
    // var yaw: Float = 0.0f,
    // var focalLenth: Float = 45.0f
) {
    // fun getPosition(): Vec3 {
    //     val x: Float = target.x + distance * cos(pitch) * sin(yaw)
    //     val y: Float = target.y + distance * sin(pitch)
    //     val z: Float = target.z + distance * cos(pitch) * cos(yaw)
    //     return Vec3(x, y, z);
    // }

    // fun getLookAtDirection(): Vec3 {
    //     return normalize(target - getPosition())
    // }

    // fun getForwardDirection(): Vec3 {
    //     val position = getPosition()
    //     return normalize(Vec3(
    //         target.x - position.x,
    //         0.0f,
    //         target.z - position.z
    //     ))
    // }

    // fun getRightDirection(): Vec3 {
    //     val forward = getForwardDirection()
    //     return normalize(cross(
    //         forward,
    //         Vec3(0.0f, 1.0f, 0.0f)
    //     ))
    // }

    // glm::mat4 getViewMatrix();

    // private fun normalize(v: Vec3): Vec3 {
    //     val length = sqrt(v.x * v.x + v.y * v.y + v.z * v.z)
    //     return Vec3(
    //         v.x / length,
    //         v.y / length,
    //         v.z / length
    //     )
    // }

    // private fun cross(v1: Vec3, v2: Vec3): Vec3 {
    //     return Vec3(
    //         v1.y * v2.z - v1.z * v2.y,
    //         v1.z * v2.x - v1.x * v2.z,
    //         v1.x * v2.y - v1.y * v2.x
    //     )
    // }
}