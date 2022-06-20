package me.liuli.rosetta.world.data

data class PotionEffect(val name: String, val amplifier: Int, var duration: Int) {

    fun tick() {
        duration = (duration - 1).coerceAtLeast(0)
    }
}