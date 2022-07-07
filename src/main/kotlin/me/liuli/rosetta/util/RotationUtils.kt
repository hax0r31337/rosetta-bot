package me.liuli.rosetta.util

import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.world.block.AxisAlignedBB
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Get the center of a box
 *
 * @param bb your box
 * @return center of box
 */
fun getCenter(bb: AxisAlignedBB): Vec3d {
    return Vec3d(
        bb.minX + (bb.maxX - bb.minX) * 0.5,
        bb.minY + (bb.maxY - bb.minY) * 0.5,
        bb.minZ + (bb.maxZ - bb.minZ) * 0.5
    )
}

/**
 * Get location eyes of a player
 * @param player
 * @return eyes location
 */
fun getEyesLocation(player: EntityPlayer): Vec3d {
    return Vec3d(player.position.x, player.position.y + 1.62f, player.position.z)
}

/**
 * Translate vec to rotation
 *
 * @param vec     target vec
 * @param predict predict new location of your body
 * @return rotation
 */
fun getRotationOf(vec: Vec3d, eyesPos: Vec3d): Pair<Float, Float> {
    val diffX = vec.x - eyesPos.x
    val diffY = vec.y - eyesPos.y
    val diffZ = vec.z - eyesPos.z
    return Pair(
        wrapAngleTo180(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
        wrapAngleTo180((-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat())
    )
}

fun getViewVector(yaw: Float, pitch: Float): Vec3d {
    val csPitch = Math.cos(pitch.toDouble())
    val snPitch = Math.sin(pitch.toDouble())
    val csYaw = Math.cos(yaw.toDouble())
    val snYaw = Math.sin(yaw.toDouble())
    return Vec3d(-snYaw * csPitch, snPitch, -csYaw * csPitch)
}

fun wrapAngleTo180(angleIn: Float): Float {
    var angle = angleIn % 360
    if (angle >= 180) {
        angle -= 360
    }
    if (angle < -180) {
        angle += 360
    }
    return angle
}