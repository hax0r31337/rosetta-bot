package me.liuli.rosetta.entity.move

import me.liuli.rosetta.entity.Entity

interface IMoveSpeedModifier {

    fun getSpeed(baseSpeed: Float, isFlying: Boolean, entity: Entity): Float
}