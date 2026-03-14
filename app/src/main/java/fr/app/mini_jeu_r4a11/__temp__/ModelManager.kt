package fr.app.mini_jeu_r4a11.__temp__

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import androidx.core.graphics.toColorInt

class ModelManager constructor(context: Context) : View(context) {
    // private var lateinit width: Int;
    // private var lateinit height: Int;

    // Couleurs (comme dans ton JS)
    private val BACKGROUND = "#101010".toColorInt()
    private val FOREGROUND = "#50FF50".toColorInt()

    // Paints
    private val bgPaint = Paint().apply { style = Paint.Style.FILL; color = BACKGROUND }
    private val linePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
        color = FOREGROUND
    }
    // private val pointPaint = Paint().apply {
    //     style = Paint.Style.FILL
    //     isAntiAlias = true
    //     color = FOREGROUND
    // }

    // Configuration
    private val FPS = 60
    private var angle = 0.0
    private var dz: Float = 1.0f

    private val vertices = listOf(
        // cubes
        Vec3( 0.25f,  0.125f,  0.25f),
        Vec3(-0.25f,  0.125f,  0.25f),
        Vec3(-0.25f, -0.125f,  0.25f),
        Vec3( 0.25f, -0.125f,  0.25f),
        Vec3( 0.25f,  0.125f, -0.25f),
        Vec3(-0.25f,  0.125f, -0.25f),
        Vec3(-0.25f, -0.125f, -0.25f),
        Vec3( 0.25f, -0.125f, -0.25f),

        // // face interieur
        // Vec3( 0.25f, 0.0f,   0.0f),
        // Vec3(-0.25f, 0.0f,   0.0f),
        // Vec3( 0.0f,  0.125f, 0.0f),
        // Vec3( 0.0f, -0.125f, 0.0f),
        // Vec3( 0.0f,  0.0f,   0.25f),
        // Vec3( 0.0f,  0.0f,  -0.25f),
    )

    private val faces = listOf(
        // cube
        intArrayOf(0, 1, 2, 3),
        intArrayOf(4, 5, 6, 7),
        intArrayOf(0, 4),
        intArrayOf(1, 5),
        intArrayOf(2, 6),
        intArrayOf(3, 7),

        // // face interieur
        // intArrayOf(8, 10, 12),
        // intArrayOf(9, 11, 13),
        // intArrayOf(8, 11),
        // intArrayOf(9, 10),
        // intArrayOf(8, 10, 13),
        // intArrayOf(9, 11, 12),
    )

    // taille virtuelle (le View peut être de n'importe quelle taille — on s'adapte)
    private var width = 800
    private var height = 800

    // boucle de rendu
    private val frameRunnable = object : Runnable {
        override fun run() {
            val dt = 1.0 / FPS
            angle += Math.PI * dt
            dz = ((dz + 1 * dt) % 5).toFloat()
            invalidate()
            postDelayed(this, (1000L / FPS))
        }
    }

    // ----------------- Fonctions math / transformations -----------------

    private data class Vec3(val x: Float, val y: Float, val z: Float)
    private data class Vec2(val x: Float, val y: Float)

    /**
     * permet de déplacer
     */
    private fun translate(v: Vec3, move: Vec3): Vec3 {
        return Vec3(
            (v.x + move.x).toFloat(),
            (v.y + move.y).toFloat(),
            (v.z + move.z).toFloat()
        )
    }

    private fun rotateXZ(v: Vec3, angle: Double): Vec3 {
        val c = cos(angle)
        val s = sin(angle)
        val x = v.x * c - v.z * s
        val z = v.x * s + v.z * c
        return Vec3(x.toFloat(), v.y, z.toFloat())
    }

    private fun project(v: Vec3): Vec2 {
        // Evite division par zéro — si z trop petit -> clamp à epsilon
        val eps = 1e-4f
        val z = max(v.z, eps)
        return Vec2(v.x / z, v.y / z)
    }

    private fun screen(p: Vec2): Vec2 {
        // mapping -1..1 -> 0..width and invert y comme ton JS
        val sx = (p.x + 1f) / 2f * width
        val sy = (1f - (p.y + 1f) / 2f) * height
        return Vec2(sx, sy)
    }

    // ----------------- View lifecycle -----------------

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // démarrer la boucle
        removeCallbacks(frameRunnable)
        post(frameRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(frameRunnable)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
        height = h
    }

    // ----------------- Dessin -----------------

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // fond
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        for (f in faces) {
            for (i in f.indices) {
                val a = vertices[f[i]]
                val b = vertices[f[(i + 1) % f.size]]
                val pa = screen(project(translate(rotateXZ(a, angle), Vec3(0f, 0f, dz))))
                val pb = screen(project(translate(rotateXZ(b, angle), Vec3(0f, 0f, dz))))
                canvas.drawLine(pa.x, pa.y, pb.x, pb.y, linePaint)

                // pour pas retracer la même ligne dans l'autre sens
                if(f.size == 2) break;
            }
        }

        // val s = 20f
        // for (v in vertices) {
        //     val p = screen(project(translateZ(rotateXZ(v, angle), dz)))
        //     canvas.drawRect(p.x - s / 2, p.y - s / 2, p.x + s / 2, p.y + s / 2, pointPaint)
        // }
    }
}