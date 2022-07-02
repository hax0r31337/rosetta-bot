package me.liuli.rosetta.pathfinding.path

data class Move(val x: Int, val y: Int, val z: Int, val remainingBlocks: Int, val cost: Float,
                val toBreak: MutableList<PathBreakInfo> = mutableListOf(),
                val toPlace: MutableList<PathPlaceInfo> = mutableListOf(), val parkour: Boolean = false) {

    // TODO: toBreak, toPlace

    override fun hashCode(): Int {
        return x shl 16 or z shl 4 or y
    }
}