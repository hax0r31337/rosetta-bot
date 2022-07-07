package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.util.vec.Vec3i

interface IGoal {

    /**
     * @return the distance between node and the goal
     */
    fun heuristic(node: Move): Double

    /**
     * @return true if the node has reach the goal
     */
    fun isEnd(pos: Vec3i): Boolean

    /**
     * @return true if the goal has changed and the current path
     * should be invalidated and computed again
     */
    fun hasChanged(): Boolean

    /**
     * @return true if the goal is still valid for the goal
     * for the GoalFollow this would be true if the entity is not null
     */
    fun isValid(): Boolean
}