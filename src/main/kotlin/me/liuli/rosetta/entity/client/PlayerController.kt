package me.liuli.rosetta.entity.client

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.util.getEyesLocation
import me.liuli.rosetta.util.getRotationOf
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.data.EnumBlockFacing
import me.liuli.rosetta.world.data.EnumGameMode

class PlayerController(val bot: MinecraftBot) : PlayerInput() {

    var currentBreakingBlock: Vec3i? = null
        set(value) {
            if (field != null) {
                bot.protocol.dig(field!!.x, field!!.y, field!!.z, currentBreakingFacing ?: EnumBlockFacing.UP, 2)
            }
            if (value != null) {
                bot.protocol.dig(value.x, value.y, value.z, currentBreakingFacing ?: EnumBlockFacing.UP, 0)
            }
            field = value
        }
    var currentBreakingFacing: EnumBlockFacing? = null
    var currentBreakFinishTick = 0L

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
     * TODO: calculate [facing] by self
     */
    fun breakBlock(x: Int, y: Int, z: Int, facing: EnumBlockFacing, lookAt: Boolean = true): Boolean {
        val block = bot.world.getBlockAt(x, y, z) ?: return false
        if (!block.diggable) return false

        val breakTicks = if(bot.world.gamemode == EnumGameMode.CREATIVE) 0 else block.digTime(bot.player.inventory.heldItem, false, !bot.player.onGround)
        if (breakTicks == Int.MAX_VALUE) {
            return false // unable to break the target block
        }
        currentBreakingFacing = facing
        currentBreakingBlock = Vec3i(x, y, z)
        currentBreakFinishTick = bot.world.tickExisted + breakTicks

        if (lookAt) {
            val rotation = getRotationOf(Vec3d(x + 0.5, y + 0.5, z + 0.5), getEyesLocation(bot.player))
            bot.player.rotation.x = rotation.first
            bot.player.rotation.y = rotation.second
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
}