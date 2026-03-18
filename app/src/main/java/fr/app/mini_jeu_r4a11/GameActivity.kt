package fr.app.mini_jeu_r4a11

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.core.graphics.toColorInt
import fr.app.mini_jeu_r4a11.__temp__.MyNoise
import fr.app.mini_jeu_r4a11.data.Game
import fr.app.mini_jeu_r4a11.data.Player
import fr.app.mini_jeu_r4a11.utils.SquareColor
import fr.app.mini_jeu_r4a11.utils.Vec2
import fr.app.mini_jeu_r4a11.utils.Vec3
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Region
import android.graphics.Path
import android.graphics.Paint

class GameActivity : AppCompatActivity() {
    private lateinit var player: Player
    private lateinit var game: Game

    private val noise = MyNoise()

    private val colors: List<SquareColor> = listOf(
        SquareColor(Color.rgb(40,  255, 255), Color.rgb(30,  176, 251)),
        SquareColor(Color.rgb(255, 246, 193), Color.rgb(215, 192, 158)),
        SquareColor(Color.rgb(118, 239, 124), Color.rgb(2,   166, 155)),
        SquareColor(Color.rgb(10,  145, 113), Color.rgb(22,  181, 141))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = intent.getParcelableExtra<Player>("player")!!
        game = intent.getParcelableExtra<Game>("game")!!

        val squareData = createNoise() // List<Int> length should match faces
        setContentView(ModelView(this, colors, squareData))
    }

