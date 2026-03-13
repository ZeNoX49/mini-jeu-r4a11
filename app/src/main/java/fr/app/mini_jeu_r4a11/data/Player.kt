package fr.app.mini_jeu_r4a11.data

data class Player (
    var id: String = "",
    var pseudo: String = "",
    var games: MutableMap<String, MutableList<Int>> = mutableMapOf()
)