package me.liuli.rosetta.pathfinding

data class Move (val x: Int, val y: Int, val z: Int, val remainingBlocks: Int, val cost: Double, val parkour: Boolean) {

    // TODO: toBreak, toPlace

    override fun hashCode(): Int {
        return x shl 16 or z shl 4 or y
    }
}