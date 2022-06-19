package me.liuli.rosetta.bot

import me.liuli.rosetta.bot.event.ConnectedEvent
import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.event.TeleportEvent
import me.liuli.rosetta.world.data.EnumDifficulty
import me.liuli.rosetta.world.data.EnumGameMode

class BotProtocolHandler(val bot: MinecraftBot) {

    fun onConnected() {
        bot.emit(ConnectedEvent())
    }

    fun onDisconnect(reason: String, isClient: Boolean = false) {
        bot.isConnected = false
        bot.emit(DisconnectEvent(reason, isClient))
    }

    fun onJoinGame(entityId: Int, gamemode: EnumGameMode, difficulty: EnumDifficulty) {
        bot.player.id = entityId
        this.onGamemodeChange(gamemode)
        this.onDifficultyChange(difficulty)
    }

    fun onGamemodeChange(gamemode: EnumGameMode) {
        bot.world.gamemode = gamemode
    }

    fun onDifficultyChange(difficulty: EnumDifficulty) {
        bot.world.difficulty = difficulty
    }

    fun onTeleport(x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Boolean {
        val event = TeleportEvent(x, y, z, yaw, pitch)
        bot.emit(event)

        if (event.isCancelled) {
            return false
        }
        val player = bot.player
        player.motion.x = 0f
        player.motion.y = 0f
        player.motion.z = 0f
        player.position.x = event.x
        player.position.y = event.y
        player.position.z = event.z
        player.rotation.x = event.yaw
        player.rotation.y = event.pitch

        return true
    }
}