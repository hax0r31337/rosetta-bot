package me.liuli.rosetta.entity.move

open class PhysicsSetting {

    open val negligibleVelocity = 0.005f
    open val autojumpCooldown = 10 // ticks (0.5s)
    open val honeyJumpMultiplier = 0.4f
    open val sneakSpeedMultiplier = 0.3f
    open val slowFallingMultiplier = 0.125f
    open val liquidAcceleration = 0.02f
    open val waterInertia = 0.8f
    open val lavaInertia = 0.5f
    open val gravity = 0.08f
    open val waterGravity = 0.02f // gravity / 16 when over 1.12
    open val lavaGravity = 0.02f // gravity / 4 when over 1.12
    open val outOfLiquidImpulse = 0.3f
    open val airborneAcceleration = 0.02f
    open val airborneInertia = 0.91f
    open val ladderMaxSpeed = 0.15f
    open val ladderClimbSpeed = 0.2f
    open val airDrag = 1f - 0.02f
    open val stepHeight = 0.6
    open val velocityBlockSpeed = 0.4f
    open val bubbleColumnSurfaceDrag = BubbleDrag(0.03f, -0.9f, 0.1f, 1.8f)
    open val bubbleColumnDrag = BubbleDrag(0.03f, -0.3f, 0.06f, 0.7f)

    class BubbleDrag(val down: Float, val maxDown: Float, val up: Float, val maxUp: Float)

    companion object {
        val INSTANCE = PhysicsSetting()
    }
}