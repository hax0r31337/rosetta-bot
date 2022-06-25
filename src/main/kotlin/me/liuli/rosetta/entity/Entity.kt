package me.liuli.rosetta.entity

import me.liuli.rosetta.entity.inventory.Inventory
import me.liuli.rosetta.entity.inventory.MobInventory
import me.liuli.rosetta.util.vec.Vec2f
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.world.block.AxisAlignedBB
import me.liuli.rosetta.world.block.Shape

open class Entity {

    open var id = 0
    open val type = ""
    open var displayName = ""
    open val position = Vec3d()
    open val rotation = Vec2f()

    open val shape = Shape(-0.3, .0, -0.3, 0.3, 1.8, 0.3)
    open val axisAlignedBB: AxisAlignedBB
        get() = AxisAlignedBB(position.x, position.y, position.z, shape)

    open val inventory: Inventory = MobInventory()

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

    open fun addPassenger(entity: Entity) {
        passengers.add(entity)
    }

    open fun removePassenger(entity: Entity) {
        passengers.remove(entity)
    }

    open fun tick() {
        if(riding != null) {
            position.set(riding!!.position)
        }
    }
}