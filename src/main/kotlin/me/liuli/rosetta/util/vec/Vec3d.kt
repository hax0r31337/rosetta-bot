package me.liuli.rosetta.util.vec

import kotlin.math.pow
import kotlin.math.sqrt

data class Vec3d(var x: Double = .0, var y: Double = .0, var z: Double = .0) {

    fun set(x: Double, y: Double, z: Double): Vec3d {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(vec: Vec3d): Vec3d {
        this.x = vec.x
        this.y = vec.y
        this.z = vec.z
        return this
    }

    fun distanceTo(x: Double, y: Double, z: Double): Double {
        return sqrt((x - this.x).pow(2) + (y - this.y).pow(2) + (z - this.z).pow(2))
    }

    fun distanceTo(vec: Vec3d): Double {
        return distanceTo(vec.x, vec.y, vec.z)
    }
}