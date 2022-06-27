package me.liuli.rosetta.entity.move

open class PhysicsSetting {

    open val negligibleVelocity = 0.005f
    open val autojumpCooldown = 10 // ticks (0.5s)
    open val honeyJumpMultiplier = 0.4
    open val sneakSpeedMultiplier = 0.3
    open val slowFallingMultiplier = 0.125
    open val liquidAcceleration = 0.02
    open val waterInertia = 0.8
    open val lavaInertia = 0.5
    open val gravity = 0.08
    open val waterGravity = 0.02 // gravity / 16 when over 1.12
    open val lavaGravity = 0.02 // gravity / 4 when over 1.12
    open val outOfLiquidImpulse = 0.3
    open val airborneAcceleration = 0.02
    open val airborneInertia = 0.91
    open val ladderMaxSpeed = 0.15
    open val ladderClimbSpeed = 0.2
    open val airDrag = 1 - 0.02f
    open val stepHeight = 0.6
    open val velocityBlockSpeed = 0.4
    open val bubbleColumnSurfaceDrag = BubbleDrag(0.03, -0.9, 0.1, 1.8)
    open val bubbleColumnDrag = BubbleDrag(0.03, -0.3, 0.06, 0.7)

    class BubbleDrag(val down: Double, val maxDown: Double, val up: Double, val maxUp: Double)

    companion object {
        val INSTANCE = PhysicsSetting()
    }
}