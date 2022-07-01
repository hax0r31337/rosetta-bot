package me.liuli.rosetta.entity.move

import me.liuli.rosetta.util.vec.Vec2f
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.world.World
import me.liuli.rosetta.world.WorldIdentifier
import me.liuli.rosetta.world.block.AxisAlignedBB
import me.liuli.rosetta.world.block.Shape

interface ISimulatable {

    val position: Vec3d
    val motion: Vec3d
    val rotation: Vec2f
    val shape: Shape
    val axisAlignedBB: AxisAlignedBB

    var isCollidedHorizontally: Boolean
    var isCollidedVertically: Boolean

    val walkSpeed: Float

    var onGround: Boolean
    var sprinting: Boolean
    var sneaking: Boolean

    /**
     * @param identifier you can not pass it, but some feature will be disabled
     */
    fun applyMotionCollides(world: World, settings: PhysicsSetting, identifier: WorldIdentifier?) {
        var dx = motion.x
        var dy = motion.y
        var dz = motion.z

        var oldVelX = dx
        var oldVelY = dy
        var oldVelZ = dz

        if (sneaking && onGround) {
            val step = 0.05f
            while (dx != .0 && world.getSurroundingBBs(axisAlignedBB.apply { offset(dx, .0, .0) }).isEmpty()) {
                if (dx < step && dx >= -step) dx = .0 else if (dx > 0) dx -= step else dx += step
                oldVelX = dx
            }
            while (dz != .0 && world.getSurroundingBBs(axisAlignedBB.apply { offset(.0, .0, dz) }).isEmpty()) {
                if (dz < step && dz >= -step) dz = .0 else if (dz > 0) dz -= step else dz += step
                oldVelZ = dz
            }
            while (dx != .0 && dz != .0 && world.getSurroundingBBs(axisAlignedBB.apply { offset(dx, .0, dz) }).isEmpty()) {
                if (dx < step && dx >= -step) dx = .0 else if (dx > 0) dx += step else dx -= step
                if (dz < step && dz >= -step) dz = .0 else if (dz > 0) dz += step else dz -= step
                oldVelX = dx
                oldVelZ = dz
            }
        }

        var playerBB = this.axisAlignedBB
        val queryBB = playerBB.copy().apply { addCoord(dx, dy, dz) }
        val surroundingBBs = world.getSurroundingBBs(queryBB)
        val oldBB = playerBB.copy()

        for (blockBB in surroundingBBs) {
            dy = blockBB.computeOffsetY(playerBB, dy)
        }
        playerBB.offset(.0, dy, .0)

        for (blockBB in surroundingBBs) {
            dx = blockBB.computeOffsetX(playerBB, dx)
        }
        playerBB.offset(dx, .0, .0)

        for (blockBB in surroundingBBs) {
            dz = blockBB.computeOffsetZ(playerBB, dz)
        }
        playerBB.offset(.0, .0, dz)

        // step on block if height < stepHeight
        if (settings.stepHeight > 0f && (onGround || (dy != oldVelY && oldVelY < 0)) && (dx != oldVelX || dz != oldVelZ)) {
            val oldVelXCol = dx
            val oldVelYCol = dy
            val oldVelZCol = dz
            val oldBBCol = playerBB.copy()

            dy = settings.stepHeight
            val queryBB = oldBB.copy().apply { addCoord(oldVelX, dy, oldVelZ) }
            val surroundingBBs = world.getSurroundingBBs(queryBB)

            val bb1 = oldBB.copy()
            val bb2 = oldBB.copy()
            val bbXZ = oldBB.copy().apply { addCoord(dx, .0, dz) }

            var dy1 = dy
            var dy2 = dy
            for(blockBB in surroundingBBs) {
                dy1 = blockBB.computeOffsetY(bbXZ, dy1)
                dy2 = blockBB.computeOffsetY(bb2, dy2)
            }
            bb1.offset(.0, dy1, .0)
            bb2.offset(.0, dy2, .0)

            var dx1 = oldVelX
            var dx2 = oldVelX
            for(blockBB in surroundingBBs) {
                dx1 = blockBB.computeOffsetX(bb1, dx1)
                dx2 = blockBB.computeOffsetX(bb2, dx2)
            }
            bb1.offset(dx1, .0, .0)
            bb2.offset(dx2, .0, .0)

            var dz1 = oldVelZ
            var dz2 = oldVelZ
            for(blockBB in surroundingBBs) {
                dz1 = blockBB.computeOffsetZ(bb1, dz1)
                dz2 = blockBB.computeOffsetZ(bb2, dz2)
            }
            bb1.offset(.0, .0, dz1)
            bb2.offset(.0, .0, dz2)

            val norm1 = dx1 * dx1 + dz1 * dz1
            val norm2 = dx2 * dx2 + dz2 * dz2

            if (norm1 > norm2) {
                dx = dx1
                dy = -dy1
                dz = dz1
                playerBB = bb1
            } else {
                dx = dx2
                dy = -dy2
                dz = dz2
                playerBB = bb2
            }

            for(blockBB in surroundingBBs) {
                dy = blockBB.computeOffsetY(playerBB, dy)
            }
            playerBB.offset(.0, dy, .0)

            if (oldVelXCol * oldVelXCol + oldVelZCol * oldVelZCol >= dx * dx + dz * dz) {
                dx = oldVelXCol
                dy = oldVelYCol
                dz = oldVelZCol
                playerBB = oldBBCol
            }
        }

        // update flags
        position.x = playerBB.minX - this.shape.minX
        position.y = playerBB.minY
        position.z = playerBB.minZ - this.shape.minZ
        this.isCollidedHorizontally = dx != oldVelX || dz != oldVelZ
        this.isCollidedVertically = dy != oldVelY
        this.onGround = this.isCollidedVertically && oldVelY < 0

        if (dx != oldVelX) motion.x = .0
        if (dz != oldVelZ) motion.z = .0
        if (dy != oldVelY) {
            if (identifier != null) {
                val blockAtFeet = world.getBlockAt(position.x.toInt(), (position.y - 0.2).toInt(), position.z.toInt())
                if (blockAtFeet != null && identifier.isBlockBounceable(blockAtFeet) && !sneaking) {
                    motion.y = -motion.y
                } else {
                    motion.y = .0
                }
            } else {
                motion.y = .0
            }
        }
    }
}