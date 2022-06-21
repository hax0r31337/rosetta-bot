package me.liuli.rosetta.world.block

import com.google.gson.JsonObject

data class Block(val id: Int, val material: Material, val name: String,
                 val hardness: Float = 0f, val diggable: Boolean = true, val harvertLimit: Array<Int>? = null,
                 val boundingBox: AxisAlignedBB? = AxisAlignedBB.SHAPE_BLOCK) {

    var additionalData: JsonObject? = null

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