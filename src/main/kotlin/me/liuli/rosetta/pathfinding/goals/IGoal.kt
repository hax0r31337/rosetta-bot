package me.liuli.rosetta.pathfinding.goals

import me.liuli.rosetta.pathfinding.Move

interface IGoal {

    fun heuristic(node: Move): Double

    fun isEnd(node: Move): Boolean

    fun hasChanged(): Boolean

    fun isValid(): Boolean
}