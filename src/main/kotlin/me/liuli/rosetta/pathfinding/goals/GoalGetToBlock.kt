package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.util.distanceXZ
import kotlin.math.abs

/**
 * Don't get into the block, but get directly adjacent to it. Useful for chests.
 */
open class GoalGetToBlock(val x: Int, val y: Int, val z: Int) : IGoal {

    override fun heuristic(node: Move): Double {
        return distanceXZ(this.x - node.x, this.z - node.z).toDouble() +
                abs((node.y - this.y).let { if(it < 0) it + 1 else it })
    }

    override fun isEnd(node: Move): Boolean {
        return abs(this.x - node.x) + abs(this.z - node.z) + abs((node.y - this.y).let { if(it < 0) it + 1 else it }) == 1
    }

    override fun hasChanged() = false

    override fun isValid() = true
}