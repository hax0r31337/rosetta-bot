package me.liuli.rosetta.world.data

data class Team(val name: String, var displayName: String, var prefix: String, var suffix: String, var players: MutableList<String>)