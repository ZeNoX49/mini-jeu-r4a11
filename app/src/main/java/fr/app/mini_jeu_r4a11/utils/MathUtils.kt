package fr.app.mini_jeu_r4a11.utils

class MathUtils {
    fun dot(v1: Vec3, v2: Vec3): Float {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
    }

    /**
     * produit linéaire
     */
    fun crossProd(v1: Vec3, v2: Vec3): Vec3 {
        return Vec3(
            v1.y * v2.z - v1.z * v2.y,
            v1.z * v2.x - v1.x * v2.z,
            v1.x * v2.y - v1.y * v2.x
        )
    }
}