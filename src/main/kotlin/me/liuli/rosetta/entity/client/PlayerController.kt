package me.liuli.rosetta.entity.client

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.util.getEyesLocation
import me.liuli.rosetta.util.getRotationOf
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.data.EnumBlockFacing
import me.liuli.rosetta.world.data.EnumGameMode

class PlayerController(val bot: MinecraftBot) : PlayerInput() {

    private var currentBreakingBlock: Vec3i? = null
        set(value) {
            if (field != null) {
                bot.protocol.dig(field!!.x, field!!.y, field!!.z, currentBreakingFacing ?: EnumBlockFacing.UP, 2)
            }
            if (value != null) {
                bot.protocol.dig(value.x, value.y, value.z, currentBreakingFacing ?: EnumBlockFacing.UP, 0)
            }
            field = value
        }
    private var currentBreakingFacing: EnumBlockFacing? = null
    private var currentBreakFinishTick = 0L
    val isDigging: Boolean
        get() = currentBreakingBlock != null

    fun tick() {
        if (currentBreakingBlock != null) {
            bot.protocol.swingItem()
            if (bot.world.tickExisted > currentBreakFinishTick) {
                currentBreakingBlock = null
                currentBreakingFacing = null
                currentBreakFinishTick = 0
            }
        }
    }

    /**
     * break a block at [x] [y] [z]
     * @return able to break the block or not
     */
    fun breakBlock(x: Int, y: Int, z: Int, rotate: Boolean = true): Boolean {
        val block = bot.world.getBlockAt(x, y, z) ?: return false
        if (!block.diggable) return false

        val breakTicks = if(bot.world.gamemode == EnumGameMode.CREATIVE) 0 else block.digTime(bot.player.inventory.heldItem, false, !bot.player.onGround)
        if (breakTicks == Int.MAX_VALUE) {
            return false // unable to break the target block
        }

        currentBreakingFacing = EnumBlockFacing.calculateFacing(bot.player, x, y, z)
        currentBreakingBlock = Vec3i(x, y, z)
        currentBreakFinishTick = bot.world.tickExisted + breakTicks

        if (rotate) {
            this.lookAt(x, y, z)
        }

        return true
    }

    /**
     * abort current breaking block
     */
    fun abortBreaking() {
        currentBreakingBlock = null
    }

    /**
     * request server to jump, if you want to jump please set [jump] to true
     */
    fun packetJump() {
        bot.protocol.jump()
    }

    /**
     * look at position
     */
    fun lookAt(vec3d: Vec3d) {
        val rotation = getRotationOf(vec3d, getEyesLocation(bot.player))
        bot.player.rotation.x = rotation.first
        bot.player.rotation.y = rotation.second
    }

    /**
     * look at block
     */
    fun lookAt(vec3i: Vec3i) {
        lookAt(Vec3d(vec3i.x + 0.5, vec3i.y + 0.5, vec3i.z + 0.5))
    }

    /**
     * look at block
     */
    fun lookAt(x: Int, y: Int, z: Int) {
        lookAt(Vec3d(x + 0.5, y + 0.5, z + 0.5))
    }

    /**
     * use item on block (a.k.a. place block)
     */
    fun useOnBlock(x: Int, y: Int, z: Int, rotate: Boolean = true) {
        bot.protocol.useItem(x, y, z, EnumBlockFacing.calculateFacing(bot.player, x, y, z))

        if (rotate) {
            lookAt(x, y, z)
        }
    }

    /**
     * use item on block (a.k.a. place block)
     */
    fun useOnBlock(x: Int, y: Int, z: Int, facing: EnumBlockFacing, rotate: Boolean = true) {
        bot.protocol.useItem(x, y, z, facing)

        if (rotate) {
            lookAt(x, y, z)
        }
    }
}