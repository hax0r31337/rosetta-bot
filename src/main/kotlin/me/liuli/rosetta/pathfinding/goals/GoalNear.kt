package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.pathfinding.path.Move

/**
 * A block position that the player should get within a certain radius of, used for following entities
 */
open class GoalNear(x: Int, y: Int, z: Int, range: Double) : GoalBlock(x, y, z) {

    protected val rangeSq = range * range

    override fun isEnd(node: Move): Boolean {
        val dx = this.x - node.x
        val dy = this.y - node.y
        val dz = this.z - node.z
        return (dx * dx + dy * dy + dz * dz) <= this.rangeSq
    }
}