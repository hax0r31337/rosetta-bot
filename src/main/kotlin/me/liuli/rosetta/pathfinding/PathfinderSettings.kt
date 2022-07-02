package me.liuli.rosetta.pathfinding

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.pathfinding.path.PathBlock
import me.liuli.rosetta.pathfinding.path.PathBreakInfo
import me.liuli.rosetta.pathfinding.path.PathPlaceInfo
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.WorldIdentifier
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.item.Item
import kotlin.math.sqrt

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
     * @return a positive number (includes 0) that defines extra cost for that specific Block.
     * 0 means no extra cost, 100 means it is impossible for pathfinder to consider this move.
     */
    open fun exclusionBreak(block: PathBlock) = 0f

    /**
     * @return a positive number (includes 0) that defines extra cost for that specific Block.
     * 0 means no extra cost, 100 means it is impossible for pathfinder to consider this move.
     */
    open fun exclusionStep(block: PathBlock) = 0f

    /**
     * @return a positive number (includes 0) that defines extra cost for that specific Block.
     * 0 means no extra cost, 100 means it is impossible for pathfinder to consider this move.
     */
    open fun exclusionPlace(block: PathBlock) = 0f

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
            identifier.isClimbable(block), y +
                    getBlockHeight(block), identifier.isOpenableDoor(block))
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

        return !this.canBreakBlock(block.block) && this.exclusionBreak(block) < 100
    }

    /**
     * Takes into account if the block is within the stepExclusionAreas. And returns 100 if a block to be broken is within break exclusion areas.
     * @return cost
     */
    open fun safeOrBreak(block: PathBlock, toBreak: MutableList<PathBreakInfo>): Float {
        if (block.safe) return 0f
        if (!this.safeToBreak(block)) return 100f
        toBreak.add(PathBreakInfo(block))
        val digTime = block.block.digTime(bot.player.inventory.heldItem, false, false)
        return (1 + digTime * 0.003f) * this.digCost
    }

    open fun getMoveJumpUp(node: Move, dir: Pair<Int, Int>, neighbors: MutableList<Move>) {
        val blockA = getBlockAt(node.x, node.y + 2, node.z)
        val blockH = getBlockAt(node.x + dir.first, node.y + 2, node.z + dir.second)
        val blockB = getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second)
        val blockC = getBlockAt(node.x + dir.first, node.y, node.z + dir.second)

        var cost = 2f
        val toBreak = mutableListOf<PathBreakInfo>()
        val toPlace = mutableListOf<PathPlaceInfo>()

        if (!blockC.physical) {
            if (node.remainingBlocks == 0) return

            val blockD = getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second)
            if (!blockD.physical) {
                if (node.remainingBlocks == 1) return

                if (!blockD.replaceable) {
                    if (!safeToBreak(blockD)) return
                    cost += exclusionBreak(blockD)
                    toBreak.add(PathBreakInfo(blockD))
                }
                cost += exclusionPlace(blockD)
                toPlace.add(PathPlaceInfo(node.x, node.y - 1, node.z, dir.first, 0, dir.second, Vec3i(node.x, node.y, node.z)))
                cost += this.placeCost
            }

            if (!blockC.replaceable) {
                if (!safeToBreak(blockC)) return
                cost += exclusionBreak(blockC)
                toBreak.add(PathBreakInfo(blockC))
            }
            cost += exclusionBreak(blockC)
            toPlace.add(PathPlaceInfo(node.x + dir.first, node.y - 1, node.z + dir.second, 0, 1, 0))
            cost += this.placeCost

            blockC.height += 1
        }

        val block0 = getBlockAt(node.x, node.y - 1, node.z)
        if (blockC.height - block0.height > 1.2) return

        cost += safeOrBreak(blockA, toBreak)
        if (cost > 100) return
        cost += safeOrBreak(blockH, toBreak)
        if (cost > 100) return
        cost += safeOrBreak(blockB, toBreak)
        if (cost > 100) return

        neighbors.add(Move(blockB.x, blockB.y, blockB.z, node.remainingBlocks - toPlace.size, cost, toBreak, toPlace))
    }

    open fun getMoveForward(node: Move, dir: Pair<Int, Int>, neighbors: MutableList<Move>) {
        val blockB = getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second)
        val blockC = getBlockAt(node.x + dir.first, node.y, node.z + dir.second)
        val blockD = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)

        var cost = 1f
        cost += exclusionStep(blockC)

        val toBreak = mutableListOf<PathBreakInfo>()
        val toPlace = mutableListOf<PathPlaceInfo>()

        if(!blockD.physical && !blockC.liquid) {
            if (node.remainingBlocks == 0) return

            if (!blockD.replaceable) {
                if (!safeToBreak(blockD)) return
                cost += exclusionBreak(blockD)
                toBreak.add(PathBreakInfo(blockD))
            }
            cost += exclusionPlace(blockC)
            toPlace.add(PathPlaceInfo(node.x, node.y - 1, node.z, dir.first, 0, dir.second))
            cost += this.placeCost
        }

        cost += safeOrBreak(blockB, toBreak)
        if (cost > 100) return

        // Open fence gates
        if (this.canOpenDoors && blockC.openable && blockC.block.shape != null) {
            toPlace.add(PathPlaceInfo(node.x + dir.first, node.y, node.z + dir.second, 0, 0, 0, useOne = true )) // Indicate that a block should be used on this block not placed
        } else {
            cost += safeOrBreak(blockC, toBreak)
            if (cost > 100) return
        }

        if (getBlockAt(node.x, node.y, node.z).liquid) cost += this.liquidCost
        neighbors.add(Move(blockC.x, blockC.y, blockC.z, node.remainingBlocks - toPlace.size, cost, toBreak, toPlace))
    }

    open fun getMoveDiagonal(node: Move, dir: Pair<Int, Int>, neighbors: MutableList<Move>) {
        var cost = sqrt(2f)
        val toBreak = mutableListOf<PathBreakInfo>()

        val blockC = getBlockAt(node.x + dir.first, node.y, node.z + dir.second)
        val y = if (blockC.physical) 1 else 0

        val block0 = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)

        var cost1 = 0f
        val toBreak1 = mutableListOf<PathBreakInfo>()
        val blockB1 = getBlockAt(node.x, node.y + y + 1, node.z + dir.second)
        val blockC1 = getBlockAt(node.x, node.y + y, node.z + dir.second)
        val blockD1 = getBlockAt(node.x, node.y + y - 1, node.z + dir.second)
        cost1 += safeOrBreak(blockB1, toBreak1)
        cost1 += safeOrBreak(blockC1, toBreak1)
        if (blockD1.height - block0.height > 1.2) cost1 += safeOrBreak(blockD1, toBreak1)

        var cost2 = 0f
        val toBreak2 = mutableListOf<PathBreakInfo>()
        val blockB2 = getBlockAt(node.x + dir.first, node.y + y + 1, node.z)
        val blockC2 = getBlockAt(node.x + dir.first, node.y + y, node.z)
        val blockD2 = getBlockAt(node.x + dir.first, node.y + y - 1, node.z)
        cost2 += safeOrBreak(blockB2, toBreak2)
        cost2 += safeOrBreak(blockC2, toBreak2)
        if (blockD2.height - block0.height > 1.2) cost2 += safeOrBreak(blockD2, toBreak2)

        if (cost1 > cost2) {
            cost += cost1
            toBreak.addAll(toBreak1)
        } else {
            cost += cost2
            toBreak.addAll(toBreak2)
        }
        if (cost > 100) return
        cost += safeOrBreak(getBlockAt(node.x + dir.first, node.y + y, node.z + dir.second), toBreak)
        if (cost > 100) return
        cost += safeOrBreak(getBlockAt(node.x + dir.first, node.y + y + 1, node.z + dir.second), toBreak)
        if (cost > 100) return

        if (getBlockAt(node.x, node.y, node.z).liquid) cost += this.liquidCost

        val blockD = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)
        if (y == 1) { // Case jump up by 1
            if (blockC.height - block0.height > 1.2) return
            cost += safeOrBreak(getBlockAt(node.x, node.y + 2, node.z), toBreak)
            if (cost > 100) return
            cost += 1
            neighbors.add(Move(blockC.x, blockC.y + 1, blockC.z, node.remainingBlocks, cost, toBreak = toBreak))
        } else if (blockD.physical || blockC.liquid) {
            neighbors.add(Move(blockC.x, blockC.y, blockC.z, node.remainingBlocks, cost, toBreak = toBreak))
        } else if (getBlockAt(node.x + dir.first, node.y - 2, node.z + dir.second).physical || blockD.liquid) {
            if (!blockD.safe) return // don't self-immolate
            neighbors.add(Move(blockC.x, blockC.y - 1, blockC.z, node.remainingBlocks, cost, toBreak = toBreak))
        }
    }

    open fun getLandingBlock(node: Move, dir: Pair<Int, Int>): PathBlock? {
        var blockLand = getBlockAt(node.x + dir.first, node.y - 2, node.z + dir.second)
        while (blockLand.y > 0) {
            if (blockLand.liquid && blockLand.safe) return blockLand
            if (blockLand.physical) {
                if (node.y - blockLand.y <= this.maxDropDown) return getBlockAt(blockLand.x, blockLand.y + 1, blockLand.z)
                return null
            }
            if (!blockLand.safe) return null
            blockLand = getBlockAt(blockLand.x, blockLand.y - 1, blockLand.z)
        }
        return null
    }

    open fun getMoveDropDown(node: Move, dir: Pair<Int, Int>, neighbors: MutableList<Move>) {
        val blockB = getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second)
        val blockC = getBlockAt(node.x + dir.first, node.y, node.z + dir.second)
        val blockD = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)

        var cost = 1f
        val toBreak = mutableListOf<PathBreakInfo>()

        val blockLand = getLandingBlock(node, dir) ?: return
        if (!this.infiniteLiquidDropdownDistance && (node.y - blockLand.y) > this.maxDropDown) return

        cost += this.safeOrBreak(blockB, toBreak)
        if (cost > 100) return
        cost += this.safeOrBreak(blockC, toBreak)
        if (cost > 100) return
        cost += this.safeOrBreak(blockD, toBreak)
        if (cost > 100) return
        if (blockC.liquid) return

        neighbors.add(Move(blockLand.x, blockLand.y, blockLand.z, node.remainingBlocks, cost, toBreak))
    }

    open fun getMoveDown(node: Move, neighbors: MutableList<Move>) {
        val block0 = getBlockAt(node.x, node.y - 1, node.z)

        var cost = 1f
        val toBreak = mutableListOf<PathBreakInfo>()
        val blockLand = getLandingBlock(node, 0 to 0) ?: return

        cost += this.safeOrBreak(block0, toBreak)
        if (cost > 100) return

        if (getBlockAt(node.x, node.y, node.z).liquid) return

        neighbors.add(Move(blockLand.x, blockLand.y, blockLand.z, node.remainingBlocks, cost, toBreak))
    }

    open fun getMoveUp(node: Move, neighbors: MutableList<Move>) {
        val block1 = getBlockAt(node.x, node.y, node.z)
        if (block1.liquid) return

        val block2 = getBlockAt(node.x, node.y + 2, node.z)
        var cost = 1f

        val toBreak = mutableListOf<PathBreakInfo>()
        val toPlace = mutableListOf<PathPlaceInfo>()

        cost += this.safeOrBreak(block2, toBreak)
        if (cost > 100) return

        if (!block1.climbable) {
            if (!this.allow1by1Towers || node.remainingBlocks == 0) return

            if (!block1.replaceable) {
                if (!this.safeToBreak(block1)) return
                toBreak.add(PathBreakInfo(block1))
            }

            val block0 = getBlockAt(node.x, node.y - 1, node.z)
            if (block0.physical && block0.height - node.y < -0.2) return

            cost += this.exclusionPlace(block1)
            toPlace.add(PathPlaceInfo(node.x, node.y - 1, node.z, 0, 1, 0, jump = true))
            cost += this.placeCost
        }

        if (cost > 100) return

        neighbors.add(Move(node.x, node.y + 1, node.z, node.remainingBlocks - toPlace.size, cost, toBreak, toPlace))
    }

    open fun getMoveParkourForward(node: Move, dir: Pair<Int, Int>, neighbors: MutableList<Move>) {
        val block0 = getBlockAt(node.x, node.y - 1, node.z)
        val block1 = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)
        if ((block1.physical && block1.height >= block0.height) ||
                !getBlockAt(node.x + dir.first, node.y, node.z + dir.second).safe ||
            !getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second).safe) return

        if (getBlockAt(node.x, node.y, node.z).liquid) return // can't jump from water

        // if we have a block on the ceiling, we cannot jump but we can still fall
        var ceilingClear = getBlockAt(node.x, node.y + 2, node.z).safe && getBlockAt(node.x + dir.first, node.y + 2, node.z + dir.second).safe

        // similarly for the down path
        var floorCleared = !getBlockAt(node.x + dir.first, node.y - 2, node.z + dir.second).physical

        val maxD = if(this.allowSprinting) 4 else 2

        for (d in 2..maxD) {
            val dx = dir.first * d
            val dz = dir.second * d
            val blockA = getBlockAt(node.x + dx, node.y + 2, node.z + dz)
            val blockB = getBlockAt(node.x + dx, node.y + 1, node.z + dz)
            val blockC = getBlockAt(node.x + dx, node.y + 0, node.z + dz)
            val blockD = getBlockAt(node.x + dx, node.y - 1, node.z + dz)

            if (ceilingClear && blockB.safe && blockC.safe && blockD.physical) {
                // forward
                neighbors.add(Move(blockC.x, blockC.y, blockC.z, node.remainingBlocks, this.exclusionStep(blockB) + 1, parkour = true))
                break
            } else if (ceilingClear && blockB.safe && blockC.physical) {
                // up
                if (blockA.safe) {
                    if (blockC.height - block0.height > 1.2) break // Too high to jump
                    neighbors.add(Move(blockB.x, blockB.y, blockB.z, node.remainingBlocks, this.exclusionStep(blockA) + 1, parkour = true))
                    break
                }
            } else if ((ceilingClear || d === 2) && blockB.safe && blockC.safe && blockD.safe && floorCleared) {
                val blockE = getBlockAt(node.x + dx, node.y - 1, node.z + dz)
                if (blockE.physical) {
                    neighbors.add(Move(blockE.x, blockE.y, blockE.z, node.remainingBlocks, this.exclusionStep(blockD) + 1, parkour = true))
                }
                floorCleared = !blockE.physical
            } else if (!blockB.safe || !blockC.safe) {
                break
            }

            ceilingClear = ceilingClear && blockA.safe
        }
    }

    open fun getNeighbors(node: Move): MutableList<Move> {
        val neighbors = mutableListOf<Move>()

        for (dir in cardinalDirections) {
            this.getMoveForward(node, dir, neighbors)
            this.getMoveJumpUp(node, dir, neighbors)
            this.getMoveDropDown(node, dir, neighbors)
            if (this.allowParkour) {
                this.getMoveParkourForward(node, dir, neighbors)
            }
        }

        for (dir in diagonalDirections) {
            this.getMoveDiagonal(node, dir, neighbors)
        }

        this.getMoveDown(node, neighbors)
        this.getMoveUp(node, neighbors)

        return neighbors
    }

    companion object {
        val cardinalDirections = arrayOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val diagonalDirections = arrayOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
    }
}