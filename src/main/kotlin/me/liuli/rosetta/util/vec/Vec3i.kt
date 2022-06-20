package me.liuli.rosetta.util.vec

data class Vec3i(var x: Int = 0, var y: Int = 0, var z: Int = 0) {

    fun set(x: Int, y: Int, z: Int): Vec3i {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(vec: Vec3i): Vec3i {
        this.x = vec.x
        this.y = vec.y
        this.z = vec.z
        return this
    }
}