    private fun createNoise(): List<Int> {
        val width = 33
        val height = 33

        val id = game.id
        val gradient = noise.generateMulti(
            width,
            height,
            (id[1].code + id[5].code + id[9].code) % id[13].code,
            mutableListOf(3, 3, 3, 3)
        )

        val data = ArrayList<Int>(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                data.add(gradient[y][x])
            }
        }
        return data
    }

    /**
     * Vue qui contient toute la logique de rendu / picking / animation.
     * (Remplace ModelManager)
     */
    private class ModelView(
        context: Context,
        colors: List<SquareColor>,
        squareDataParam: List<Int>
    ) : View(context) {

        // Paints
        private val bgPaint = Paint().apply {
            style = Paint.Style.FILL
            color = "#101010".toColorInt()
        }
        private val facePaints: MutableList<Paint> = mutableListOf()
        private val linePaints: MutableList<Paint> = mutableListOf()

        // configuration
        private val camera = fr.app.mini_jeu_r4a11.utils.Camera()
        private val FPS = 60
        private val path = Path()
        private val region = Region()
        private val clip = Region() // set later with real bounds

        private val squareData: List<Int> = squareDataParam

        // click / touch
        private var clickX = -1f
        private var clickY = -1f

        // animation (tap -> center)
        private var isAnimating = false
        private var animStartTime: Long = 0L
        private var animDuration: Long = 0L
        private var animFrom = Vec3(0f, 0f, 1.0f)
        private var animTo = Vec3(0f, 0f, 1.0f)

        // geometry
        private val vertices: MutableList<Vec3> = mutableListOf()
        private val faces: MutableList<IntArray> = mutableListOf()

        init {
            // create paints from colors (SquareColor stores ARGB Ints)
            for (c in colors) {
                facePaints.add(Paint().apply {
                    style = Paint.Style.FILL
                    color = c.faceColor
                })
                linePaints.add(Paint().apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                    isAntiAlias = true
                    color = c.verticesColor
                })
            }

            // grid geometry: 33x33 quads centered around (0,0) at z=5
            var idx = 0
            for (y in -16..16) {
                for (x in -16..16) {
                    vertices.add(Vec3(x - 0.5f, y - 0.5f, 5f))
                    vertices.add(Vec3(x - 0.5f, y + 0.5f, 5f))
                    vertices.add(Vec3(x + 0.5f, y + 0.5f, 5f))
                    vertices.add(Vec3(x + 0.5f, y - 0.5f, 5f))
                    faces.add(intArrayOf(idx++, idx++, idx++, idx++))
                }
            }

            // sanity check: squareData should match faces
            if (squareData.size != faces.size) {
                // if mismatch, fallback: repeat first color index
                // (you can also throw an exception)
                android.util.Log.w(
                    "ModelView",
                    "squareData size (${squareData.size}) != faces size (${faces.size}), using modulo indexing"
                )
            }

            // initialize clip rect for Region (will be updated in onSizeChanged)
            clip.set(0, 0, width, height)
        }

        private fun project(v: Vec3): Vec2 {
            val eps = 1e-6f
            val z = max(v.z, eps)
            return Vec2(v.x / z, v.y / z)
        }

        private fun screen(p: Vec2): Vec2 {
            val s = min(width, height) - 50.0f
            val px = width / 2.0f + ((p.x + 1f) / 2f - 0.5f) * s
            val py = height / 2.0f + ((1f - (p.y + 1f) / 2f) - 0.5f) * s
            return Vec2(px, py)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            clip.set(0, 0, w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            updateAnimation()
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            var idx = 0
            for (f in faces) {
                // project corners
                val p0 = screen(project(vertices[f[0]] + camera.target))
                val p1 = screen(project(vertices[f[1]] + camera.target))
                val p2 = screen(project(vertices[f[2]] + camera.target))
                val p3 = screen(project(vertices[f[3]] + camera.target))

                // build path
                path.reset()
                path.moveTo(p0.x, p0.y)
                path.lineTo(p1.x, p1.y)
                path.lineTo(p2.x, p2.y)
                path.lineTo(p3.x, p3.y)
                path.close()

                // set region (reused object)
                region.setPath(path, clip)

                // picking (tap)
                if (clickX >= 0f && clickY >= 0f && region.contains(clickX.toInt(), clickY.toInt())) {
                    val centerWorld = Vec3(
                        (vertices[f[0]].x + vertices[f[2]].x) / 2f,
                        (vertices[f[0]].y + vertices[f[2]].y) / 2f,
                        1.0f
                    )
                    val target = Vec3(-centerWorld.x, -centerWorld.y, 1.0f)
                    startAnimationTo(target)
                    clickX = -1f
                    clickY = -1f
                }

                // pick color index safely (modulo fallback)
                val raw = if (idx < squareData.size) squareData[idx] else 0
                val colorIndex = if (facePaints.isNotEmpty()) (raw % facePaints.size).coerceAtLeast(0) else 0

                // draw face + edges
                canvas.drawPath(path, facePaints[colorIndex])
                for (i in f.indices) {
                    val a = vertices[f[i]] + camera.target
                    val b = vertices[f[(i + 1) % f.size]] + camera.target
                    val pa = screen(project(a))
                    val pb = screen(project(b))
                    canvas.drawLine(pa.x, pa.y, pb.x, pb.y, linePaints[colorIndex])
                }

                idx++
            }

            // center marker (optional)
            val v0 = screen(project(Vec3(-0.1f,  0.1f, 5.0f)))
            val v1 = screen(project(Vec3( 0.1f,  0.1f, 5.0f)))
            val v2 = screen(project(Vec3( 0.1f, -0.1f, 5.0f)))
            val v3 = screen(project(Vec3(-0.1f, -0.1f, 5.0f)))
            path.reset()
            path.moveTo(v0.x, v0.y)
            path.lineTo(v1.x, v1.y)
            path.lineTo(v2.x, v2.y)
            path.lineTo(v3.x, v3.y)
            path.close()
            canvas.drawPath(path, bgPaint)
        }

        // animation helpers
        private fun startAnimationTo(target: Vec3) {
            animFrom = camera.target
            animTo = target
            animStartTime = SystemClock.uptimeMillis()
            isAnimating = true

            val distance = distanceBetween(animFrom, animTo)
            val speed = 2.0f
            val durationMs = ((distance / speed) * 1000f).toLong()
            animDuration = durationMs
        }

        private fun updateAnimation() {
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

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            removeCallbacks(frameRunnable)
            post(frameRunnable)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            removeCallbacks(frameRunnable)
        }

        private val frameRunnable = object : Runnable {
            override fun run() {
                updateAnimation()
                invalidate()
                postDelayed(this, (1000L / FPS))
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                clickX = event.x
                clickY = event.y
                invalidate()
                return true
            }
            return super.onTouchEvent(event)
        }
    }
}