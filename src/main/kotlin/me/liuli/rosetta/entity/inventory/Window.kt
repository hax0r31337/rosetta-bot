package me.liuli.rosetta.entity.inventory

import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.world.item.Item

class Window(val id: Int, sizeIn: Int, val name: String, val type: String) : Inventory() {

    var fallbackOffset = 9
    val properties = mutableMapOf<Int, Int>()
    var player: EntityClientPlayer? = null

    init {
        initStorage(sizeIn)
    }

    fun onOpen(player: EntityClientPlayer) {
        this.player = player
    }

    fun onClose() {
        this.player = null
    }

    override fun set(index: Int, item: Item) {
        if (index >= size) {
            println("$index $size")
            player ?: throw IllegalStateException("Unable to fallback slot(player=null)")
            player!!.inventory[index - size + fallbackOffset] = item
            return
        }
        super.set(index, item)
    }

    override fun get(index: Int): Item {
        if (index >= size) {
            player ?: throw IllegalStateException("Unable to fallback slot(player=null)")
            return player!!.inventory[index - size + fallbackOffset]
        }
        return super.get(index)
    }

    override var heldItem: Item
        get() = Item.AIR
        set(value) {}
    override var helmetItem: Item
        get() = Item.AIR
        set(value) {}
    override var chestplateItem: Item
        get() = Item.AIR
        set(value) {}
    override var leggingsItem: Item
        get() = Item.AIR
        set(value) {}
    override var bootsItem: Item
        get() = Item.AIR
        set(value) {}
}