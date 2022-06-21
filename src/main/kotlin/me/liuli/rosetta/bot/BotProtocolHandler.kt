package me.liuli.rosetta.bot

import me.liuli.rosetta.bot.event.*
import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.entity.EntityLiving
import me.liuli.rosetta.world.Chunk
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.data.*
import java.awt.Dimension
import java.util.*

class BotProtocolHandler(val bot: MinecraftBot) {

    fun onConnected() {
        if (!bot.isConnected) {
            bot.isConnected = true
            bot.startTick()
        }
        bot.emit(ConnectedEvent())
    }

    fun onDisconnect(reason: String, isClient: Boolean = false) {
        bot.isConnected = false
        bot.emit(DisconnectEvent(reason, isClient))
    }

    fun onJoinGame(entityId: Int, dimension: Int) {
        bot.player.id = entityId
        bot.world.entities[entityId] = bot.player
        bot.world.dimension = dimension
    }

    fun onGamemodeChange(gamemode: EnumGameMode) {
        bot.world.gamemode = gamemode
    }

    fun onDifficultyChange(difficulty: EnumDifficulty) {
        bot.world.difficulty = difficulty
    }

    fun onPlayerTeleport(x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Boolean {
        val event = TeleportEvent(x, y, z, yaw, pitch)
        bot.emit(event)

        if (event.isCancelled) {
            return false
        }
        val player = bot.player
        player.motion.x = 0f
        player.motion.y = 0f
        player.motion.z = 0f
        player.position.set(event.x, event.y, event.z)
        player.rotation.set(event.yaw, event.pitch)
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

    fun onHealthChange(entityId: Int, health: Float, maxHealth: Float, absorption: Float) {
        val entity = bot.world.entities[entityId] ?: return
        if (entity !is EntityLiving) {
            return
        }
        entity.health = health
        entity.maxHealth = maxHealth
        entity.absorption = absorption
    }

    fun onSpawnPositionChange(x: Int, y: Int, z: Int) {
        bot.world.spawn.set(x, y, z)
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

    fun spawnEntity(instance: Entity) {
        bot.world.entities[instance.id] = instance
    }

    fun onSetMotion(entityId: Int, motionX: Float, motionY: Float, motionZ: Float) {
        if (entityId == bot.player.id) {
            bot.player.motion.set(motionX, motionY, motionZ)
        }
    }

    fun onTeleport(entityId: Int, x: Double, y: Double, z: Double, yaw: Float, pitch: Float, onGround: Boolean) {
        val entity = bot.world.entities[entityId] ?: return
        entity.position.set(x, y, z)
        entity.rotation.set(yaw, pitch)
    }

    fun onMovement(entityId: Int, onGround: Boolean) {
//        val entity = bot.world.entities[entityId] ?: return
    }

    fun onMovement(entityId: Int, onGround: Boolean, x: Double, y: Double, z: Double) {
        val entity = bot.world.entities[entityId] ?: return
        entity.position.set(x, y, z)
    }

    fun onMovement(entityId: Int, onGround: Boolean, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        val entity = bot.world.entities[entityId] ?: return
        entity.position.set(x, y, z)
        entity.rotation.set(yaw, pitch)
    }

    fun onMovement(entityId: Int, onGround: Boolean, yaw: Float, pitch: Float) {
        val entity = bot.world.entities[entityId] ?: return
        entity.rotation.set(yaw, pitch)
    }


    fun onRemoveEntity(entityId: Int) {
        bot.world.entities.remove(entityId)
    }

    fun onWeatherUpdate(rain: Float, thunder: Float) {
        bot.world.rainStrength = rain
        bot.world.thunderStrength = thunder
    }

    fun onPlayerListInfoUpdate(header: String, footer: String) {
        bot.world.playerListInfo = header to footer
    }

    fun onTitle(type: EnumTitleType, message: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        bot.emit(TitleEvent(type, message, fadeIn, stay, fadeOut))
    }

    fun setBossBar(bossBar: BossBar) {
        bot.world.bossBar[bossBar.uuid] = bossBar
    }

    fun removeBossBar(uuid: UUID) {
        bot.world.bossBar.remove(uuid)
    }

    fun setScoreboard(sb: Scoreboard) {
        bot.world.scoreboard[sb.name] = sb
    }

    fun removeScoreboard(sb: String) {
        bot.world.scoreboard.remove(sb)
//        if (bot.world.displayScoreboardName == sb) {
//            bot.world.displayScoreboardName = ""
//        }
    }

    fun displayScoreboard(sb: String) {
        bot.world.displayScoreboardName = sb
    }

    fun addEffect(entityId: Int, effect: PotionEffect) {
        val entity = bot.world.entities[entityId] as? EntityLiving ?: return
        entity.effects.removeIf { it.name == effect.name }
        entity.effects.add(effect)
    }

    fun removeEffect(entityId: Int, effect: String) {
        val entity = bot.world.entities[entityId] as? EntityLiving ?: return
        entity.effects.removeIf { it.name == effect }
    }

    fun onChunk(chunk: Chunk) {
        bot.world.setChunk(chunk)
    }

    fun unloadChunk(x: Int, z: Int) {
        bot.world.eraseChunkAt(x, z)
    }

    fun onBlockUpdate(x: Int, y: Int, z: Int, block: Block) {
        bot.world.setBlockAt(x, y, z, block)
    }

    fun onSwing(entityId: Int) {
//        val entity = bot.world.entities[entityId] as? Entity
    }

    fun onRespawn(dimension: Int, clearChunk: Boolean) {
        bot.world.dimension = dimension
        if (clearChunk) {
            bot.world.chunk.clear()
        }
    }

    fun onHeldItemChange(heldItem: Int) {
        bot.player.heldItemSlot = heldItem
    }
}