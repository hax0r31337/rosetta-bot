package me.liuli.rosetta.entity.client

import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.util.vec.Vec3f

class EntityClientPlayer : EntityPlayer() {

    override val type = "client"
    val motion = Vec3f()

    var isSpawned = false
}