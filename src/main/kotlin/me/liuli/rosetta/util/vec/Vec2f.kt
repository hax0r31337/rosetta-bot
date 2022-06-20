package me.liuli.rosetta.util.vec

data class Vec2f(var x: Float = 0f, var y: Float = 0f) {

    fun set(x: Float, y: Float): Vec2f {
        this.x = x
        this.y = y
        return this
    }

    fun set(vec: Vec2f): Vec2f {
        this.x = vec.x
        this.y = vec.y
        return this
    }
}