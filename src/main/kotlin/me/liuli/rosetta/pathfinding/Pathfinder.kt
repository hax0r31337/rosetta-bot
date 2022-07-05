package me.liuli.rosetta.pathfinding

import me.liuli.rosetta.bot.event.ListenerSet
import me.liuli.rosetta.bot.event.PreMotionEvent
import me.liuli.rosetta.entity.move.Physics
import me.liuli.rosetta.pathfinding.algorithm.AStar
import me.liuli.rosetta.pathfinding.algorithm.PhysicSimulator
import me.liuli.rosetta.pathfinding.goals.IGoal
import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.pathfinding.path.PathPlaceInfo
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.block.ComplexShape
import me.liuli.rosetta.world.block.Shape
import kotlin.math.abs
import kotlin.math.floor

class Pathfinder(val settings: PathfinderSettings, val physics: Physics) : ListenerSet() {

    private var stateGoal: IGoal? = null
    private var aStarContext: AStar? = null
    private var aStarTimedOut = false
    private var dynamicGoal = false
    private var path = mutableListOf<Move>()
    private var pathUpdated = false
    private var digging = false
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
        if (digging) {
            settings.bot.controller.abortBreaking()
            digging = false
        }
        placing = false
        pathUpdated = false
        aStarContext = null
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
            if (abs(node.y - lastNode.y) > 0.5 || node.toBreak.isNotEmpty() || node.toPlace.isNotEmpty() || !PhysicSimulator.canStraightLineBetween(lastNode, node)) {
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
            path = result.path
            aStarTimedOut = result.status == AStar.ResultStatus.PARTIAL
        }

    }
}