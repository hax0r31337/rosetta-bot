package me.liuli.rosetta.entity.move

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.event.FuncListener
import me.liuli.rosetta.bot.event.PreMotionEvent
import me.liuli.rosetta.util.vec.Vec3f
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.WorldIdentifier
import me.liuli.rosetta.world.block.AxisAlignedBB
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.data.EnumBlockFacing
import kotlin.math.*

/**
 * some code copy from https://github.com/PrismarineJS/prismarine-physics/
 */
class Physics(val bot: MinecraftBot, val identifier: WorldIdentifier, val settings: PhysicsSetting = PhysicsSetting.INSTANCE) {

    // client player data
    var isInWater = false
        private set
    var isInLava = false
        private set
    var isInWeb = false
        private set
    var jumpTicks = 0
        private set

    /**
     * this method will set an event listener to bot and automatically simulate physics every tick
     */
    fun setupTickListener() {
        bot.registerListener(FuncListener(PreMotionEvent::class.java) {
            if (it.isCancelled) return@FuncListener
            simulate()
        })
    }

    fun simulate() {
        if (!bot.isConnected || bot.world.getChunkAt(bot.player.position.x.toInt() shr 4, bot.player.position.z.toInt() shr 4) == null) {
            return
        }
        applyWaterFlow()
        val lavaBB = bot.player.axisAlignedBB.apply { contract(0.1, 0.4, 0.1) }
        isInLava = bot.world.getSurroundingBlocks(lavaBB) { it, _, _, _ ->
            identifier.isLava(it)
        }.isNotEmpty()

        val motion = bot.player.motion
        val position = bot.player.position

        // Reset velocity component if it falls under the threshold
        if (abs(motion.x) < settings.negligibleVelocity) motion.x = .0
        if (abs(motion.y) < settings.negligibleVelocity) motion.y = .0
        if (abs(motion.z) < settings.negligibleVelocity) motion.z = .0

        if (bot.controller.jump) {
            if (jumpTicks > 0) jumpTicks--
            if (isInWater || isInLava) {
                motion.y += 0.04
            } else if(bot.player.onGround && jumpTicks == 0) {
                val blockBelow = bot.world.getBlockAt(position.x.toInt(), position.y.toInt() - 1, position.z.toInt())
                motion.y = 0.42f * (if(blockBelow?.let { identifier.isHoneyBlock(it) } == true) settings.honeyJumpMultiplier else 1.0)
                val jumpBoost = identifier.jumpBoostLevel(bot.player)
                if (jumpBoost > 0) {
                    motion.y += 0.1 * jumpBoost
                }
                bot.protocol.jump()
                if (bot.player.sprinting) {
                    val yaw = Math.toRadians(bot.player.rotation.x.toDouble())
                    motion.x -= sin(yaw).toFloat() * 0.2
                    motion.z += cos(yaw).toFloat() * 0.2
                }
                jumpTicks = settings.autojumpCooldown
            }
        } else {
            jumpTicks = 0
        }

        var strafe = bot.controller.strafeValue * 0.98
        var forward = bot.controller.forwardValue * 0.98

        if (bot.player.sneaking) {
            strafe *= settings.sneakSpeedMultiplier
            forward *= settings.sneakSpeedMultiplier
        }

        if (isInWater || isInLava) {
            bot.player.sprinting = false
            moveInWater(strafe, forward)
        } else {
            moveInAir(strafe, forward)
        }
    }

