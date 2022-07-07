package me.liuli.rosetta.pathfinding

import me.liuli.rosetta.bot.event.ListenerSet
import me.liuli.rosetta.bot.event.PreMotionEvent
import me.liuli.rosetta.bot.event.WorldBlockUpdateEvent
import me.liuli.rosetta.bot.event.WorldChunkLoadEvent
import me.liuli.rosetta.entity.move.Physics
import me.liuli.rosetta.pathfinding.algorithm.AStar
import me.liuli.rosetta.pathfinding.algorithm.PhysicSimulator
import me.liuli.rosetta.pathfinding.goals.IGoal
import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.pathfinding.path.PathPlaceInfo
import me.liuli.rosetta.util.getEyesLocation
import me.liuli.rosetta.util.getRotationOf
import me.liuli.rosetta.util.getViewVector
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.util.wrapAngleTo180
import me.liuli.rosetta.world.Chunk
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.block.ComplexShape
import me.liuli.rosetta.world.block.Shape
import me.liuli.rosetta.world.data.EnumBlockFacing
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor

class Pathfinder(val settings: PathfinderSettings, val physics: Physics) : ListenerSet() {

    private val simulator = PhysicSimulator(settings.bot, settings.identifier, physics.settings)
    private var stateGoal: IGoal? = null
    private var aStarContext: AStar? = null
    private var aStarTimedOut = false
    private var dynamicGoal = false
    private var path = mutableListOf<Move>()
    private var pathUpdated = false
    private var placing = false
    private var placingBlock: PathPlaceInfo? = null
    private var lastNodeTime = System.currentTimeMillis()
    private var returningPos: Vec3i? = null
    private var stopPathing = false

    init {
        settings.bot.registerListeners(*(this.listeners))
    }

    fun setGoal(goal: IGoal, dynamic: Boolean = false) {
        this.stateGoal = goal
        this.dynamicGoal = dynamic
        resetPath()
    }

    fun resetPath(clearState: Boolean = true) {
        if (settings.bot.controller.isDigging) {
            settings.bot.controller.abortBreaking()
        }
        placing = false
        pathUpdated = false
        aStarContext = null
        path.clear()
        if (stopPathing) stop()
        else if (clearState) settings.bot.controller.clearControlState()
    }

    fun stop() {
        stopPathing = false
        aStarContext = null
        fullStop()
    }

    fun fullStop() {
        settings.bot.controller.clearControlState()
        settings.bot.player.sprinting = false
        settings.bot.player.sneaking = false

        // Force horizontal velocity to 0 (otherwise inertia can move us too far)
        // Kind of cheaty, but the server will not tell the difference
        settings.bot.player.motion.also {
            it.x = .0
            it.z = .0
        }
        val position = settings.bot.player.position
        val blockX = floor(position.x) + 0.5
        val blockZ = floor(position.z) + 0.5

        // Make sure our bounding box don't collide with neighboring blocks
        // otherwise recenter the position
        if (abs(position.x - blockX) < 0.2) position.x = blockX
        if (abs(position.z - blockZ) < 0.2) position.z = blockZ
    }

    private fun postProcessPath(path: MutableList<Move>) {
        path.forEachIndexed { i, node ->
            if (node.toBreak.isNotEmpty() || node.toPlace.isNotEmpty()) return@forEachIndexed // TODO: break in original code
            val b = settings.bot.world.getBlockAt(node.x, node.y, node.z)
            if (b != null && (settings.identifier.getWaterDepth(b) != -1 ||
                        (settings.identifier.isClimbable(b) && i + 1 < path.size && path[i + 1].y < node.y))) {
                node.postX = node.x + 0.5
                node.postZ = node.z + 0.5
                return@forEachIndexed
            }
            var npY = node.y
            var np = b?.let { getPositionOnTopOf(it) }
            if (np == null) np = settings.bot.world.getBlockAt(node.x, node.y - 1, node.z)?.let { npY -= 1; getPositionOnTopOf(it) }
            if (np != null) {
                node.postX += np.x
                node.postY = npY + np.y
                node.postZ += np.z
            } else {
                node.postX = node.x + 0.5
                node.postY -= 1
                node.postZ = node.z + 0.5
            }
        }

        if (path.isEmpty()) {
            return
        }
        val newPath = mutableListOf<Move>()
        var lastNode = path[0]
        path.forEachIndexed { i, node ->
            if (i == 0) return@forEachIndexed
            if (abs(node.y - lastNode.y) > 0.5 || node.toBreak.isNotEmpty() || node.toPlace.isNotEmpty() || !simulator.canStraightLineBetween(lastNode, node)) {
                newPath.add(path[i - 1])
                lastNode = path[i - 1]
            }
        }
        newPath.add(path.last())

        path.clear()
        path.addAll(newPath)
    }

