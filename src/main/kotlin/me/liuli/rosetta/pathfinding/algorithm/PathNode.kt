package me.liuli.rosetta.pathfinding.algorithm

import me.liuli.rosetta.pathfinding.path.Move

class PathNode() {

    lateinit var data: Move
    var g = .0
    var h = .0
    var f = .0
    var parent: PathNode? = null

    @JvmField
    var heapPosition = 0

    constructor(data: Move, g: Double, h: Double, parent: PathNode? = null) : this() {
        this.set(data, g, h, parent)
    }

    fun set(data: Move, g: Double, h: Double, parent: PathNode? = null) {
        this.data = data
        this.g = g
        this.h = h
        this.f = g + h
        this.parent = parent
    }
}