    private fun moveInWater(strafe: Double, forward: Double) {
        val motion = bot.player.motion
        val position = bot.player.position

        val lastY = position.y
        var acceleration = settings.liquidAcceleration
        val inertia = if(isInWater) settings.waterInertia else settings.lavaInertia
        var horizontalInertia = inertia

        if (isInWater) {
            var strider = identifier.depthStriderEnchantLevel(bot.player).toDouble()
            if (!bot.player.onGround) {
                strider *= 0.5
            }
            if (strider > 0) {
                horizontalInertia += (0.546 - horizontalInertia) * strider / 3
                acceleration *= (0.7 - acceleration) * strider / 3
                if (identifier.dolphinsGraceLevel(bot.player) > 0) horizontalInertia = 0.96
            }
        }

        applyHeading(strafe, forward, acceleration)
        movePlayer()

        motion.y *= inertia
        motion.y -= (if(isInWater) settings.waterGravity else settings.lavaGravity) *
                (if(motion.y <= 0 && identifier.slowFallingLevel(bot.player) > 0) settings.slowFallingMultiplier else 1.0)
        motion.x *= horizontalInertia
        motion.z *= horizontalInertia

        if (bot.player.isCollidedHorizontally) {
            val bb = AxisAlignedBB(position.x + motion.x, lastY + motion.y + 0.6, position.z + motion.z, bot.player.shape)
            if(!bot.world.getSurroundingBBs(bb).any { it.intersects(bb) } && getWaterInBB(bb).isNotEmpty()) {
                motion.y = settings.outOfLiquidImpulse
            }
        }
    }

    private fun movePlayer() {
        val motion = bot.player.motion

        if(isInWeb) {
            motion.x *= 0.25
            motion.y *= 0.25
            motion.z *= 0.25
        }
        bot.player.applyMotionCollides(bot.world, settings, identifier)
        if (isInWeb) {
            motion.set(.0, .0, .0)
        }

        val bb = bot.player.axisAlignedBB.apply { contract(0.001, 0.001, 0.001) }
        bot.world.getSurroundingBlocks(bb) { block, x, y, z ->
            if (identifier.velocityBlocksOnCollision && identifier.isVelocityBlock(block)) {
                motion.x *= settings.velocityBlockSpeed
                motion.z *= settings.velocityBlockSpeed
            }
            isInWeb = identifier.isWeb(block)
            val bubble = identifier.getBubbleStat(block)
            if (bubble != 0) {
                val aboveBlock = bot.world.getBlockAt(x, y - 1, z)
                val drag = if(aboveBlock == null || aboveBlock.id == Block.AIR.id) settings.bubbleColumnSurfaceDrag else settings.bubbleColumnDrag
                if (bubble == -1) {
                    motion.y = (motion.y - drag.down).coerceAtLeast(drag.maxDown)
                } else {
                    motion.y = (motion.y + drag.up).coerceAtMost(drag.maxUp)
                }
            }
            false
        }

        if (!identifier.velocityBlocksOnCollision) {
            val blockUnder = bot.world.getBlockAt(bot.player.position.x.toInt(), (bot.player.position.y - 0.5).toInt(), bot.player.position.z.toInt())
            if (blockUnder != null && identifier.isVelocityBlock(blockUnder)) {
                motion.x *= settings.velocityBlockSpeed
                motion.z *= settings.velocityBlockSpeed
            }
        }
    }

    private fun moveInAir(strafe: Double, forward: Double) {
        val motion = bot.player.motion
        val position = bot.player.position

        var acceleration = settings.airborneAcceleration
        var inertia = settings.airborneInertia
        val blockUnder = bot.world.getBlockAt(position.x.toInt(), position.y.toInt() - 1, position.z.toInt()) ?: Block.AIR

        if (bot.player.onGround) {
            inertia *= identifier.getSlipperiness(blockUnder)
            acceleration = (bot.player.walkSpeed * 0.1627714 / inertia.pow(3)).coerceAtLeast(.0) // acceleration should not be negative
        }

        applyHeading(strafe, forward, acceleration)

        if (identifier.isClimbable(bot.world.getBlockAt(position.x.toInt(), position.y.toInt(), position.z.toInt()) ?: Block.AIR)) {
            motion.x = motion.x.coerceIn(-settings.ladderMaxSpeed, settings.ladderMaxSpeed)
            motion.z = motion.z.coerceIn(-settings.ladderMaxSpeed, settings.ladderMaxSpeed)
            motion.y = motion.y.coerceAtLeast(if (bot.player.sneaking) .0 else -settings.ladderMaxSpeed)
        }

        movePlayer()

        // refresh isOnClimbableBlock cuz position changed
        if (identifier.isClimbable(bot.world.getBlockAt(position.x.toInt(), position.y.toInt(), position.z.toInt()) ?: Block.AIR)
            && (bot.player.isCollidedHorizontally || (identifier.climbUsingJump && bot.controller.jump))) {
            motion.y = settings.ladderClimbSpeed
        }

        // apply friction and gravity
        val levitation = identifier.levitationLevel(bot.player)
        if (levitation > 0) {
            motion.y += (0.05 * levitation - motion.y) * 0.2
        } else {
            motion.y -= settings.gravity *
                    (if(motion.y <= 0 && identifier.slowFallingLevel(bot.player) > 0) settings.slowFallingMultiplier else 1.0)
        }
        motion.x *= inertia
        motion.z *= inertia
        motion.y *= settings.airDrag
    }