    /**
     * @returnthe average x/z position of the highest standing positions in the block.
     */
    private fun getPositionOnTopOf(block: Block): Vec3d? {
        if (block.shape == null) return null
        val p = Vec3d(0.5, .0, 0.5)
        var n = 1
        fun processShape(shape: Shape) {
            val h = shape.maxY
            if (h == p.y) {
                p.x += (shape.minX + shape.maxX) / 2
                p.z += (shape.minZ + shape.maxZ) / 2
                n++
            } else if (h > p.y) {
                n = 2
                p.x = 0.5 + (shape.minX + shape.maxX) / 2
                p.y = h
                p.z = 0.5 + (shape.minZ + shape.maxZ) / 2
            }
        }
        if (block.shape is ComplexShape) {
            for (shape in block.shape.shapes) {
                processShape(shape)
            }
        } else {
            processShape(block.shape)
        }
        p.x /= n
        p.z /= n
        return p
    }

    private fun moveToBlock(pos: Vec3i): Boolean {
        val targetPos = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
        if (settings.bot.player.position.distanceTo(targetPos) > 0.2) {
            val rotation = getRotationOf(targetPos, getEyesLocation(settings.bot.player))
            settings.bot.player.rotation.x = rotation.first
            settings.bot.player.rotation.y = rotation.second
            settings.bot.controller.forward = true
            return false
        }
        settings.bot.controller.forward = false
        return true
    }

    private fun moveToEdge(block: Vec3i, edgeX: Int, edgeZ: Int): Boolean {
        // Target viewing direction while approaching edge
        // The Bot approaches the edge while looking in the opposite direction from where it needs to go
        // The target Pitch angle is roughly the angle the bot has to look down for when it is in the position
        // to place the next block
        val targetPosDelta = settings.bot.player.position.copy().apply {
            x -= block.x + 0.5
            y -= block.y
            z -= block.z + 0.5
        }
        val targetYaw = wrapAngleTo180(Math.toDegrees(atan2(-targetPosDelta.x, targetPosDelta.z)).toFloat())
        val viewVector = getViewVector(targetYaw, -1.421f)
        // While the bot is not in the right position rotate the view and press back while crouching
        if (settings.bot.player.position.distanceTo(Vec3d(block.x + 0.5 + edgeX, 1.0, block.z + 0.5 + edgeZ)) > 0.4) {
            settings.bot.controller.lookAt(viewVector.apply {
                x += settings.bot.player.position.x
                y += settings.bot.player.position.y
                z += settings.bot.player.position.z
            })
            settings.bot.player.sneaking = true
            settings.bot.controller.back = true
            return false
        }
        settings.bot.controller.back = false
        return true
    }

    fun getPathTo(goal: IGoal): AStar.Result {
        val player = settings.bot.player
        val pos = player.position.floored()
        val dy = player.position.y - pos.y
        val block = settings.bot.world.getBlockAt(pos.x, pos.y, pos.z)

        val start = Move(pos.x, pos.y + if(block != null && dy > 0.001 && player.onGround) 1 else 0, pos.z,
            settings.countBridgeableItems(), 0f)
        aStarContext = AStar(start, settings, goal)
        return aStarContext!!.compute()
    }

    private fun pathFromPlayer(path: MutableList<Move>) {
        if (path.isEmpty()) return
        var minI = 0
        var minDistance = 1000.0
        var isBreak = false
        path.forEachIndexed { i, node ->
            if (isBreak || node.toBreak.isNotEmpty() || node.toPlace.isNotEmpty()) {
                isBreak = true
                return@forEachIndexed
            }
            val dist = settings.bot.player.position.distanceSq(node.postX, node.postY, node.postZ)
            if (dist < minDistance) {
                minDistance = dist
                minI = i
            }
        }
        // check if we are between 2 nodes
        val n1 = path[minI]
        // check if node already reached
        val dx = n1.postX - settings.bot.player.position.x
        val dy = n1.postY - settings.bot.player.position.y
        val dz = n1.postZ - settings.bot.player.position.z
        val reached = abs(dx) <= 0.35 && abs(dz) <= 0.35 && abs(dy) < 1
        if (minI + 1 < path.size && n1.toBreak.isEmpty() && n1.toPlace.isEmpty()) {
            val n2 = path[minI + 1]
            val pos2 = Vec3d(n2.postX, n2.postY, n2.postZ)
            val d2 = settings.bot.player.position.distanceSq(pos2.x, pos2.y, pos2.z)
            val d12 = pos2.distanceSq(n1.postX, n1.postY, n1.postZ)
            minI += if(d12 > d2 || reached) 1 else 0
        }
        path.map { it }.forEachIndexed { i, node ->
            if (i <= minI) {
                path.remove(node)
            }
        }
    }

