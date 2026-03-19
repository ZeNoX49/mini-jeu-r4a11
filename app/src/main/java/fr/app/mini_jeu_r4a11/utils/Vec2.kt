package fr.app.mini_jeu_r4a11.utils

data class Vec2(
    var x: Float = 0.0f,
    var y: Float = 0.0f
) {
    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vec2(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vec2(x / scalar, y / scalar)

    fun toScreen(W: Int, H: Int): Vec2 {
        val aspect = W.toFloat() / H.toFloat()
        return Vec2(
            (this.x / aspect + 1f) * W / 2,
            (-this.y + 1) * H / 2
        )
    }
}