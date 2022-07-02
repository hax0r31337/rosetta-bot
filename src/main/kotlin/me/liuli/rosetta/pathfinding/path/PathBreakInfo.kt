package me.liuli.rosetta.pathfinding.path

data class PathBreakInfo(val x: Int, val y: Int, val z: Int) {
    constructor(block: PathBlock) : this(block.x, block.y, block.z)
}