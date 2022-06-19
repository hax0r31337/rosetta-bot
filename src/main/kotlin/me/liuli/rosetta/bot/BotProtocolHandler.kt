package me.liuli.rosetta.bot

import me.liuli.rosetta.bot.event.ChatReceiveEvent
import me.liuli.rosetta.bot.event.ConnectedEvent
import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.event.TeleportEvent
import me.liuli.rosetta.world.data.EnumDifficulty
import me.liuli.rosetta.world.data.EnumGameMode
import me.liuli.rosetta.world.data.NetworkPlayerInfo

class BotProtocolHandler(val bot: MinecraftBot) {

    fun onConnected() {
        bot.isConnected = true
        bot.emit(ConnectedEvent())
    }

    fun onDisconnect(reason: String, isClient: Boolean = false) {
        bot.isConnected = false
        bot.emit(DisconnectEvent(reason, isClient))
    }

    fun onJoinGame(entityId: Int) {
        bot.player.id = entityId
        bot.world.entities[entityId] = bot.player
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
        player.isSpawned = true

        return true
    }

    fun onChat(msg: String, json: String) {
        bot.emit(ChatReceiveEvent(msg, json))
    }

    fun onTimeUpdate(time: Long) {
        bot.world.time = time
    }

    fun onAbilitiesChange(isFlying: Boolean, canFly: Boolean, isInvincible: Boolean) {
        bot.player.flying = isFlying
        bot.player.canFly = canFly
        bot.player.invincible = isInvincible
    }

    fun onMoveSpeedChange(walkSpeed: Float, flySpeed: Float) {
        bot.player.walkSpeed = walkSpeed
        bot.player.flySpeed = flySpeed
    }

    fun onFoodChange(food: Float, foodSaturation: Float) {
        bot.player.food = food
        bot.player.foodSaturation = foodSaturation
    }

    fun onHealthChange(health: Float, maxHealth: Float, absorption: Float) {
        bot.player.health = health
        bot.player.maxHealth = maxHealth
        bot.player.absorption = absorption
    }

    fun onSpawnPositionChange(x: Int, y: Int, z: Int) {
        bot.world.spawn.x = x
        bot.world.spawn.y = y
        bot.world.spawn.z = z
    }

    fun onExperienceChange(experience: Float, level: Int) {
        bot.player.exp = experience
        bot.player.expLevel = level
    }

    fun onPlayerListUpdate(list: List<NetworkPlayerInfo>) {
        list.forEach {
            bot.world.playerList[it.uuid] = it
        }
    }

    fun onPlayerListRemove(list: List<NetworkPlayerInfo>) {
        list.forEach {
            bot.world.playerList.remove(it.uuid)
        }
    }
}