package me.liuli.rosetta.util.vec

data class Vec3f(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {

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
}