package me.liuli.rosetta.world.block

import com.google.gson.JsonObject
import me.liuli.rosetta.world.item.Item

data class Block(val id: Int, val material: Material, val name: String,
                 val hardness: Float = 0f, val diggable: Boolean = true, val harvertLimit: Array<Int>? = null,
                 val boundingBox: AxisAlignedBB? = AxisAlignedBB.SHAPE_BLOCK) {

    val hardnessModifier = mutableListOf<IBlockHardnessModifier>()

    var additionalData: JsonObject? = null

    fun getHardness(item: Item): Float {
        var hardness = this.hardness
        hardnessModifier.forEach { hardness = it.getModifier(hardness, item) }
        return hardness
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
        val AIR = Block(0, Material.AIR, "air", boundingBox = null)
    }
}