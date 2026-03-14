package fr.app.mini_jeu_r4a11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import fr.app.mini_jeu_r4a11.data.Game
import fr.app.mini_jeu_r4a11.data.Player
import fr.app.mini_jeu_r4a11.launcher.LauncherCreateName
import fr.app.mini_jeu_r4a11.launcher.LauncherPseudo

const val PREFS_NAME = "PlayerData"

class LauncherActivity : AppCompatActivity() {
    private lateinit var player: Player

    private lateinit var btnPseudo: Button
    private lateinit var btnPlay: Button
    private lateinit var btnCreate: Button

    // permet de récupérer le pseudo depuis LauncherPseudo et le mettre a jour dans la bdd
    private val pseudoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val newPseudo = result.data?.getStringExtra("pseudo") ?: return@registerForActivityResult   // remplace continue
            modifyPseudo(newPseudo)
        }
    }

    private val launcherPlay = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val game = result.data?.getParcelableExtra<Game>("game") ?: return@registerForActivityResult   // remplace continue
            // launchGame(game)
        }
    }

    private val launcherCreateName = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val game = result.data?.getParcelableExtra<Game>("game") ?: return@registerForActivityResult   // remplace continue
            // launchGame(game)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        // pour prendre en compte la partie haute et basse du tel
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        connectPlayer()

        btnPseudo = findViewById(R.id.btnPseudo)
        btnPlay = findViewById(R.id.btnPlay)
        btnCreate = findViewById(R.id.btnCreate)

        btnPseudo.setOnClickListener {
            intent = Intent(this, LauncherPseudo::class.java)
            intent.putExtra("pseudo", player.pseudo)
            pseudoLauncher.launch(intent)
        }

        btnPlay.setOnClickListener {
            intent = Intent(this, LauncherPlay::class.java)
            intent.putExtra("player", player)
            launcherPlay.launch(intent)
        }

        btnCreate.setOnClickListener {
            intent = Intent(this, LauncherCreateName::class.java)
            intent.putExtra("player", player)
            launcherCreateName.launch(intent)
        }
    }

    /**
     * Permet de récupérer les données du joueurs
     * ou le créer si il n'a pas de compte
     */
    private fun connectPlayer() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val playerId = prefs.getString("player_id", null)
        val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference

        if (playerId == null) {
            // le joueur n'existe pas -> création d'un compte
            createPlayer(db)
        } else {
            db.child("players").child(playerId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        player = snapshot.getValue(Player::class.java)!!
                        btnPseudo.text = player.pseudo
                    } else {
                        // Le joueur n'existe plus en bdd
                        createPlayer(db)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Database", "Impossible de lire les données du joueur", e)
                }
        }
    }

    private fun createPlayer(db: DatabaseReference) {
        val newPlayerRef = db.child("players").push()
        val id = newPlayerRef.key!!

        val games: MutableMap<String, MutableList<String>> = mutableMapOf()
        games["created"] = mutableListOf()
        games["joined"] = mutableListOf()
        player = Player(id, "", games)

        intent = Intent(this, LauncherPseudo::class.java)
        intent.putExtra("pseudo", player.pseudo)
        pseudoLauncher.launch(intent)
    }

    private fun modifyPseudo(newPseudo: String) {
        val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference

        player.pseudo = newPseudo
        db.child("players").child(player.id).setValue(player)
            .addOnSuccessListener {
                btnPseudo.text = player.pseudo

                val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                prefs.edit { putString("player_id", player.id) }
            }
            .addOnFailureListener { e ->
                Log.e("Dababase", "Erreur lors de l'ecriture dans la bdd", e)
            }
    }
}