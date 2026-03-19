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
import kotlin.text.isEmpty

class LauncherPseudo : AppCompatActivity() {
    private lateinit var etPseudo: EditText
    private lateinit var tvInfo: TextView
    private lateinit var btnReturn: Button
    private lateinit var btnConfirm: Button

    private val db = FirebaseDatabase.getInstance("https://mini-jeu-r4a11-default-rtdb.europe-west1.firebasedatabase.app").reference

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

        val pseudo = intent.getStringExtra("pseudo").orEmpty()
        if (pseudo.isNotEmpty()) {
            etPseudo.setText(pseudo)
        }

        btnReturn.setOnClickListener {
            cancelAndFinish()
        }

        btnConfirm.setOnClickListener {
            val newPseudo = etPseudo.text.toString().trim()

            if (newPseudo.isEmpty()) {
                tvInfo.text = "Le pseudo ne peut pas être vide"
                return@setOnClickListener
            }

            if(newPseudo == pseudo) {
                cancelAndFinish()
                return@setOnClickListener
            }

            // Vérifier si le pseudo existe déjà
            db.child("players").orderByChild("pseudo").equalTo(newPseudo).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        tvInfo.text = "Ce pseudo existe déjà"
                    } else {
                        val data = Intent().apply { putExtra("pseudo", newPseudo) }
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Database", "Erreur recherche pseudo", e)
                }
        }
    }

    private fun cancelAndFinish() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}