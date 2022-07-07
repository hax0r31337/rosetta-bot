package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.util.distanceXZ
import me.liuli.rosetta.util.vec.Vec3i
import kotlin.math.abs

/**
 * Useful for long-range goals that don't have a specific Y level
 */
open class GoalXZ(val x: Int, val z: Int) : IGoal {

    override fun heuristic(node: Move): Double {
        return distanceXZ(this.x - node.x, this.z - node.z).toDouble()
    }

    override fun isEnd(pos: Vec3i): Boolean {
        return pos.x == this.x && pos.z == this.z
    }

    override fun hasChanged() = false

    override fun isValid() = true
}