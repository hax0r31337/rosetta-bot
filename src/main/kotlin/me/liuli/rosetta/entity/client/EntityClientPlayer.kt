package me.liuli.rosetta.entity.client

import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.entity.inventory.PlayerInventory
import me.liuli.rosetta.entity.inventory.Window
import me.liuli.rosetta.entity.move.IMoveSpeedModifier
import me.liuli.rosetta.entity.move.ISimulatable
import me.liuli.rosetta.entity.move.PhysicsSetting
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.world.World
import me.liuli.rosetta.world.WorldIdentifier

class EntityClientPlayer : EntityPlayer(), ISimulatable {

    override val type = "client"
    override val motion = Vec3d()
    override var onGround = false

    // abilities
    var needAbilitiesUpdate = false
    var invincible = false
        set(value) {
            if (field != value) {
                needAbilitiesUpdate = true
            }
            field = value
        }
    var flying = false
        set(value) {
            if (field != value) {
                needAbilitiesUpdate = true
            }
            field = value
        }
    var canFly = false
        set(value) {
            if (field != value) {
                needAbilitiesUpdate = true
            }
            field = value
        }

    // movement
    var baseFlySpeed = 0.05f
    var baseWalkSpeed = 0.1f
    var moveSpeedModifiers = mutableListOf<IMoveSpeedModifier>()
    override val walkSpeed: Float
        get() {
            var speed = baseWalkSpeed
            moveSpeedModifiers.forEach { m -> speed = m.getSpeed(speed, false, this) }
            return speed
        }
    val flySpeed: Float
        get() {
            var speed = baseFlySpeed
            moveSpeedModifiers.forEach { m -> speed = m.getSpeed(speed, true, this) }
            return speed
        }

    // experience
    var exp = 0.0f
    var expLevel = 0

    // food
    var food = 20.0f
    var foodSaturation = 0.0f

    override val inventory = PlayerInventory(this)
    var openWindow: Window? = null
        set(value) {
            field?.let { it.onClose() }
            value?.let { it.onOpen(this) }
            field = value
        }
    var heldItemSlot = 0

    var isSpawned = false
    var isAlive = true

    /**
     * only available when calling [applyMotionCollides]
     */
    override var isCollidedHorizontally = false
    /**
     * only available when calling [applyMotionCollides]
     */
    override var isCollidedVertically = false

    fun applyMotion() {
        position.x += motion.x
        position.y += motion.y
        position.z += motion.z
    }
}