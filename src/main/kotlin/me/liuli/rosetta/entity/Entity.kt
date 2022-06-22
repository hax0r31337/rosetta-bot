package me.liuli.rosetta.entity

import me.liuli.rosetta.util.vec.Vec2f
import me.liuli.rosetta.util.vec.Vec3d

open class Entity {

    open var id = 0
    open val type = ""
    open var displayName = ""
    open val position = Vec3d()
    open val rotation = Vec2f()

    open var width = 0.6f
    open var height = 1.8f

    // TODO: vehicle and entity attachments
}