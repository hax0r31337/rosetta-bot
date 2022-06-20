package me.liuli.rosetta.util.vec

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
}