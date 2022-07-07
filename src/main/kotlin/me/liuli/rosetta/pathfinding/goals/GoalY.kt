package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.util.vec.Vec3i
import kotlin.math.abs

/**
 * Goal is a Y coordinate
 */
open class GoalY(val y: Int) : IGoal {

    override fun heuristic(node: Move): Double {
        return abs(this.y - node.y).toDouble()
    }

    override fun isEnd(pos: Vec3i): Boolean {
        return pos.y == this.y
    }

    override fun hasChanged() = false

    override fun isValid() = true
}