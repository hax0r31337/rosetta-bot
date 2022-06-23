package me.liuli.rosetta.world.item

import com.google.gson.JsonObject

data class Item(val id: Int, var count: Int, var damage: Int, val type: Type) {

    var compoundTag: JsonObject? = null

    enum class Type {
        SWORD,
        PICKAXE,
        AXE,
        SHOVEL,
        HOE,
        BOW,
        ARROW,
        BOOTS,
        LEGGINGS,
        CHESTPLATE,
        HELMET,
        BLOCK,
        ITEM
    }
}