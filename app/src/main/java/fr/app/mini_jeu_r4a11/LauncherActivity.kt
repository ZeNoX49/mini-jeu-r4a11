package fr.app.mini_jeu_r4a11

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit
import com.google.firebase.database.FirebaseDatabase
import fr.app.mini_jeu_r4a11.data.Player

const val PREFS_NAME = "PlayerData"

class LauncherActivity : AppCompatActivity() {
    private lateinit var player: Player

    private lateinit var btnPseudo: Button
    private lateinit var btnPlay: Button
    private lateinit var btnCreate: Button

    // permet de récupérer le pseudo depuis LauncherPseudo et le mettre a jour dans la bdd
    private val pseudoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val newPseudo = result.data?.getStringExtra("pseudo") ?: return@registerForActivityResult
            modifyPseudo(newPseudo)
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

        btnPseudo.setOnClickListener {
            intent = Intent(this, LauncherPseudo::class.java)
            intent.putExtra("pseudo", player.pseudo)
            pseudoLauncher.launch(intent)
        }

        btnPlay.setOnClickListener {
            // TODO
        }

        btnCreate.setOnClickListener {
            // TODO
        }

        connectPlayer()

        btnPseudo.text = player.pseudo
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
            val newPlayerRef = db.push().child("players")
            val id = newPlayerRef.key!!
            player = Player(id, "", mutableMapOf())

            intent = Intent(this, LauncherPseudo::class.java)
            intent.putExtra("pseudo", player.pseudo)
            pseudoLauncher.launch(intent)
        } else {
            db.child("players").child(playerId).get().addOnSuccessListener { snapshot ->
                player = snapshot.getValue(Player::class.java)!!
            }.addOnFailureListener { e ->
                Log.e("Database", "Impossible de lire les données du joueur", e)
            }
        }
    }

    private fun modifyPseudo(newPseudo: String) {
        val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference
        val newPlayerRef = db.push().child("players")

        player.pseudo = newPseudo
        newPlayerRef.setValue(player)
            .addOnFailureListener { e -> Log.e("Dababase", "Erreur lors de l'ecriture dans la bdd", e) }
    }
}

/**
 * Activité pour saisir/modifier un pseudo,
 * le pseudo doit être unique
 */
class LauncherPseudo : AppCompatActivity() {
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

        val pseudo = intent.getStringExtra("pseudo")
        if (pseudo != null && !pseudo.isEmpty()) {
            etPseudo.setText(pseudo)
        }

        btnReturn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnConfirm.setOnClickListener {
            val newPseudo = etPseudo.text.toString().trim()

            if (newPseudo.isEmpty()) {
                tvInfo.text = "Le pseudo ne peut pas être vide"
                return@setOnClickListener
            }

            if(newPseudo == pseudo) {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            // Vérifier si le pseudo existe déjà
            val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference
            db.child("players").orderByChild("pseudo").equalTo(newPseudo).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        tvInfo.text = "Ce pseudo existe déjà"
                    } else {
                        // on renvoie le pseudo
                        val data = Intent().apply { putExtra("pseudo", newPseudo) }
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    tvInfo.text = "Erreur vérification pseudo"
                    Log.e("Database", "Erreur recherche pseudo", e)
                }
        }
    }
}