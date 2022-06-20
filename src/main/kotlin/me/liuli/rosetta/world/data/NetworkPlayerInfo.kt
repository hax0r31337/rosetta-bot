package me.liuli.rosetta.world.data

import java.util.*

data class NetworkPlayerInfo(val uuid: UUID, val name: String, var gamemode: EnumGameMode, var latency: Int, var displayName: String?)