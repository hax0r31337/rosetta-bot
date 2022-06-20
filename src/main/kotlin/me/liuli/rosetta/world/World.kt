package me.liuli.rosetta.world

import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.data.*
import java.util.*

class World {

    val entities = mutableMapOf<Int, Entity>()
    // TODO: tile entities
    val playerList = mutableMapOf<UUID, NetworkPlayerInfo>()
    var playerListInfo = Pair("", "")
    val bossBar = mutableMapOf<UUID, BossBar>()

    // scoreboard
    val scoreboard = mutableMapOf<String, Scoreboard>()
    var displayScoreboardName = ""
    val displayScoreboard: Scoreboard?
        get() = scoreboard[displayScoreboardName]

    var gamemode = EnumGameMode.SURVIVAL
    var difficulty = EnumDifficulty.NORMAL

    // ambience
    var time = 0L
    var rainStrength = 0.0f
    var thunderStrength = 0.0f

    val spawn = Vec3i()

    fun tick() {
        time++
    }
}