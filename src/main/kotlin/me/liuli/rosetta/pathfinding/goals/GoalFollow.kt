package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.util.distanceXZ
import kotlin.math.abs
import kotlin.math.floor

open class GoalFollow(val entity: Entity, range: Double) : IGoal {

    protected var x = floor(entity.position.x).toInt()
    protected var y = floor(entity.position.y).toInt()
    protected var z = floor(entity.position.z).toInt()
    protected val rangeSq = range * range

    override fun heuristic(node: Move): Double {
        return distanceXZ(this.x - node.x, this.z - node.z).toDouble() + abs(this.y - node.y)
    }

    override fun isEnd(node: Move): Boolean {
        val dx = this.x - node.x
        val dy = this.y - node.y
        val dz = this.z - node.z
        return (dx * dx + dy * dy + dz * dz) <= this.rangeSq
    }

    override fun hasChanged(): Boolean {
        val px = floor(entity.position.x).toInt()
        val py = floor(entity.position.y).toInt()
        val pz = floor(entity.position.z).toInt()
        val dx = this.x - px
        val dy = this.y - py
        val dz = this.z - pz
        if (dx * dx + dy * dy + dz * dz > rangeSq) {
            this.x = px
            this.y = py
            this.z = pz
            return true
        }
        return false
    }

    override fun isValid() = true
}