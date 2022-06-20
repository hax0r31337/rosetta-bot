package me.liuli.rosetta.entity

import java.util.*

open class EntityPlayer : EntityLiving() {

    var uuid = UUID.randomUUID()

    override val type = "player"
}