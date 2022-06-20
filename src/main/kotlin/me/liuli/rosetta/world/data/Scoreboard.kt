package me.liuli.rosetta.world.data

data class Scoreboard(val name: String, var displayName: String, var sort: Sort) {

    val score = mutableMapOf<String, Score>()
    val teams = mutableMapOf<String, Team>()

    val sortedScore: List<Score>
        get() = (if(sort == Sort.ASCENDING) 1 else -1).let { s ->
            score.values.sortedBy { s * it.score }
        }

    fun findTeam(player: String): Team? {
        return teams.values.firstOrNull { it.players.contains(player) }
    }

    enum class Sort {
        ASCENDING,
        DESCENDING
    }

    data class Score(val name: String, var score: Int) {

        fun displayName(sb: Scoreboard) = sb.findTeam(name)?.let {
            it.prefix + name + it.suffix
        } ?: name
    }

    data class Team(val name: String, var displayName: String, var prefix: String, var suffix: String, var players: MutableList<String>)
}
