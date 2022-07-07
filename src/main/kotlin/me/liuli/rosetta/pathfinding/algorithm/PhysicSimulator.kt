package me.liuli.rosetta.pathfinding.algorithm

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.entity.client.PlayerInput
import me.liuli.rosetta.entity.move.Physics
import me.liuli.rosetta.entity.move.PhysicsSetting
import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.util.wrapAngleTo180
import me.liuli.rosetta.world.WorldIdentifier
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor

class PhysicSimulator(private val bot: MinecraftBot, private val identifier: WorldIdentifier, private val physicsSetting: PhysicsSetting) {

    /**
     * @param goal A function is the goal has been reached or not
     * @param controller Controller that can change the current control State for the next tick
     * @param ticks Number of ticks to simulate
     * @returns [FakePlayer] A player state of the final simulation tick
     */
    fun simulateUntil(goal: (FakePlayer) -> Boolean, controller: (FakePlayer, PlayerInput, Int) -> Unit, ticks: Int = 1, stateIn: FakePlayer? = null, inputIn: PlayerInput? = null): FakePlayer {
        val state = stateIn ?: FakePlayer(bot.player)
        val input = inputIn ?: PlayerInput.cloneFrom(bot.controller)
        val physics = Physics(bot.world, state, input, identifier, physicsSetting)

        for (i in 0 until ticks) {
            controller(state, input, i)
            physics.simulate()
            state.isInWater = physics.isInWater
            if (physics.isInLava) return state
            if (goal(state)) return state
        }

        return state
    }

    fun getReached(path: MutableList<Move>): (FakePlayer) -> Boolean {
        return {
            val node = path[0]
            val delta = it.position.copy().apply {
                x = node.postX - x
                y = node.postY - y
                z = node.postZ - z
            }
            abs(delta.x) <= 0.35 && abs(delta.z) <= 0.35 && abs(delta.y) < 1
        }
    }

    fun getController(nextPoint: Move, jump: Boolean, sprint: Boolean, jumpAfter: Int = 0): (FakePlayer, PlayerInput, Int) -> Unit {
        return { state, input, tick ->
            val dx = nextPoint.postX - state.position.x
            val dz = nextPoint.postZ - state.position.z
            state.rotation.x = wrapAngleTo180(Math.toDegrees(atan2(-dx, dz)).toFloat())

            input.forward = true
            input.jump = jump && tick >= jumpAfter
            state.sprinting = sprint
        }
    }

    fun canStraightLineBetween(n1: Move, n2: Move): Boolean {
        val reached = { state: FakePlayer ->
            val delta = state.position.copy().apply {
                x -= n2.postX
                y -= n2.postY
                z -= n2.postZ
            }
            val r2 = 0.15 * 0.15
            (delta.x * delta.x + delta.z * delta.z) <= r2 && abs(delta.y) < 0.001 && (state.onGround || state.isInWater)
        }
        val state = FakePlayer(bot.player)
        state.position.set(n1.postX, n1.postY, n1.postZ)
        this.simulateUntil(reached, this.getController(n2, false, true), floor(5 * Vec3d(n1.postX, n1.postY, n1.postZ).distanceTo(n2.postX, n2.postY, n2.postZ)).toInt(), state)
        return reached(state)
    }

    fun canStraightLine(path: MutableList<Move>, sprint: Boolean = false): Boolean {
        val reached = this.getReached(path)
        val state = this.simulateUntil(reached, this.getController(path[0], false, sprint), 200)
        if (reached(state)) return true

        if (sprint) {
            if (this.canSprintJump(path, 0)) return false
        } else {
            if (this.canWalkJump(path, 0)) return false
        }

        for (i in 1 until 7) {
            if (sprint) {
                if (this.canSprintJump(path, i)) return true
            } else {
                if (this.canWalkJump(path, i)) return true
            }
        }
        return false
    }

    fun canSprintJump(path: MutableList<Move>, jumpAfter: Int = 0): Boolean {
        val reached = this.getReached(path)
        val state = this.simulateUntil(reached, this.getController(path[0], true, true, jumpAfter), 20)
        return reached(state)
    }

    fun canWalkJump(path: MutableList<Move>, jumpAfter: Int = 0): Boolean {
        val reached = this.getReached(path)
        val state = this.simulateUntil(reached, this.getController(path[0], true, false, jumpAfter), 20)
        return reached(state)
    }
}