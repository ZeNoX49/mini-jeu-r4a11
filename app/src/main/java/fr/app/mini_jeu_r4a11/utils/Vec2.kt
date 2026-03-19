package fr.app.mini_jeu_r4a11.utils

import kotlin.math.min

data class Vec2(
    var x: Float = 0.0f,
    var y: Float = 0.0f
) {
    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vec2(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vec2(x / scalar, y / scalar)

    fun toScreen(W: Int, H: Int, zoom: Float): Vec2 {
        val half = minOf(W, H) / 2f
        return Vec2(
            W / 2f + this.x * zoom * half,
            H / 2f - this.y * zoom * half
        )
    }
}