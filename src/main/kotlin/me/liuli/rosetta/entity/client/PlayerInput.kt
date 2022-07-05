package me.liuli.rosetta.entity.client

open class PlayerInput {

    var forward = false
    var back = false
    var left = false
    var right = false
    var jump = false

    val forwardValue: Float
        get() = (if(forward) 1f else 0f) - (if(back) 1f else 0f)
    val strafeValue: Float
        get() = (if(right) 1f else 0f) - (if(left) 1f else 0f)

    open fun clearControlState() {
        forward = false
        back = false
        left = false
        right = false
        jump = false
    }

    companion object {
        fun clone(input: PlayerInput): PlayerInput {
            return PlayerInput().also {
                it.forward = input.forward
                it.back = input.back
                it.left = input.left
                it.right = input.back
                it.jump = input.jump
            }
        }
    }
}