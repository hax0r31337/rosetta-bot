package me.liuli.rosetta.bot

import me.liuli.rosetta.bot.event.*
import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.entity.EntityLiving
import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.entity.inventory.EnumEquipment
import me.liuli.rosetta.entity.inventory.Window
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.Chunk
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.data.*
import me.liuli.rosetta.world.item.Item
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
        bot.emit(PlayerJoinGameEvent())
    }

    fun onGamemodeChange(gamemode: EnumGameMode) {
        bot.emit(WorldGameModeChangeEvent(gamemode))
        bot.world.gamemode = gamemode
    }

    fun onDifficultyChange(difficulty: EnumDifficulty) {
        bot.emit(WorldDifficultyChangeEvent(difficulty))
        bot.world.difficulty = difficulty
    }

    fun onPlayerTeleport(x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Boolean {
        val event = PlayerTeleportEvent(x, y, z, yaw, pitch)
        bot.emit(event)

        if (event.isCancelled) {
            return false
        }
        val player = bot.player
        player.motion.set(.0, .0, .0)
        player.position.set(event.x, event.y, event.z)
        player.rotation.set(event.yaw, event.pitch)
        player.isSpawned = true

        return true
    }

    fun onChat(msg: String, json: String) {
        bot.emit(ChatReceiveEvent(msg, json))
    }

    fun onTimeUpdate(time: Long) {
        bot.emit(WorldTimeChangeEvent(time))
        bot.world.time = time
    }

    fun onAbilitiesChange(isFlying: Boolean, canFly: Boolean, isInvincible: Boolean) {
        bot.emit(PlayerAbilitiesChangeEvent(isFlying, canFly, isInvincible))
        bot.player.flying = isFlying
        bot.player.canFly = canFly
        bot.player.invincible = isInvincible
        bot.player.needAbilitiesUpdate = false
    }

    fun onMoveSpeedChange(walkSpeed: Float, flySpeed: Float) {
        bot.emit(PlayerMoveSpeedChangeEvent(walkSpeed, flySpeed))
        bot.player.baseWalkSpeed = walkSpeed
        bot.player.baseFlySpeed = flySpeed
    }

    fun onFoodChange(food: Float, foodSaturation: Float) {
        bot.emit(PlayerFoodLevelChangeEvent(food, foodSaturation))
        bot.player.food = food
        bot.player.foodSaturation = foodSaturation
    }

    fun onHealthChange(entityId: Int, health: Float, maxHealth: Float, absorption: Float) {
        val entity = bot.world.entities[entityId] ?: return
        if (entity !is EntityLiving) {
            return
        }
        bot.emit(EntityHealthChangeEvent(entity, health, maxHealth, absorption))
        entity.health = health
        entity.maxHealth = maxHealth
        entity.absorption = absorption
    }

    fun onSpawnPositionChange(x: Int, y: Int, z: Int) {
        bot.emit(WorldSpawnPointChangeEvent(Vec3i(x, y, z)))
        bot.world.spawn.set(x, y, z)
    }

    fun onExperienceChange(experience: Float, level: Int) {
        bot.emit(PlayerExperienceLevelChangeEvent(experience, level))
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
        bot.emit(EntitySpawnEvent(instance))
        bot.world.entities[instance.id] = instance
    }

    fun onSetMotion(entityId: Int, motionX: Double, motionY: Double, motionZ: Double) {
        val entity = bot.world.entities[entityId] ?: return
        bot.emit(EntityVelocitySetEvent(entity, motionX, motionY, motionZ))
        if (entity is EntityClientPlayer) {
            entity.motion.set(motionX, motionY, motionZ)
        }
    }

    fun onTeleport(entityId: Int, x: Double, y: Double, z: Double, yaw: Float, pitch: Float, onGround: Boolean) {
        val entity = bot.world.entities[entityId] ?: return
        bot.emit(EntityMoveEvent(entity, x, y, z, yaw, pitch))
        entity.position.set(x, y, z)
        entity.rotation.set(yaw, pitch)
    }

    fun onMovement(entityId: Int, onGround: Boolean) {
//        val entity = bot.world.entities[entityId] ?: return
    }

    fun onMovement(entityId: Int, onGround: Boolean, x: Double, y: Double, z: Double) {
        val entity = bot.world.entities[entityId] ?: return
        bot.emit(EntityMoveEvent(entity, x, y, z, entity.rotation.x, entity.rotation.y))
        entity.position.set(x, y, z)
    }

    fun onMovement(entityId: Int, onGround: Boolean, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        val entity = bot.world.entities[entityId] ?: return
        bot.emit(EntityMoveEvent(entity, x, y, z, yaw, pitch))
        entity.position.set(x, y, z)
        entity.rotation.set(yaw, pitch)
    }

    fun onMovement(entityId: Int, onGround: Boolean, yaw: Float, pitch: Float) {
        val entity = bot.world.entities[entityId] ?: return
        bot.emit(EntityMoveEvent(entity, entity.position.x, entity.position.y, entity.position.z, yaw, pitch))
        entity.rotation.set(yaw, pitch)
    }


    fun onRemoveEntity(entityId: Int) {
        val entity = bot.world.entities[entityId] ?: return
        bot.emit(EntityDespawnEvent(entity))
        entity.riding = null
        entity.passengers.map { it }.forEach {
            it.riding = null
        }
        bot.world.entities.remove(entityId)
    }

    fun onWeatherUpdate(rain: Float, thunder: Float) {
        bot.emit(WorldWeatherChangeEvent(rain, thunder))
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
        bot.emit(EntityAddEffectEvent(entity, effect))
        entity.effects.removeIf { it.name == effect.name }
        entity.effects.add(effect)
    }

    fun removeEffect(entityId: Int, effect: String) {
        val entity = bot.world.entities[entityId] as? EntityLiving ?: return
        bot.emit(EntityRemoveEffectEvent(entity, effect))
        entity.effects.removeIf { it.name == effect }
    }

    fun onChunk(chunk: Chunk) {
        bot.emit(WorldChunkLoadEvent(chunk))
        bot.world.setChunk(chunk)
    }

    fun unloadChunk(x: Int, z: Int) {
        bot.emit(WorldChunkUnloadEvent(x, z))
        bot.world.eraseChunkAt(x, z)
    }

    fun onBlockUpdate(x: Int, y: Int, z: Int, block: Block) {
        bot.emit(WorldBlockUpdateEvent(x, y, z, block))
        bot.world.setBlockAt(x, y, z, block)
    }

    fun onSwing(entityId: Int) {
//        val entity = bot.world.entities[entityId] as? Entity
    }

    fun onRespawn(dimension: Int, clearChunk: Boolean) {
        bot.emit(PlayerRespawnEvent(dimension, clearChunk))
        bot.world.dimension = dimension
        if (clearChunk) {
            bot.world.chunk.clear()
        }
    }

    fun onHeldItemChange(heldItem: Int) {
        bot.emit(PlayerHeldItemChangedEvent(heldItem))
        bot.player.heldItemSlot = heldItem
    }

    fun onEntityDeath(entityId: Int, cause: String) {
        if (entityId == bot.player.id) {
            bot.emit(PlayerDeathEvent(cause))
            bot.player.isAlive = false
        }
    }

    fun onEntityPose(entityId: Int, sprinting: Boolean, sneaking: Boolean) {
        val player = bot.world.entities[entityId] as? EntityPlayer ?: return
        player.sprinting = sprinting
        player.sneaking = sneaking
    }

    fun onWorldBorderChangeCenter(x: Double, z: Double, size: Int) {
        bot.world.border.centerX = x
        bot.world.border.centerZ = z
        bot.world.border.worldSize = size
    }

    fun onWorldBorderChangeSize(startDiameter: Double, endDiameter: Double, time: Long) {
        bot.world.border.setTransition(startDiameter, endDiameter, time)
    }

    fun onWorldBorderChangeWarning(warningDistance: Int, warningTime: Int) {
        bot.world.border.warningDistance = warningDistance
        bot.world.border.warningTime = warningTime
    }

    fun onRequestEditTileEntity(x: Int, y: Int, z: Int) {

    }

    fun onSetPassengers(vehicleId: Int, passengers: IntArray) {
        val vehicle = bot.world.entities[vehicleId] ?: return
        vehicle.passengers.map { it }.forEach {
            if (!passengers.contains(it.id)) {
                it.riding = null
            }
        }
        passengers.forEach {
            val entity = bot.world.entities[it] ?: return@forEach
            entity.riding = vehicle
        }
    }

    fun setWindow(window: Window?) {
        bot.emit(PlayerWindowDisplayEvent(window))
        bot.player.openWindow = window
    }

    fun setEquipment(entityId: Int, equipment: EnumEquipment, item: Item) {
        val entity = bot.world.entities[entityId] ?: return
        entity.inventory[equipment] = item
    }

    fun updateSlot(windowId: Int, slot: Int, item: Item) {
        if (windowId == 0) {
            bot.player.inventory[slot] = item
        } else {
            val window = bot.player.openWindow ?: return
            if (window.id == windowId) {
                window[slot] = item
            }
        }
    }
}