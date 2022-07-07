package me.liuli.rosetta.entity.inventory

import me.liuli.rosetta.world.item.Item

abstract class Inventory {

    var size = 0
        protected set
    val storage = mutableListOf<Item>()

    open fun initStorage(size: Int) {
        storage.clear()
        for (i in 0 until size) {
            storage.add(i, Item.AIR)
        }
        this.size = size
    }

    open operator fun set(index: Int, item: Item) {
        if (index >= size) {
            throw IndexOutOfBoundsException(index)
        }
        storage[index] = item
    }

    open operator fun get(index: Int): Item {
        if (index >= size) {
            throw IndexOutOfBoundsException(index)
        }
        return storage[index]
    }

    open operator fun set(equipment: EnumEquipment, item: Item) {
        when(equipment) {
            EnumEquipment.HAND -> heldItem = item
            EnumEquipment.HELMET -> helmetItem = item
            EnumEquipment.CHESTPLATE -> helmetItem = item
            EnumEquipment.LEGGINGS -> helmetItem = item
            EnumEquipment.BOOTS -> helmetItem = item
        }
    }

    open fun searchInIndex(from: Int, to: Int, func: (Item) -> Boolean): Int? {
        for(i in from..to) {
            if (func(this[i])) {
                return i
            }
        }
        return null
    }

    // get frequency used items
    abstract var heldItem: Item
    abstract var helmetItem: Item
    abstract var chestplateItem: Item
    abstract var leggingsItem: Item
    abstract var bootsItem: Item
}