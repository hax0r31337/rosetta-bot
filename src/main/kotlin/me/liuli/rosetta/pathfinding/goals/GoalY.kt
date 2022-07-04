package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.pathfinding.path.Move
import kotlin.math.abs

/**
 * Goal is a Y coordinate
 */
open class GoalY(val y: Int) : IGoal {

    override fun heuristic(node: Move): Double {
        return abs(this.y - node.y).toDouble()
    }

    override fun isEnd(node: Move): Boolean {
        return node.y == this.y
    }

    override fun hasChanged() = false

    override fun isValid() = true
}