package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.util.distanceXZ
import me.liuli.rosetta.util.vec.Vec3i
import kotlin.math.abs

/**
 * One specific block that the player should stand inside at foot level
 */
open class GoalBlock(val x: Int, val y: Int, val z: Int) : IGoal {

    override fun heuristic(node: Move): Double {
        return distanceXZ(this.x - node.x, this.z - node.z).toDouble() + abs(this.y - node.y)
    }

    override fun isEnd(pos: Vec3i): Boolean {
        return pos.x == this.x && pos.y == this.y && pos.z == this.z
    }

    override fun hasChanged() = false

    override fun isValid() = true
}