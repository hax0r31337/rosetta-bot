package me.liuli.rosetta.entity.client

import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.entity.move.IMoveSpeedModifier
import me.liuli.rosetta.util.vec.Vec3f

class EntityClientPlayer : EntityPlayer() {

    override val type = "client"
    val motion = Vec3f()
    var onGround = false

    // abilities
    var invincible = false
    var flying = false
    var canFly = false

    // movement
    var baseFlySpeed = 0.05f
    var baseWalkSpeed = 0.1f
    var moveSpeedModifiers = mutableListOf<IMoveSpeedModifier>()
    val walkSpeed: Float
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

    // TODO: inventory
    var heldItemSlot = 0

    var isSpawned = false
    var isAlive = true

}