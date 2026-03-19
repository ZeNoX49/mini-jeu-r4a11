package fr.app.mini_jeu_r4a11.game

import android.os.SystemClock
import fr.app.mini_jeu_r4a11.utils.Camera
import fr.app.mini_jeu_r4a11.utils.Vec3
import kotlin.math.sqrt

class EventManager constructor(cam: Camera) {
    private val camera = cam

    // animation (tap -> center)
    private var isAnimating = false
    private var animStartTime: Long = 0L
    private var animDuration: Long = 0L
    private var animFrom = Vec3(0f, 0f, 1.0f)
    private var animTo = Vec3(0f, 0f, 1.0f)

    // animation helpers
    fun startAnimationTo(target: Vec3) {
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
        camera.target = lerpVec3(animFrom, animTo, eased)
        if (t >= 1f) {
            isAnimating = false
            camera.target = animTo
        }
    }

    private fun lerpVec3(a: Vec3, b: Vec3, t: Float): Vec3 =
        Vec3(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t)

    private fun distanceBetween(a: Vec3, b: Vec3): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dz = a.z - b.z
        return sqrt(dx*dx + dy*dy + dz*dz)
    }
}