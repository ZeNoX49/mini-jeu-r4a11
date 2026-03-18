package fr.app.mini_jeu_r4a11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit
import com.google.firebase.database.FirebaseDatabase
import fr.app.mini_jeu_r4a11.data.Game
import fr.app.mini_jeu_r4a11.data.Player
import fr.app.mini_jeu_r4a11.launcher.LauncherNewGame
import fr.app.mini_jeu_r4a11.launcher.LauncherPseudo

const val PREFS_NAME = "PlayerData"

/**
 * capteurs dans mon téléphone :
 * - Grip Notifier Sensor
 * - LSM6DSOTR Accelerometer
 * - AK09918C Magnetometer
 * - LSM6DSOTR Gyroscope
 * - STK31610 Light
 * - AK09918C Magnetometer Uncalibrated
 * - LSM6DSOTR Gyroscope Uncalibrated
 * - Significant Motion
 * - Step Detector
 * - Step Counter
 * - Tilt Detector
 * - Pick Up Gesture
 * - Device Orientataion
 * - Interrupt Gyroscope
 * - Scontext
 * - STK31610 Light CCT
 * - Call Gesture
 * - Wake Up Motion
 * - STK31610 Auto Brightness Sensor
 * - VDIS Gyroscope
 * - Flip Cover Detector
 * - LSM6DSOTR Accelerometer Uncalibrated
 * - SuperSteady Gyroscope
 * - Device Orientataion Wake Up
 * - SBM
 * - Light Seamless Sensor
 * - Hover Proximity
 * - Touch Pocket
 * - Samsung Hall IC
 * - Motion Sensor
 * - Ear Hover Proximity Sensor (ProToS)
 * - Samsung Game Rotation Vector Sensor
 * - Samsung Gravity Sensor
 * - Samsung Linear Acceleration Sensor
 * - Samsung Rotation Vector Sensor
 * - Samsung Orientation Sensor
 */
class LauncherActivity : AppCompatActivity() {
    private lateinit var player: Player

    private lateinit var btnPseudo: Button
    private lateinit var btnPlay: Button
    private lateinit var btnCreate: Button

    private val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference

    private val pseudoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val newPseudo = result.data?.getStringExtra("pseudo") ?: return@registerForActivityResult
            modifyPseudo(newPseudo)
        }
    }

    private val gameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // maj des données du joueur
            db.child("players").child(player.id).get()
                .addOnSuccessListener { snapshot -> player = snapshot.getValue(Player::class.java)!! }
            val game = result.data?.getParcelableExtra<Game>("game") ?: return@registerForActivityResult
            launchGame(game)
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

        btnPseudo = findViewById(R.id.btnPseudo)
        btnPlay = findViewById(R.id.btnPlay)
        btnCreate = findViewById(R.id.btnCreate)

        connectPlayer()

        btnPseudo.setOnClickListener {
            launch(pseudoLauncher, LauncherPseudo::class.java) { putExtra("pseudo", player.pseudo) }
        }
        btnPlay.setOnClickListener {
            launch(gameLauncher, LauncherPlay::class.java) { putExtra("player", player) }
        }
        btnCreate.setOnClickListener {
            launch(gameLauncher, LauncherNewGame::class.java) { putExtra("player", player) }
        }
    }

    /**
     * Raccourci pour construire et lancer un Intent
     */
    private fun <T> launch(launcher: ActivityResultLauncher<Intent>, cls: Class<T>, intent: Intent.() -> Unit = {}) {
        launcher.launch(Intent(this, cls).apply(intent))
    }

    /**
     * Permet de récupérer les données du joueurs
     * ou le créer si il n'a pas de compte
     */
    private fun connectPlayer() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val playerId = prefs.getString("player_id", null)

        if (playerId == null) {
            createPlayer()
            return
        }

        db.child("players").child(playerId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    player = snapshot.getValue(Player::class.java)!!
                    btnPseudo.text = player.pseudo
                } else {
                    // Le joueur n'existe plus en bdd
                    createPlayer()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Database", "Impossible de lire les données du joueur", e)
            }
    }

    private fun createPlayer() {
        val id = db.child("players").push().key!!
        player = Player(
            id = id,
            pseudo = "",
            games = mutableMapOf("created" to mutableListOf(), "joined" to mutableListOf())
        )
        launch(pseudoLauncher, LauncherPseudo::class.java) { putExtra("pseudo", player.pseudo) }
    }

    private fun modifyPseudo(newPseudo: String) {
        player.pseudo = newPseudo
        db.child("players").child(player.id).setValue(player)
            .addOnSuccessListener {
                btnPseudo.text = player.pseudo
                val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit { putString("player_id", player.id) }
            }
            .addOnFailureListener { e ->
                Log.e("Dababase", "Erreur lors de l'ecriture dans la bdd", e)
            }
    }

    private fun launchGame(game: Game) {
        startActivity(Intent(this, GameActivity::class.java).apply {
            putExtra("player", player)
            putExtra("game", game)
        })
    }
}