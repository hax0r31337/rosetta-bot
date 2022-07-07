package me.liuli.rosetta.entity.inventory

import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.world.item.Item

class PlayerInventory(private val player: EntityClientPlayer) : Inventory() {

    var heldItemSlot = 36
    var helmetItemSlot = 5
    var chestplateItemSlot = 6
    var leggingsItemSlot = 7
    var bootsItemSlot = 8

    override var heldItem: Item
        get() = storage[heldItemSlot + player.heldItemSlot]
        set(value) { storage[heldItemSlot + player.heldItemSlot] = value }
    override var helmetItem: Item
        get() = storage[helmetItemSlot]
        set(value) { storage[helmetItemSlot] = value }
    override var chestplateItem: Item
        get() = storage[chestplateItemSlot]
        set(value) { storage[chestplateItemSlot] = value }
    override var leggingsItem: Item
        get() = storage[leggingsItemSlot]
        set(value) { storage[leggingsItemSlot] = value }
    override var bootsItem: Item
        get() = storage[bootsItemSlot]
        set(value) { storage[bootsItemSlot] = value }

    /**
     * search item on hotbar
     */
    fun searchHotbar(func: (Item) -> Boolean): Int? {
        return this.searchInIndex(heldItemSlot, heldItemSlot+8, func)
    }
}