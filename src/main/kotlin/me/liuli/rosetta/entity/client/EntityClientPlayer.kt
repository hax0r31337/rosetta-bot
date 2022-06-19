package me.liuli.rosetta.entity.client

import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.util.vec.Vec3f

class EntityClientPlayer : EntityPlayer() {

    override val type = "client"
    val motion = Vec3f()

    // abilities
    var invincible = false
    var flying = false
    var canFly = false

    // movement
    var flySpeed = 0.05f
    var walkSpeed = 0.1f

    // experience
    var exp = 0.0f
    var expLevel = 0

    // food
    var food = 20.0f
    var foodSaturation = 0.0f

    // TODO: inventory

    var isSpawned = false
}