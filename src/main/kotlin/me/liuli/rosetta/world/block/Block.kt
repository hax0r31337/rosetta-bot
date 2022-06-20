package me.liuli.rosetta.world.block

import com.google.gson.JsonObject

data class Block(val id: Int, val data: Int, val type: Type) {

    var additionalData: JsonObject? = null

    enum class Type {
        AIR,
        FULL_SOLID,
        SOLID,
        FLUID,
        PASSABLE,
    }
}