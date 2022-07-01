package me.liuli.rosetta.entity.client

abstract class PlayerInput {

    var forward = false
    var back = false
    var left = false
    var right = false
    var jump = false

    val forwardValue: Float
        get() = (if(forward) 1f else 0f) - (if(back) 1f else 0f)
    val strafeValue: Float
        get() = (if(right) 1f else 0f) - (if(left) 1f else 0f)
}