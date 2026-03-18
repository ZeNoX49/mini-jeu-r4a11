package fr.app.mini_jeu_r4a11

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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

    private val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference

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
        svllJoined = findViewById(R.id.svllJoin)
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

        // Parties créées
        player.games["created"]?.forEach { id -> loadGame(id, svllCreated, showCreator = false) }

        // Parties rejointes
        player.games["joined"]?.forEach { id -> loadGame(id, svllJoined, showCreator = true) }
    }

    private fun loadGame(id: String, container: LinearLayout, showCreator: Boolean) {
        db.child("games").child(id).get()
            .addOnSuccessListener { snapshot ->
                val game = snapshot.getValue(Game::class.java) ?: return@addOnSuccessListener

                val view = layoutInflater.inflate(R.layout.game, container, false)
                view.findViewById<TextView>(R.id.tvName).text = game.name
                view.findViewById<TextView>(R.id.tvCreator).apply {
                    if (showCreator) {
                        db.child("players").child(game.creator).get()
                            .addOnSuccessListener { playerSnap ->
                                val pseudo = playerSnap.child("pseudo").getValue(String::class.java) ?: game.creator
                                text = pseudo
                            }
                    } else {
                        visibility = View.GONE
                    }
                }
                container.addView(view)

                view.setOnClickListener {
                    setResult(Activity.RESULT_OK, Intent().apply { putExtra("game", game) })
                    finish()
                }
            }
    }
}