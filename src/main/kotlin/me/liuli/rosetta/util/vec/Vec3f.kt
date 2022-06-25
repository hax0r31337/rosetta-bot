package me.liuli.rosetta.util.vec

import kotlin.math.sqrt

data class Vec3f(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {

    val norm: Float
        get() = sqrt(x * x + y * y + z * z)

    fun set(x: Float, y: Float, z: Float): Vec3f {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(vec: Vec3f): Vec3f {
        this.x = vec.x
        this.y = vec.y
        this.z = vec.z
        return this
    }

    fun normalize(): Vec3f {
        val norm = this.norm
        if (norm != 0f) {
            this.x /= norm
            this.y /= norm
            this.z /= norm
        }
        return this
    }
}