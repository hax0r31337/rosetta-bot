package me.liuli.rosetta.world.data

import me.liuli.rosetta.world.World

data class Scoreboard(val name: String, var displayName: String, var sort: Sort) {

    val score = mutableMapOf<String, Score>()

    val sortedScore: List<Score>
        get() = (if(sort == Sort.ASCENDING) 1 else -1).let { s ->
            score.values.sortedBy { s * it.score }
        }

    enum class Sort {
        ASCENDING,
        DESCENDING
    }

    data class Score(val name: String, var score: Int) {

        fun displayName(world: World) = world.findTeam(name)?.let {
            it.prefix + name + it.suffix
        } ?: name
    }
}
