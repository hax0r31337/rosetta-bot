package me.liuli.rosetta.entity.client

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.data.EnumBlockFacing
import me.liuli.rosetta.world.data.EnumGameMode

class PlayerController(val bot: MinecraftBot) {

    var forward = false
    var back = false
    var left = false
    var right = false
    var jump = false

    val forwardValue: Float
        get() = (if(forward) 1f else 0f) - (if(back) 1f else 0f)
    val strafeValue: Float
        get() = (if(right) 1f else 0f) - (if(left) 1f else 0f)

    var currentBreakingBlock: Vec3i? = null
        set(value) {
            if (field != null) {
                bot.protocol.dig(field!!.x, field!!.y, field!!.z, currentBreakingFacing ?: EnumBlockFacing.UP, 2)
            }
            if (value != null) {
                bot.protocol.dig(value!!.x, value!!.y, value!!.z, currentBreakingFacing ?: EnumBlockFacing.UP, 0)
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

        return true
    }
}