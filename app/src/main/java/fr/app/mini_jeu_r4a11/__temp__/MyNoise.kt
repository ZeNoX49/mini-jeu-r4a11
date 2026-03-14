package fr.app.mini_jeu_r4a11.__temp__

import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class MyNoise {

    private data class Base(
        val x: Int,
        val y: Int,
        val value: Int
    )

    private fun areWeightsOk(width: Int, height: Int, weights: MutableList<Int>): Boolean {
        // clamp in-place et sum
        var totalBases = 0
        for (i in weights.indices) {
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

    /**
     * Place de façon déterministe `sum(weights)` bases dans la grille [width x height].
     * Utilise un IntArray d'indices et un Fisher-Yates partiel pour éviter d'allouer des Pair/objets.
     */
    private fun placeBases(width: Int, height: Int, seed: Int, weights: List<Int>): MutableList<Base> {
        val totalBases = weights.sum()
        val size = width * height
        val indices = IntArray(size) { it }

        // RNG déterministe si seed > 0
        val rng = if (seed > 0) Random(seed) else Random.Default

        // Fisher-Yates partiel : on n'a besoin que des `totalBases` premières positions
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
            // retourner une grille vide correctement typée
            return posValues
        }

        // placer les bases
        val bases = placeBases(width, height, seed, weights)

        val rng = Random(seed)

        for (y in 0 until height) {
            for (x in 0 until width) {
                var maxDist = 0f
                var maxValue = -1

                for (b in bases) {
                    val dx = (x - b.x).toFloat()
                    val dy = (y - b.y).toFloat()
                    val dsq = dx * dx + dy * dy

                    if (dsq > maxDist) {
                        maxDist = dsq
                        maxValue = b.value
                    } else if (dsq == maxDist) {
                        if (rng.nextBoolean()) {
                            maxValue = b.value
                        }
                    }
                }

                posValues[y][x] = maxValue
            }
        }

        return posValues
    }

    /**
     * Permet de générer un multi-value gradient
     * @param seed si > 0, rend la génération reproductible
     * @param weights nombre de bases à placer pour les valeurs
     * @param percent 0..100 -> contrôle la "netteté"
     * @return une grille height x width avec les valeurs pour le gradient
     */
    fun generateMultiGradient(width: Int, height: Int, seed: Int, weights: MutableList<Int>, percent: Int): List<List<FloatArray>> {
        val pClamped = percent.coerceIn(0, 100)
        val power = 1.0f + (pClamped / 100f) * 4.0f  // 1..5
        val nbValues = weights.size

        if (!areWeightsOk(width, height, weights)) {
            // retourner une grille vide correctement typée
            return List(height) { List(width) { FloatArray(nbValues) { 0f } } }
        }

        // placer les bases
        val bases = placeBases(width, height, seed, weights)
        val eps = 1e-9f

        // stockage interne en tableaux primitifs 2D
        val posValuesArr = Array(height) { Array(width) { FloatArray(nbValues) { 0f } } }

        // boucle principale
        for (y in 0 until height) {
            for (x in 0 until width) {
                val out = posValuesArr[y][x]
                // réinitialiser le buffer de sortie (évite une allocation)
                out.fill(0f)

                var onBase = false
                var baseValue = -1

                // accumuler directement dans `out`
                for (b in bases) {
                    val dx = (x - b.x).toFloat()
                    val dy = (y - b.y).toFloat()
                    val dsq = dx * dx + dy * dy

                    if (dsq <= 0f) {
                        onBase = true
                        baseValue = b.value
                        break
                    }

                    // w = (1 / sqrt(dsq + eps))^power
                    val inv = 1f / sqrt(dsq + eps)
                    val wgt = inv.toDouble().pow(power.toDouble()).toFloat()
                    out[b.value] += wgt
                }

                if (onBase) {
                    // pixel exactement sur une base
                    // remplissage optimisé : fill(0f) puis valeur à 1
                    out.fill(0f)
                    out[baseValue] = 1f
                } else {
                    // normalisation
                    var total = 0f
                    for (k in 0 until nbValues) total += out[k]
                    if (total > 0f) {
                        val invTotal = 1f / total
                        for (k in 0 until nbValues) out[k] = out[k] * invTotal
                    } else {
                        // cas limite (toutes les contributions = 0), répartir uniformément
                        val v = 1f / nbValues
                        for (k in 0 until nbValues) out[k] = v
                    }
                }
            }
        }

        // convertir en List<List<FloatArray>>
        return posValuesArr.map { row -> row.toList() }
    }
}