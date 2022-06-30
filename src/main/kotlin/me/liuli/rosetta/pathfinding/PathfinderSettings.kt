package me.liuli.rosetta.pathfinding

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.world.WorldIdentifier
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.block.ComplexShape
import me.liuli.rosetta.world.item.Item

abstract class PathfinderSettings(val bot: MinecraftBot, val identifier: WorldIdentifier,
    /**
     * Boolean to allow breaking blocks
     */
    var canDig: Boolean = true,
    /**
     * Additional cost for breaking blocks.
     */
    var digCost: Float = 1f,
    /**
     * Additional cost for placing blocks.
     */
    var placeCost: Float = 1f,
    /**
     * Additional cost for interacting with liquids.
     */
    var liquidCost: Float = 1f,
    /**
     * Do not break blocks that touch liquid blocks.
     */
    var dontCreateFlow: Boolean = true,
    /**
     * Allow pillaring up on 1x1 towers.
     */
    var allow1by1Towers: Boolean = true,
    /**
     * Allow to walk to the next node/goal in a strait line if terrain allows it.
     */
    var allowFreeMotion: Boolean = true,
    /**
     * Allow parkour jumps like jumps over gaps bigger then 1 block
     */
    var allowParkour: Boolean = true,
    /**
     * Allow sprinting when moving.
     */
    var allowSprinting: Boolean = true,
    /**
     * Do not break blocks that have gravityBlock above.
     */
    var dontMineUnderFallBlock: Boolean = true,
    /**
     * Max drop down distance. Only considers drops that have blocks to land on.
     */
    var maxDropDown: Float = 4f,
    /**
     * Option to ignore maxDropDown distance when the landing position is in water.
     */
    var infiniteLiquidDropdownDistance: Boolean = true,
    /**
     * Option to allow bot to open doors
     */
    var canOpenDoors: Boolean = true) {

    /**
     * is the block can be broken by the bot when [canDig] is enabled
     */
    abstract fun canBreakBlock(block: Block): Boolean

    /**
     * is the bot need to avoid with collide with the block
     */
    abstract fun needAvoidBlock(block: Block): Boolean

    /**
     * is the block can be replaced during bot bridging
     */
    abstract fun replaceableBlock(block: Block): Boolean

    /**
     * is the item can be used during bot bridging
     */
    abstract fun bridgeableItem(item: Item): Boolean

    /**
     * get height of the block
     */
    open fun getBlockHeight(block: Block): Double {
        val shape = block.shape
        return shape?.maxY ?: .0
    }

    /**
     * is the block can be walk on safely (like height smaller than 0.1)
     */
    open fun isSafeToWalkOn(block: Block): Boolean {
        val height = getBlockHeight(block)
        return height <= 0.1
    }

    /**
     * is the block is fence liked (height over 1)
     */
    open fun isFenceLikedBlock(block: Block): Boolean {
        val height = getBlockHeight(block)
        return height > 0.1
    }

    /**
     * is the block is full block
     */
    open fun isFullCube(block: Block): Boolean {
        val shape = block.shape ?: return false
        return shape.minX == .0 && shape.minY == .0 && shape.minZ == .0 && shape.maxX == 1.0 && shape.maxY == 1.0 && shape.maxZ == 1.0
    }

    open fun getBlockAt(x: Int, y: Int, z: Int): PathBlock {
        val block = bot.world.getBlockAt(x, y, z)
            ?: return PathBlock(x, y, z, Block.AIR, false, false, false, false, false, false, .0, false)
        return PathBlock(x, y, z, block, replaceableBlock(block), identifier.isGravityBlock(block),
            !needAvoidBlock(block) && (isSafeToWalkOn(block) || identifier.isClimbable(block)),
            isFullCube(block), identifier.getWaterDepth(block) != -1 || identifier.isLava(block),
            identifier.isClimbable(block), getBlockHeight(block), identifier.isOpenableDoor(block))
    }

    /**
     * Takes into account if the block is within a break exclusion area.
     */
    open fun safeToBreak(block: PathBlock): Boolean {
        if (!this.canDig) return false

        if (this.dontCreateFlow) {
            if (this.getBlockAt(block.x, block.y + 1, block.z).liquid) return false
            if (this.getBlockAt(block.x - 1, block.y, block.z).liquid) return false
            if (this.getBlockAt(block.x + 1, block.y, block.z).liquid) return false
            if (this.getBlockAt(block.x, block.y, block.z - 1).liquid) return false
            if (this.getBlockAt(block.x, block.y, block.z + 1).liquid) return false
        }

        if (this.dontMineUnderFallBlock) {
            if (this.getBlockAt(block.x, block.y + 1, block.z).canFall) return false
        }

        return !this.canBreakBlock(block.block)
    }

    /**
     * Takes into account if the block is within the stepExclusionAreas. And returns 100 if a block to be broken is within break exclusion areas.
     * @return cost
     */
    open fun safeOrBreak(block: PathBlock, toBreak: MutableList<Block>): Float {
        if (block.safe) return 0f
        if (!this.safeToBreak(block)) return 100f
        // TODO: process block break
        return 100f
    }

    data class PathBlock(val x: Int, val y: Int, val z: Int, val block: Block,
                         val replaceable: Boolean, val canFall: Boolean, val safe: Boolean,
                         val physical: Boolean, val liquid: Boolean, val climbable: Boolean,
                         val height: Double, val openable: Boolean)
}