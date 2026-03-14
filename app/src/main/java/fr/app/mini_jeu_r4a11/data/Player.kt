package fr.app.mini_jeu_r4a11.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player (
    var id: String = "",
    var pseudo: String = "",
    var games: MutableMap<String, MutableList<String>> = mutableMapOf()
) : Parcelable