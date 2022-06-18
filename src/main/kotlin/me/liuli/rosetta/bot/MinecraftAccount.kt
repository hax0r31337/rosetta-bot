package me.liuli.rosetta.bot

import java.util.*

data class MinecraftAccount(val username: String, val uuid: UUID, val accessToken: String) {
    companion object {
        fun offline(username: String): MinecraftAccount {
            return MinecraftAccount(username, UUID.nameUUIDFromBytes(("Offline: $username").toByteArray(Charsets.UTF_8)), "-")
        }
    }
}