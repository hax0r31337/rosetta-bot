package me.liuli.rosetta.world.block

import me.liuli.rosetta.util.vec.Vec3d

open class Shape(open var minX: Double, open var minY: Double, open var minZ: Double,
                 open var maxX: Double, open var maxY: Double, open var maxZ: Double) {

    companion object {
        val SHAPE_BLOCK = Shape(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
    }

    override fun toString(): String {
        return "SimpleShape(minX=$minX, minY=$minY, minZ=$minZ, maxX=$maxX, maxY=$maxY, maxZ=$maxZ)"
    }
}