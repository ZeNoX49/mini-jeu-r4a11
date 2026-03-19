package fr.app.mini_jeu_r4a11.game

import android.util.Log
import kotlin.random.Random

class MyNoise {

    private data class Base(
        val x: Int,
        val y: Int,
        val value: Int
    )

    private fun areWeightsOk(width: Int, height: Int, weights: MutableList<Int>): Boolean {
        var totalBases = 0
        for (i in weights.indices) {
            // on s'assure que la valeur est au moins égale a 0
            weights[i] = weights[i].coerceAtLeast(0)
            totalBases += weights[i]
        }

        if (totalBases <= 0) {
            Log.e("MyNoise", "Au moins une base est requise.")
            return false
        }
        if (totalBases > width * height) {
            Log.e("MyNoise", "Trop de bases pour la taille de la grille.")
            return false
        }
        return true
    }

    private fun placeBases(width: Int, height: Int, seed: Int, weights: List<Int>): MutableList<Base> {
        val totalBases = weights.sum()
        val size = width * height
        val indices = IntArray(size) { it }

        val rng = if (seed > 0) Random(seed) else Random.Default

        for (i in 0 until totalBases) {
            // choisir j aléatoire dans [i, size-1]
            val j = rng.nextInt(i, size)
            val tmp = indices[i]
            indices[i] = indices[j]
            indices[j] = tmp
        }

        val posBase = ArrayList<Base>(totalBases)
        var idx = 0
        for (value in weights.indices) {
            repeat(weights[value]) {
                val pos = indices[idx++]
                val x = pos % width
                val y = pos / width
                posBase.add(Base(x, y, value))
            }
        }
        return posBase
    }

    /**
     * Permet de générer un multi-value gradient
     * @param seed si > 0, rend la génération reproductible
     * @param weights nombre de bases à placer pour les valeurs
     * @return une grille height x width avec les valeurs pour le gradient
     */
    fun generateMulti(width: Int, height: Int, seed: Int, weights: MutableList<Int>): List<List<Int>> {
        val posValues = List(height) { MutableList(width) { 0 } }
        if (!areWeightsOk(width, height, weights)) {
            return posValues
        }

        // placer les bases
        val bases = placeBases(width, height, seed, weights)

        // en cas d'égalité sur dsq
        val rng = Random.Default

        for (y in 0 until height) {
            for (x in 0 until width) {
                var minDist = Float.POSITIVE_INFINITY
                var value = -1

                for (b in bases) {
                    val dx = (x - b.x).toFloat()
                    val dy = (y - b.y).toFloat()
                    val dsq = dx * dx + dy * dy

                    if (dsq < minDist) {
                        minDist = dsq
                        value = b.value
                    } else if (dsq == minDist) {
                        if (rng.nextBoolean()) {
                            value = b.value
                        }
                    }
                }

                posValues[y][x] = value
            }
        }

        return posValues
    }
}