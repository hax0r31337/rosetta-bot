package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.pathfinding.path.Move

/**
 * Useful for finding builds that you don't have an exact Y level for, just an approximate X and Z level
 */
open class GoalNearXZ(x: Int, z: Int, range: Double) : GoalXZ(x, z) {

    protected val rangeSq = range * range

    override fun isEnd(node: Move): Boolean {
        val dx = this.x - node.x
        val dz = this.z - node.z
        return (dx * dx + dz * dz) <= this.rangeSq
    }
}