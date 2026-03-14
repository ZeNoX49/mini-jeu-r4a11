package fr.app.mini_jeu_r4a11.__temp__

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import fr.app.mini_jeu_r4a11.R
import fr.app.mini_jeu_r4a11.data.Player
import kotlin.random.Random

class GameActivity : AppCompatActivity() {
    private lateinit var player: Player

    private val noise = MyNoise()

    // dofus
    // slugterra
    // map plate + systeme de biome par point

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_launcher)
        setContentView(ModelManager(this))

        // pour prendre en compte la partie haute et basse du tel
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // createImage()
    }

    private fun createImage() {
        val colors = arrayOf(
            intArrayOf(255, 0, 0),
            intArrayOf(0, 255, 0),
            intArrayOf(0, 0, 255)
        )

        val width = 10
        val height = 10

        val gradient = noise.generateMulti(
            width,
            height,
            Random.nextInt(1, 1000),
            mutableListOf(
                Random.nextInt(1, 10),
                Random.nextInt(1, 10),
                Random.nextInt(1, 10)
            )
        )

        val bitmap = createBitmap(width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val n = gradient[y][x]
                bitmap[x, y] = Color.rgb(
                    colors[n][0],
                    colors[n][1],
                    colors[n][2]
                )
            }
        }

        // imageView.setImageBitmap(bitmap)
    }
}