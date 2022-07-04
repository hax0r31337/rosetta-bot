package me.liuli.rosetta.bot.event

import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.entity.EntityLiving
import me.liuli.rosetta.entity.inventory.Window
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.Chunk
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.data.EnumDifficulty
import me.liuli.rosetta.world.data.EnumGameMode
import me.liuli.rosetta.world.data.EnumTitleType
import me.liuli.rosetta.world.data.PotionEffect
import me.liuli.rosetta.world.item.Item

// event from server

/**
 * called when bot connected to server
 */
class ConnectedEvent : Event()

/**
 * called when bot got kicked or disconnected
 */
class DisconnectEvent(val reason: String, val isClient: Boolean) : Event()

// CLIENT EVENTS

/**
 * called when bot got teleported by the server
 */
class PlayerTeleportEvent(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float) : EventCancellable()

/**
 * called when received chat message from server
 */
class ChatReceiveEvent(var message: String, var json: String) : Event()

/**
 * called every tick before stream position to server
 */
class PreMotionEvent : EventCancellable()

/**
 * called every tick after stream position to server
 */
class PostMotionEvent : Event()

/**
 * called when display the title
 */
class TitleEvent(val type: EnumTitleType, val message: String, val fadeIn: Int, val stay: Int, val fadeOut: Int) : Event()

/**
 * called when player death
 */
class PlayerDeathEvent(val cause: String = "unknown") : Event()

/**
 * called when player joined the game
 */
class PlayerJoinGameEvent : Event()

/**
 * called when player abilities changed
 */
class PlayerAbilitiesChangeEvent(val isFlying: Boolean, val canFly: Boolean, val isInvincible: Boolean) : Event()

/**
 * called when player moving speed changed
 */
class PlayerMoveSpeedChangeEvent(val walkSpeed: Float, val flySpeed: Float) : Event()

/**
 * called when player respawn
 */
class PlayerRespawnEvent(val dimension: Int, val clearChunk: Boolean) : Event()

/**
 * called when server forced to change player held item slot
 */
class PlayerHeldItemChangedEvent(val slot: Int) : Event()

/**
 * called when server displayed a window to client, null when close window
 */
class PlayerWindowDisplayEvent(val window: Window?) : Event()

/**
 * called when player food level changed
 */
class PlayerFoodLevelChangeEvent(val food: Float, val foodSaturation: Float) : Event()

/**
 * called when player exp level changed
 */
class PlayerExperienceLevelChangeEvent(val experience: Float, val level: Int) : Event()

/**
 * called when inventory/window slot updated
 * @param windowId 0 when inventory
 */
class PlayerInventoryUpdateEvent(val windowId: Int, val slot: Int, val item: Item)

// ENTITY EVENTS

/**
 * called when entity spawned (not include client player)
 */
class EntitySpawnEvent(val entity: Entity) : Event()

/**
 * called when server request to set entity velocity
 */
class EntityVelocitySetEvent(val entity: Entity, val motionX: Double, val motionY: Double, val motionZ: Double) : Event()

/**
 * called when entity teleported or moved
 */
class EntityMoveEvent(val entity: Entity, val x: Double, val y: Double, val z: Double, val yaw: Float, val pitch: Float) : Event()

/**
 * called when entity despawned
 */
class EntityDespawnEvent(val entity: Entity) : Event()

/**
 * called when entity (include client player) health changed
 */
class EntityHealthChangeEvent(val entity: EntityLiving, val health: Float, val maxHealth: Float, val absorption: Float) : Event()

/**
 * called when entity was set on a effect
 */
class EntityAddEffectEvent(val entity: EntityLiving, val effect: PotionEffect) : Event()

/**
 * called when a entity effect was removed
 */
class EntityRemoveEffectEvent(val entity: EntityLiving, val effectName: String) : Event()

// WORLD EVENTS

/**
 * called when [EnumGameMode] changed
 */
class WorldGameModeChangeEvent(val gameMode: EnumGameMode): Event()

/**
 * called when [EnumDiffculty] changed
 */
class WorldDifficultyChangeEvent(val difficulty: EnumDifficulty) : Event()

/**
 * called when world time changed or sync
 */
class WorldTimeChangeEvent(val time: Long) : Event()

/**
 * called when world spawn point is changed
 */
class WorldSpawnPointChangeEvent(val pos: Vec3i) : Event()

/**
 * called when world weather changed
 */
class WorldWeatherChangeEvent(val rain: Float, val thunder: Float) : Event()

/**
 * called when world chunk loaded
 */
class WorldChunkLoadEvent(val chunk: Chunk) : Event()

/**
 * called when world chunk unload
 */
class WorldChunkUnloadEvent(val x: Int, val z: Int) : Event()

/**
 * called when world block update
 */
class WorldBlockUpdateEvent(val x: Int, val y: Int, val z: Int, val block: Block) : Event()