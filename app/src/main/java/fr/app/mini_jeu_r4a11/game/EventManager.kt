package fr.app.mini_jeu_r4a11.game

import android.os.SystemClock
import fr.app.mini_jeu_r4a11.utils.Camera
import fr.app.mini_jeu_r4a11.utils.Vec2
import kotlin.math.sqrt

class EventManager(cam: Camera) {
    private val camera = cam

    // animation (tap -> center)
    private var isAnimating = false
    private var animStartTime: Long = 0L
    private var animDuration: Long = 0L
    private var animFrom = Vec2(0f, 0f)
    private var animTo = Vec2(0f, 0f)

    // animation helpers
    fun startAnimationTo(target: Vec2) {
        animFrom = camera.target
        animTo = target
        animStartTime = SystemClock.uptimeMillis()
        isAnimating = true

        val distance = distanceBetween(animFrom, animTo)
        val speed = 2.0f
        val durationMs = ((distance / speed) * 1000f).toLong()
        animDuration = durationMs
    }

    fun updateAnimation() {
        if (!isAnimating) return
        if (animDuration <= 0L) {
            camera.target = animTo
            isAnimating = false
            return
        }
        val now = SystemClock.uptimeMillis()
        val tRaw = (now - animStartTime).toFloat() / animDuration.toFloat()
        val t = when {
            tRaw <= 0f -> 0f
            tRaw >= 1f -> 1f
            else -> tRaw
        }
        val eased = t * t * (3f - 2f * t)
        camera.target = lerpVec2(animFrom, animTo, eased)
        if (t >= 1f) {
            isAnimating = false
            camera.target = animTo
        }
    }

    private fun lerpVec2(a: Vec2, b: Vec2, t: Float): Vec2 =
        Vec2(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

    private fun distanceBetween(a: Vec2, b: Vec2): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx*dx + dy*dy )
    }
}