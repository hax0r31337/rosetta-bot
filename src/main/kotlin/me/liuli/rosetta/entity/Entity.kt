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

    var riding: Entity? = null
        set(value) {
            if (field != null) {
                field!!.removePassenger(this)
            }
            if (value != null) {
                value!!.addPassenger(this)
            }
            field = value
        }
    open val passengers = mutableListOf<Entity>()

    fun addPassenger(entity: Entity) {
        passengers.add(entity)
    }

    fun removePassenger(entity: Entity) {
        passengers.remove(entity)
    }

    fun tick() {
        if(riding != null) {
            position.set(riding!!.position)
        }
    }
}