    private fun applyHeading(strafe: Double, forward: Double, multiplier: Double) {
        var speed = sqrt(strafe * strafe + forward * forward)
        if (speed < 0.01) return
        speed = multiplier / speed.coerceAtLeast(1.0)

        val strafe = strafe * speed
        val forward = forward * speed

        val yaw = Math.toRadians(bot.player.rotation.x.toDouble())
        val sinYaw = sin(yaw).toFloat()
        val cosYaw = cos(yaw).toFloat()

        bot.player.motion.x += strafe * cosYaw - forward * sinYaw
        bot.player.motion.z += forward * cosYaw + strafe * sinYaw
    }

    private fun getFlow(x: Int, y: Int, z: Int): Vec3f {
        val flow = Vec3f()
        val curLevel = identifier.getWaterDepth(bot.world.getBlockAt(x, y, z) ?: return flow)
        if (curLevel == -1) {
            return flow
        }
        val directions = arrayOf(EnumBlockFacing.EAST, EnumBlockFacing.WEST, EnumBlockFacing.NORTH, EnumBlockFacing.SOUTH)
        for (direction in directions) {
            val dx = direction.offset.x
            val dz = direction.offset.z
            val adjBlock = bot.world.getBlockAt(x + dx, y, z + dz)
            val adjLevel = adjBlock?.let { identifier.getWaterDepth(it) } ?: -1
            if (adjLevel == -1) {
                if (adjBlock?.shape != null) {
                    val newLevel = bot.world.getBlockAt(x + dx, y - 1, z + dz)?.let { identifier.getWaterDepth(it) } ?: -1
                    if (newLevel >= 0) {
                        val f = newLevel - (curLevel - 8)
                        flow.x += dx * f
                        flow.z += dz * f
                    }
                }
            } else {
                val f = adjLevel - curLevel
                flow.x += dx * f
                flow.z += dz * f
            }
        }

        flow.normalize()

        return flow
    }

    private fun applyWaterFlow() {
        val waterBB = bot.player.axisAlignedBB.apply { contract(0.001, 0.401, 0.001) }
        val waterCollides = getWaterInBB(waterBB)
        isInWater = waterCollides.isNotEmpty()
        if (!isInWater) {
            return
        }

        val acceleration = Vec3f()
        for (blockPos in waterCollides) {
            val flow = getFlow(blockPos.x, blockPos.y, blockPos.z)
            acceleration.x += flow.x
            acceleration.y += flow.y
            acceleration.z += flow.z
        }

        acceleration.normalize()
        bot.player.motion.x += acceleration.x * 0.014f
        bot.player.motion.y += acceleration.y * 0.014f
        bot.player.motion.z += acceleration.z * 0.014f
    }

    private fun getWaterInBB(waterBB: AxisAlignedBB): List<Vec3i> {
        val minY = waterBB.minY.toInt()
        val maxY = ceil(waterBB.maxY)
        return bot.world.getSurroundingBlocks(waterBB) { it, _, y, _ ->
            if (y < minY) return@getSurroundingBlocks false

            val depth = identifier.getWaterDepth(it)
            if (depth == -1) {
                false
            } else {
                val level = y + 1 - ((depth + 1) / 9f)
                maxY >= level
            }
        }
    }
}