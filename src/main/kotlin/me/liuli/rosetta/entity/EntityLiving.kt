package me.liuli.rosetta.entity

import me.liuli.rosetta.world.data.PotionEffect

open class EntityLiving : Entity() {

    // health
    var health = 20.0f
    var maxHealth = 20.0f
    var absorption = 0.0f

    val effects = mutableListOf<PotionEffect>()

    override val type = "living"

    override fun tick() {
        super.tick()
        effects.forEach {
            it.duration--
        }
        effects.removeIf { it.duration <= 0 }
    }
}