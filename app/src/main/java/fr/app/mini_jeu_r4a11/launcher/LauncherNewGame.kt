package fr.app.mini_jeu_r4a11.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import fr.app.mini_jeu_r4a11.R
import fr.app.mini_jeu_r4a11.data.Game
import fr.app.mini_jeu_r4a11.data.Player

class LauncherNewGame: AppCompatActivity() {
    private lateinit var btnReturn: Button
    private lateinit var etName: EditText
    private lateinit var tvNgInfo: TextView
    private lateinit var btnNewGame: Button
    private lateinit var svllJoin: LinearLayout

    private lateinit var player: Player
    private val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_newgame)

        // pour prendre en compte la partie haute et basse du tel
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnReturn = findViewById(R.id.btnReturn)
        etName = findViewById(R.id.etName)
        tvNgInfo = findViewById(R.id.tvNgInfo)
        btnNewGame = findViewById(R.id.btnNewGame)
        svllJoin = findViewById(R.id.svllJoin)

        player = intent.getParcelableExtra<Player>("player") ?: run {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        btnReturn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnNewGame.setOnClickListener {
            createGame()
        }

        loadJoinableGames()
    }

    private fun createGame() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            tvNgInfo.text = "Le nom ne peut pas être vide"
            return
        }

        val newGameRef = db.child("games").push()
        val id = newGameRef.key!! + player.id
        val game = Game(player.id, id, name).apply {
            playerInfo[player.id] = ""
        }

        db.child("games").child(id).setValue(game)
            .addOnSuccessListener {
                player.games.getOrPut("created") { mutableListOf() }.add(id)

                db.child("players").child(player.id).setValue(player)
                    .addOnSuccessListener { snapshot ->
                        okAndFinish(game)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Database", "Erreur écriture joueur", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Database", "Erreur écriture partie", e)
            }
    }

    private fun loadJoinableGames() {
        val excluded = (player.games["created"] ?: emptyList()) +
                       (player.games["joined"] ?: emptyList())

        db.child("games").get()
            .addOnSuccessListener { snapshot ->
                for (gameSnap in snapshot.children) {
                    val id = gameSnap.key ?: continue
                    if (id in excluded) continue

                    val game = gameSnap.getValue(Game::class.java) ?: continue

                    val view = layoutInflater.inflate(R.layout.game, svllJoin, false)
                    view.findViewById<TextView>(R.id.tvName).text = game.name
                    svllJoin.addView(view)

                    // Récupérer le pseudo du créateur via son id
                    db.child("players").child(game.creator).get()
                        .addOnSuccessListener { playerSnap ->
                            val pseudo = playerSnap.child("pseudo").getValue(String::class.java) ?: game.creator
                            view.findViewById<TextView>(R.id.tvCreator).text = pseudo
                        }

                    view.setOnClickListener {
                        game.playerInfo[player.id] = ""
                        db.child("games").child(id).setValue(game)

                        player.games.getOrPut("joined") { mutableListOf() }.add(id)
                        db.child("players").child(player.id).setValue(player)

                        okAndFinish(game)
                    }
                }
            }
    }

    private fun okAndFinish(game: Game) {
        setResult(Activity.RESULT_OK, Intent().apply { putExtra("game", game) })
        finish()
    }
}