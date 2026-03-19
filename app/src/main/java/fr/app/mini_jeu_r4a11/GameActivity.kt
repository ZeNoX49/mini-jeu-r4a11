package fr.app.mini_jeu_r4a11

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.core.graphics.toColorInt
import fr.app.mini_jeu_r4a11.game.MyNoise
import fr.app.mini_jeu_r4a11.data.Game
import fr.app.mini_jeu_r4a11.data.Player
import fr.app.mini_jeu_r4a11.utils.SquareColor
import fr.app.mini_jeu_r4a11.utils.Vec2
import fr.app.mini_jeu_r4a11.utils.Vec3
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Region
import android.graphics.Path
import android.graphics.Paint
import android.util.Log
import fr.app.mini_jeu_r4a11.game.EventManager
import fr.app.mini_jeu_r4a11.utils.Camera

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

        val squareData = createNoise()
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

        val metrics = resources.displayMetrics
        val WIDTH = metrics.widthPixels
        val HEIGHT = metrics.heightPixels

        // configuration
        private val camera = Camera()
        private val eventManager = EventManager(camera)
        private val FPS = 60
        private val path = Path()
        private val region = Region()
        private val clip = Region()

        private val squareData: List<Int> = squareDataParam

        // click / touch
        private var clickX = -1f
        private var clickY = -1f

        // geometry
        private val vertices: MutableList<Vec3> = mutableListOf()
        private val faces: MutableList<IntArray> = mutableListOf()

        init {
            // width : 2340 | height : 1080
            // Log.w("SIZE", "width : $WIDTH | height : $HEIGHT")

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

            // if (squareData.size != faces.size) {
            //     android.util.Log.w(
            //         "ModelView",
            //         "squareData size (${squareData.size}) != faces size (${faces.size}), using modulo indexing"
            //     )
            // }

            clip.set(0, 0, width, height)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            clip.set(0, 0, w, h)
        }

        private fun display(v: Vec3): Vec2 {
            return (v - camera.target)
                .rotationY(camera.yaw)
                .rotationX(camera.pitch)
                .projection(camera.focalLength)
                .toScreen(WIDTH, HEIGHT)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            eventManager.updateAnimation()
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint.apply { color = "#101010".toColorInt()})

            var idx = 0
            for (f in faces) {
                // project corners
                val p0 = display(vertices[f[0]])
                val p1 = display(vertices[f[1]])
                val p2 = display(vertices[f[2]])
                val p3 = display(vertices[f[3]])

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
                    eventManager.startAnimationTo(target)
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
                    val pa = a.projection(camera.focalLength).toScreen(WIDTH, HEIGHT)
                    val pb = b.projection(camera.focalLength).toScreen(WIDTH, HEIGHT)
                    canvas.drawLine(pa.x, pa.y, pb.x, pb.y, linePaints[colorIndex])
                }

                idx++
            }

            // marqueur central
            val xy = 0.1f
            val z = 5.0f
            val v0 = Vec3(-xy,  xy, z).projection(camera.focalLength).toScreen(WIDTH, HEIGHT)
            val v1 = Vec3( xy,  xy, z).projection(camera.focalLength).toScreen(WIDTH, HEIGHT)
            val v2 = Vec3( xy, -xy, z).projection(camera.focalLength).toScreen(WIDTH, HEIGHT)
            val v3 = Vec3(-xy, -xy, z).projection(camera.focalLength).toScreen(WIDTH, HEIGHT)
            path.reset()
            path.moveTo(v0.x, v0.y)
            path.lineTo(v1.x, v1.y)
            path.lineTo(v2.x, v2.y)
            path.lineTo(v3.x, v3.y)
            path.close()
            canvas.drawPath(path, bgPaint.apply { color = "#FF0000".toColorInt()})
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