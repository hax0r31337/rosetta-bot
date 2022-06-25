package me.liuli.rosetta.world

import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.entity.EntityLiving
import me.liuli.rosetta.world.block.Block

interface WorldIdentifier {

    /**
     * get water depth, contains water logged blocks
     * @return depth level, up to 8, return -1 when unable to fetch depth
     */
    fun getWaterDepth(block: Block): Int

    /**
     * @return true when the block is any type of lava
     */
    fun isLava(block: Block): Boolean

    /**
     * @return true when the block is honey block
     */
    fun isHoneyBlock(block: Block): Boolean

    /**
     * @return jump boost level, 0 if don't have
     */
    fun jumpBoostLevel(entity: EntityLiving): Int

    /**
     * @return depth strider level, 0 if don't have
     */
    fun depthStrider(entity: EntityLiving): Int

    /**
     * @return dolphin's grace level, 0 if don't have
     */
    fun dolphinsGrace(entity: EntityLiving): Int

    /**
     * @return slow falling level, 0 if don't have
     */
    fun slowFalling(entity: EntityLiving): Int
}