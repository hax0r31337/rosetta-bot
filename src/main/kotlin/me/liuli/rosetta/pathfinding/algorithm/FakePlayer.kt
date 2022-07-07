package me.liuli.rosetta.pathfinding.algorithm

import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.entity.move.ISimulatable
import me.liuli.rosetta.util.vec.Vec2f
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.world.block.AxisAlignedBB
import me.liuli.rosetta.world.block.Shape

class FakePlayer() : ISimulatable {

    override val position = Vec3d()
    override val motion = Vec3d()
    override val rotation = Vec2f()
    override val shape: Shape = Shape(-0.3, .0, -0.3, 0.3, 1.8, 0.3)
    override val axisAlignedBB: AxisAlignedBB
        get() = AxisAlignedBB(position.x, position.y, position.z, shape)
    override var isCollidedHorizontally = false
    override var isCollidedVertically = false
    override var walkSpeed = 0.1f
    override var onGround = true
    override var sprinting = false
    override var sneaking = false

    var isInWater = false

    constructor(player: EntityClientPlayer) : this() {
        this.position.set(player.position)
        this.motion.set(player.motion)
        this.rotation.set(player.rotation)
        this.isCollidedHorizontally = player.isCollidedHorizontally
        this.isCollidedVertically = player.isCollidedVertically
        this.walkSpeed = player.walkSpeed
        this.onGround = player.onGround
        this.sprinting = player.sprinting
        this.sneaking = player.sneaking
    }
}