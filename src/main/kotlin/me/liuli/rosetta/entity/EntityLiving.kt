package me.liuli.rosetta.entity

open class EntityLiving : Entity() {

    // health
    var health = 20.0f
    var maxHealth = 20.0f
    var absorption = 0.0f

    override val type = "living"
}