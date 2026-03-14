package fr.app.mini_jeu_r4a11

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import fr.app.mini_jeu_r4a11.data.Game
import fr.app.mini_jeu_r4a11.data.Player

class LauncherPlay : AppCompatActivity() {
    private lateinit var svllCreated: LinearLayout
    private lateinit var svllJoined: LinearLayout
    private lateinit var btnReturn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_play)

        // pour prendre en compte la partie haute et basse du tel
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        svllCreated = findViewById(R.id.svllCreated)
        svllJoined = findViewById(R.id.svllJoined)
        btnReturn = findViewById(R.id.btnReturn)

        btnReturn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        val player = intent.getParcelableExtra<Player>("player")
        if (player == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return // sinon faut mettre "player?" après
        }

        val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference

        // parties créées
        player.games["created"]?.forEach { id ->
            db.child("games").child(id).get()
                .addOnSuccessListener { snapshot ->
                    val game = snapshot.getValue(Game::class.java) ?: return@addOnSuccessListener   // remplace continue

                    val view = layoutInflater.inflate(R.layout.game, svllCreated, false)
                    view.findViewById<TextView>(R.id.tvName).text = game.name
                    view.findViewById<TextView>(R.id.tvCreator).visibility = View.GONE
                    svllCreated.addView(view)

                    view.setOnClickListener {
                        val data = Intent().apply { putExtra("game", game) }
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    }
                }
        }

        // parties rejoint
        player.games["joined"]?.forEach { id ->
            db.child("games").child(id).get()
                .addOnSuccessListener { snapshot ->
                    val game = snapshot.getValue(Game::class.java) ?: return@addOnSuccessListener   // remplace continue

                    val view = layoutInflater.inflate(R.layout.game, svllCreated, false)
                    view.findViewById<TextView>(R.id.tvName).text = game.name
                    view.findViewById<TextView>(R.id.tvCreator).text = game.creator
                    svllCreated.addView(view)

                    view.setOnClickListener {
                        val data = Intent().apply { putExtra("game", game) }
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    }
                }
        }
    }
}