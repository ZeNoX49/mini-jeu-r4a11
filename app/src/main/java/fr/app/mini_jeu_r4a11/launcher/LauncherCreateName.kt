package fr.app.mini_jeu_r4a11.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import fr.app.mini_jeu_r4a11.R
import fr.app.mini_jeu_r4a11.data.Game
import fr.app.mini_jeu_r4a11.data.Player
import kotlin.collections.contains

/**
 * Activité pour saisir un nom de partie lors de la création,
 */
class LauncherCreateName : AppCompatActivity() {
    private lateinit var etPseudo: EditText
    private lateinit var tvInfo: TextView
    private lateinit var btnReturn: Button
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_pseudo)

        // pour prendre en compte la partie haute et basse du tel
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etPseudo = findViewById(R.id.etPseudo)
        tvInfo = findViewById(R.id.tvInfo)
        btnReturn = findViewById(R.id.btnReturn)
        btnConfirm = findViewById(R.id.btnConfirm)

        etPseudo.hint = "nom de la partie"

        val player = intent.getParcelableExtra<Player>("player")
        if (player == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return // sinon faut mettre "player?" après
        }

        btnReturn.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btnConfirm.setOnClickListener {
            if (etPseudo.text.isEmpty()) {
                tvInfo.text = "Le nom ne peut pas être vide"
                return@setOnClickListener   // remplace continue
            }

            val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference
            val newGameRef = db.child("games").push()
            val id: String = newGameRef.key!!

            val game = Game(player.id, id, etPseudo.text.toString())

            db.child("games").child(id).setValue(game)
                .addOnSuccessListener {
                    if("created" !in player.games) player.games["created"] = mutableListOf<String>()
                    player.games["created"]?.add(id)

                    db.child("players").child(player.id).setValue(player)
                        .addOnSuccessListener { snapshot ->
                            val data = Intent().apply { putExtra("game", game) }
                            setResult(Activity.RESULT_OK, data)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Database", "Impossible de lire les données du joueur", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Dababase", "Erreur lors de l'ecriture dans la bdd", e)
                }
        }
    }
}