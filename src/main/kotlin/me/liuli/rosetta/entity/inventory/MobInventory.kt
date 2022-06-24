package me.liuli.rosetta.entity.inventory

import me.liuli.rosetta.world.item.Item

class MobInventory : Inventory() {

    init {
        initStorage(5)
    }

    override var heldItem: Item
        get() = storage[0]
        set(value) { storage[0] = value }
    override var helmetItem: Item
        get() = storage[1]
        set(value) { storage[1] = value }
    override var chestplateItem: Item
        get() = storage[2]
        set(value) { storage[2] = value }
    override var leggingsItem: Item
        get() = storage[3]
        set(value) { storage[3] = value }
    override var bootsItem: Item
        get() = storage[4]
        set(value) { storage[4] = value }
}