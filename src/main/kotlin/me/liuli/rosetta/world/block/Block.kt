package me.liuli.rosetta.world.block

import com.google.gson.JsonObject
import me.liuli.rosetta.world.item.Item
import kotlin.math.ceil

data class Block(val id: Int, val material: Material, val name: String,
                 val hardness: Float = 0f, val diggable: Boolean = true, val harvertLimit: Array<Int>? = null,
                 val shape: Shape? = Shape.SHAPE_BLOCK) {

    val hardnessModifier = mutableListOf<IBlockHardnessModifier>()

    var additionalData: JsonObject? = null

    fun digTime(item: Item, inWater: Boolean = false, offGround: Boolean = false): Int {
        var breakingSpeed = 1f

        hardnessModifier.forEach { breakingSpeed = it.getModifier(breakingSpeed, item) }

        // TODO: check efficiency enchantment

        // TODO: check haste, mining_fatigue effect

        if (inWater /*&& not aqua_affinity*/) {
            breakingSpeed /= 5
        }

        if (offGround) {
            breakingSpeed /= 5
        }

        val matchingToolMultiplier = if (harvertLimit == null || harvertLimit.contains(item.id)) 30f else 100f
        var delta = breakingSpeed / hardness / matchingToolMultiplier

        if (hardness == -1f) {
            delta = 0f
        }

        if (delta == 0f) {
            return Int.MAX_VALUE
        }

        if (delta >= 1f) {
            return 0
        }

        return ceil(1 / delta).toInt()
    }

    enum class Material {
        AIR,
        WOOD,
        ROCK,
        PLANT,
        MELON,
        DIRT,
        WEB,
        WOOL,
        OTHER,
    }

    companion object {
        val AIR = Block(0, Material.AIR, "air", shape = null)
    }
}