    private fun isPositionNearPath(x: Int, y: Int, z: Int): Boolean {
        for(node in path) {
            val dx = node.postX - x - 0.5
            val dy = node.postY - y - 0.5
            val dz = node.postZ - z - 0.5
            if (dx <= 1 && dy <= 2 && dz <= 1) return true
        }
        return false
    }

    @Listen
    private fun onChunkLoad(event: WorldChunkLoadEvent) {
        aStarContext ?: return

        // Reset only if the new chunk is adjacent to a visited chunk
        val visited = aStarContext!!.visitedChunks
        if (visited.contains(Chunk.code(event.chunk.x - 1, event.chunk.z)) ||
            visited.contains(Chunk.code(event.chunk.x, event.chunk.z - 1)) ||
            visited.contains(Chunk.code(event.chunk.x + 1, event.chunk.z)) ||
            visited.contains(Chunk.code(event.chunk.x, event.chunk.z + 1)) ||
            visited.contains(Chunk.code(event.chunk.x, event.chunk.z))) {
            resetPath(false)
        }
    }

    @Listen
    private fun onBlockUpdate(event: WorldBlockUpdateEvent) {
        val oldBlock = settings.bot.world.getBlockAt(event.x, event.y, event.z)
        if (isPositionNearPath(event.x, event.y, event.z) && oldBlock?.id == event.block.id) {
            resetPath(false)
        }
    }

