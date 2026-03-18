package fr.app.mini_jeu_r4a11.utils

data class Vec3(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f
) {
    operator fun plus(other: Vec3) = Vec3(
        x + other.x,
        y + other.y,
        z + other.z
    )

    operator fun minus(other: Vec3) = Vec3(
        x - other.x,
        y - other.y,
        z - other.z
    )

    operator fun times(scalar: Float) = Vec3(
        x * scalar,
        y * scalar,
        z * scalar
    )

    operator fun div(scalar: Float) = Vec3(
        x / scalar,
        y / scalar,
        z / scalar
    )
}