package me.liuli.rosetta.entity

import me.liuli.rosetta.util.vec.Vec2f
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.util.vec.Vec3f

open class Entity {

    open var id = 0
    open val type = ""
    open var displayName = ""
    open val position = Vec3d()
    open val rotation = Vec2f()

}