    @Listen
    private fun onPreMotion(event: PreMotionEvent) {
        if (stateGoal != null) {
            if (!stateGoal!!.isValid()) {
                stop()
            } else if (stateGoal!!.hasChanged()) {
                resetPath(false)
            }
        }
        if (aStarContext != null && aStarTimedOut) {
            val result = aStarContext!!.compute()
            postProcessPath(result.path)
            pathFromPlayer(result.path)
            path = result.path
            aStarTimedOut = result.status == AStar.ResultStatus.PARTIAL
        }
        if (settings.losWhenPlacingBlocks && returningPos != null) {
            if (!moveToBlock(returningPos!!)) return
            returningPos = null
        }
        if (path.isEmpty()) {
            lastNodeTime = System.currentTimeMillis()
            if (stateGoal == null) {
                return
            }
            if (stateGoal!!.isEnd(settings.bot.player.position.floored())) {
                if (!dynamicGoal) {
                    // goal reached
                    stateGoal = null
                    fullStop()
                }
            } else if (!pathUpdated) {
                val result = getPathTo(stateGoal!!)
                postProcessPath(result.path)
                path = result.path
                aStarTimedOut = result.status == AStar.ResultStatus.PARTIAL
                pathUpdated = true
            }
            if (path.isEmpty()) {
                return
            }
        }

        var nextPoint = path.first()
        if (settings.bot.controller.isDigging || nextPoint.toBreak.isNotEmpty()) {
            if (!settings.bot.controller.isDigging && settings.bot.player.onGround) {
                val toBreak = nextPoint.toBreak.first()
                val bestItemSlot = settings.bestHarvestItem(settings.bot.world.getBlockAt(toBreak.x, toBreak.y, toBreak.z) ?: Block.AIR)
                if (bestItemSlot != settings.bot.player.heldItemSlot) {
                    settings.bot.player.heldItemSlot = bestItemSlot
                    return
                }
                settings.bot.controller.breakBlock(toBreak.x, toBreak.y, toBreak.z)
                nextPoint.toBreak.remove(toBreak)
            }
            return
        }

        // TODO: sneak when placing or make sure the block is not interactive
        // TODO fix this
        if (placing || nextPoint.toPlace.isNotEmpty()) {
            if (!placing) {
                placing = true
                placingBlock = nextPoint.toPlace.first().also { nextPoint.toPlace.remove(it) }
                fullStop()
            }

            // Open gates or doors
            if (placingBlock!!.useOne) {
                settings.bot.controller.useOnBlock(placingBlock!!.x, placingBlock!!.y, placingBlock!!.z)
                placing = false
                placingBlock = null
                return
            }
            val block = settings.searchBridgeableItem()
            if (block == null) {
                resetPath()
                return
            }
            if (settings.bot.player.heldItemSlot != (block - settings.bot.player.inventory.heldItemSlot)) {
                settings.bot.player.heldItemSlot = block - settings.bot.player.inventory.heldItemSlot
            }
            if (settings.losWhenPlacingBlocks && placingBlock!!.y == floor(settings.bot.player.position.y).toInt() - 1 && placingBlock!!.dy == 0) {
                if (!moveToEdge(Vec3i(placingBlock!!.x, placingBlock!!.y, placingBlock!!.z), placingBlock!!.dx, placingBlock!!.dz)) return
            }
            var canPlace = true
            if (placingBlock!!.jump) {
                settings.bot.controller.jump = true
                canPlace = placingBlock!!.y + 1 < settings.bot.player.position.y
            }
            if (canPlace) {
                val face = if (placingBlock!!.dx != 0) {
                    if (placingBlock!!.dx > 0) EnumBlockFacing.EAST else EnumBlockFacing.WEST
                } else if (placingBlock!!.dy != 0) {
                    if (placingBlock!!.dy > 0) EnumBlockFacing.UP else EnumBlockFacing.DOWN
                } else {
                    if (placingBlock!!.dz > 0) EnumBlockFacing.SOUTH else EnumBlockFacing.NORTH
                }
                settings.bot.controller.useOnBlock(placingBlock!!.x + placingBlock!!.dx - face.offset.x, placingBlock!!.y + placingBlock!!.dy - face.offset.y,
                    placingBlock!!.z + placingBlock!!.dz - face.offset.z, face)
                settings.bot.protocol.swingItem()
                settings.bot.player.sneaking = false
                if (settings.losWhenPlacingBlocks && placingBlock!!.returnPos != null) returningPos = placingBlock!!.returnPos!!.copy()
                placing = false
                placingBlock = null
            }
            return
        }

        val p = settings.bot.player.position
        var dx = nextPoint.postX - p.x
        val dy = nextPoint.postY - p.y
        var dz = nextPoint.postZ - p.z
        if (abs(dx) <= 0.35 && abs(dz) <= 0.35 && abs(dy) < 1) {
            // arrived at next point
            lastNodeTime = System.currentTimeMillis()
            if (stopPathing) {
                stop()
                return
            }
            path.remove(nextPoint)
            if (path.isEmpty()) { // done
                // If the block the bot is standing on is not a full block only checking for the floored position can fail as
                // the distance to the goal can get greater then 0 when the vector is floored.
                if (!dynamicGoal && stateGoal != null && (stateGoal!!.isEnd(p.floored()) || stateGoal!!.isEnd(p.floored().apply { y += 1 }))) {
                    stateGoal = null
                }
                fullStop()
                return
            }
            // not done yet
            nextPoint = path.first()
            if (nextPoint.toPlace.isNotEmpty() || nextPoint.toBreak.isNotEmpty()) {
                fullStop()
                return
            }
            dx = nextPoint.postX - p.x
            dz = nextPoint.postZ - p.z
        }

        settings.bot.player.rotation.set(wrapAngleTo180(Math.toDegrees(atan2(-dx, dz)).toFloat()), 0f)
        settings.bot.controller.forward = true
        settings.bot.controller.jump = false

        if (physics.isInWater) {
            settings.bot.controller.jump = true
            settings.bot.player.sprinting = false
        } else if (settings.allowSprinting && simulator.canStraightLine(path, true)) {
            settings.bot.controller.jump = false
            settings.bot.player.sprinting = true
        } else if (settings.allowSprinting && simulator.canSprintJump(path)) {
            settings.bot.controller.jump = true
            settings.bot.player.sprinting = true
        } else if (simulator.canStraightLine(path)) {
            settings.bot.controller.jump = false
            settings.bot.player.sprinting = false
        } else if (simulator.canWalkJump(path)) {
            settings.bot.controller.jump = true
            settings.bot.player.sprinting = false
        } else {
            settings.bot.controller.forward = false
            settings.bot.player.sprinting = false
        }

        // check for futility
        if (System.currentTimeMillis() - lastNodeTime > 5000) {
            // should never take this long to go to the next node
            resetPath()
        }
    }
}