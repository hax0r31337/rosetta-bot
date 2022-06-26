package me.liuli.rosetta.world.block

import me.liuli.rosetta.util.vec.Vec3d
import kotlin.math.floor

data class AxisAlignedBB(open var minX: Double, open var minY: Double, open var minZ: Double,
                    open var maxX: Double, open var maxY: Double, open var maxZ: Double) {

    constructor(offsetX: Double, offsetY: Double, offsetZ: Double, shape: Shape)
            : this(offsetX + shape.minX, offsetY + shape.minY, offsetZ + shape.minZ,
                offsetX + shape.maxX, offsetY + shape.maxY, offsetZ + shape.maxZ)

    fun center(): Vec3d {
        return Vec3d(minX + (maxX - minX) / 2, minY + (maxY - minY) / 2, minZ + (maxZ - minZ) / 2)
    }

    fun floor() {
        this.minX = floor(this.minX)
        this.minY = floor(this.minY)
        this.minZ = floor(this.minZ)
        this.maxX = floor(this.maxX)
        this.maxY = floor(this.maxY)
        this.maxZ = floor(this.maxZ)
    }

    fun addCoord(x: Double, y: Double, z: Double) {
        if (x < 0) this.minX += x
        else this.maxX += x

        if (y < 0) this.minY += y
        else this.maxY += y

        if (z < 0) this.minZ += z
        else this.maxZ += z
    }

    fun contract(x: Double, y: Double, z: Double) {
        this.minX += x
        this.minY += y
        this.minZ += z
        this.maxX -= x
        this.maxY -= y
        this.maxZ -= z
    }

    fun expand(x: Double, y: Double, z: Double) {
        this.minX -= x
        this.minY -= y
        this.minZ -= z
        this.maxX += x
        this.maxY += y
        this.maxZ += z
    }

    fun offset(x: Double, y: Double, z: Double) {
        this.minX += x
        this.minY += y
        this.minZ += z
        this.maxX += x
        this.maxY += y
        this.maxZ += z
    }

    fun intersects(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): Boolean {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY &&
                this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ
    }

    fun intersects(other: AxisAlignedBB): Boolean {
        return this.minX < other.maxX && this.maxX > other.minX && this.minY < other.maxY &&
                this.maxY > other.minY && this.minZ < other.maxZ && this.maxZ > other.minZ
    }

    fun computeOffsetX(other: AxisAlignedBB, offsetXIn: Double): Double {
        var offsetX = offsetXIn

        if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetX > 0.0 && other.maxX <= this.minX) {
                offsetX = (this.minX - other.maxX).coerceAtMost(offsetX)
            } else if (offsetX < 0.0 && other.minX >= this.maxX) {
                offsetX = (this.maxX - other.minX).coerceAtLeast(offsetX)
            }
        }

        return offsetX
    }

    fun computeOffsetY(other: AxisAlignedBB, offsetYIn: Double): Double {
        var offsetY = offsetYIn

        if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetY > 0.0 && other.maxY <= this.minY) {
                offsetY = (this.minY - other.maxY).coerceAtMost(offsetY)
            } else if (offsetY < 0.0 && other.minY >= this.maxY) {
                offsetY = (this.maxY - other.minY).coerceAtLeast(offsetY)
            }
        }

        return offsetY
    }

    fun computeOffsetZ(other: AxisAlignedBB, offsetZIn: Double): Double {
        var offsetZ = offsetZIn

        if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY) {
            if (offsetZ > 0.0 && other.maxZ <= this.minZ) {
                offsetZ = (this.minZ - other.maxZ).coerceAtMost(offsetZ)
            } else if (offsetZ < 0.0 && other.minZ >= this.maxZ) {
                offsetZ = (this.maxZ - other.minZ).coerceAtLeast(offsetZ)
            }
        }

        return offsetZ
    }
}