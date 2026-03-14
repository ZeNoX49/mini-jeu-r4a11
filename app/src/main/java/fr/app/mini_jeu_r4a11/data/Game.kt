package fr.app.mini_jeu_r4a11.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Game(
    var creator: String = "",
    var id: String = "",
    var name: String = "",
    var playerInfo: MutableMap<String, @RawValue Any> = mutableMapOf()
) : Parcelable
