package me.liuli.rosetta.entity

import java.util.*

open class EntityPlayer : EntityLiving() {

    var uuid = UUID.randomUUID()

    var sprinting = false
    var sneaking = false

    override val type = "player"
}