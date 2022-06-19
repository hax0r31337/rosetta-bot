package me.liuli.rosetta.world

import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.data.EnumDifficulty
import me.liuli.rosetta.world.data.EnumGameMode
import me.liuli.rosetta.world.data.NetworkPlayerInfo
import java.util.UUID

class World {

    val entities = mutableMapOf<Int, Entity>()
    val playerList = mutableMapOf<UUID, NetworkPlayerInfo>()

    var gamemode = EnumGameMode.SURVIVAL
    var difficulty = EnumDifficulty.NORMAL
    var time = 0L

    val spawn = Vec3i()

    // TODO: tick time
}