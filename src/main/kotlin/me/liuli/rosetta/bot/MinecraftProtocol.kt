package me.liuli.rosetta.bot

import me.liuli.rosetta.entity.inventory.EnumInventoryAction
import me.liuli.rosetta.world.data.EnumBlockFacing
import me.liuli.rosetta.world.item.Item
import java.net.Proxy

interface MinecraftProtocol {

    /**
     * setup protocol handler to callback packet received
     */
    fun setHandler(handler: BotProtocolHandler)

    /**
     * connect to server
     * @param host server host
     * @param port server port
     * @param proxy proxy to use
     */
    fun connect(host: String, port: Int, proxy: Proxy)

    /**
     * disconnect from server
     */
    fun disconnect()

    // packet sending

    /**
     * swing arm
     */
    fun swingItem()

    /**
     * send chat message to the server
     * @param message chat message
     */
    fun chat(message: String)

    /**
     * like onUpdateWalkingPlayer in Vanilla Minecraft, called every tick
     */
    fun move(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, onGround: Boolean, sprint: Boolean, sneak: Boolean)

    /**
     * call this insteadof [move] when player is on vehicle
     */
    fun moveVehicle(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, moveStrafe: Float, moveForward: Float, pressJump: Boolean, pressSneak: Boolean)

    /**
     * switch the current item
     * @param slot slot index
     */
    fun heldItemChange(slot: Int)

    /**
     * stream player abilities to server
     */
    fun abilities(invincible: Boolean, flying: Boolean, allowFlying: Boolean, walkSpeed: Float, flySpeed: Float)

    /**
     * dig block
     * mode 0: start digging
     * mode 1: abort digging
     * mode 2: stop digging
     */
    fun dig(x: Int, y: Int, z: Int, facing: EnumBlockFacing, mode: Int)

    /**
     * use item on air
     */
    fun useItem()

    /**
     * use item on block
     */
    fun useItem(x: Int, y: Int, z: Int, facing: EnumBlockFacing)

    /**
     * use item on entity
     * mode 0: attack
     * mode 1: interact
     * mode 2: interact at
     */
    fun useItem(entityId: Int, mode: Int)

    /**
     * request respawn
     */
    fun respawn()

    /**
     * response sign edit
     */
    fun signEdit(content: Array<String>, x: Int, y: Int, z: Int)

    /**
     * click on inventory
     */
    fun windowClick(window: Int, slot: Int, action: EnumInventoryAction)

    /**
     * creative inventory item pick
     */
    fun creativePickUp(slot: Int, item: Item)

    /**
     * open inventory
     */
    fun openWindow()

    /**
     * close inventory
     */
    fun closeWindow(id: Int)

}