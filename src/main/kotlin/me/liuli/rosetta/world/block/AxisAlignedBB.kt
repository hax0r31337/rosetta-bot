package me.liuli.rosetta.world.block

import me.liuli.rosetta.util.vec.Vec3d

open class AxisAlignedBB(open var minX: Double, open var minY: Double, open var minZ: Double, open var maxX: Double, open var maxY: Double, open var maxZ: Double) {

    fun center(): Vec3d {
        return Vec3d(minX + (maxX - minX) / 2, minY + (maxY - minY) / 2, minZ + (maxZ - minZ) / 2)
    }

    companion object {
        val SHAPE_BLOCK = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
    }

    override fun toString(): String {
        return "AxisAlignedBB(minX=$minX, minY=$minY, minZ=$minZ, maxX=$maxX, maxY=$maxY, maxZ=$maxZ)"
    }
}