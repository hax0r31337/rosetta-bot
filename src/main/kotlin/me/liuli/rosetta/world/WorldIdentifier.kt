package me.liuli.rosetta.world

import me.liuli.rosetta.entity.EntityLiving
import me.liuli.rosetta.world.block.Block

interface WorldIdentifier {

    /**
     * @return player can climb ladders and vines by pressing jump, implemented in 1.14+
     */
    val climbUsingJump: Boolean

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
     * https://www.mcpk.wiki/w/index.php?title=Slipperiness
     * @return slipperiness of the target block
     */
    fun getSlipperiness(block: Block): Float

    /**
     * check if the target block is climbable (etc. Ladder, Vine)
     * @return is climbable or not
     */
    fun isClimbable(block: Block): Boolean

    /**
     * @return jump boost level, 0 if don't have
     */
    fun jumpBoostLevel(entity: EntityLiving): Int

    /**
     * @return depth strider level, 0 if don't have
     */
    fun depthStriderEnchantLevel(entity: EntityLiving): Int

    /**
     * @return dolphin's grace level, 0 if don't have
     */
    fun dolphinsGraceLevel(entity: EntityLiving): Int

    /**
     * @return slow falling level, 0 if don't have
     */
    fun slowFallingLevel(entity: EntityLiving): Int

    /**
     * @return levitation level, 0 if don't have
     */
    fun levitationLevel(entity: EntityLiving): Int
}