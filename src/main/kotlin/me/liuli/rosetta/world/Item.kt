package me.liuli.rosetta.world

import com.google.gson.JsonObject

class Item(var id: Int, var count: Int, var damage: Int, val type: Type) {

    var additionalData: JsonObject? = null

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