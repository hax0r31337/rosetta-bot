package me.liuli.rosetta.world.data

import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.util.vec.Vec3i

enum class EnumBlockFacing(val offset: Vec3i) {
    UP(Vec3i(0, 1, 0)),
    DOWN(Vec3i(0, -1, 0)),
    NORTH(Vec3i(0, 0, -1)),
    SOUTH(Vec3i(0, 0, 1)),
    WEST(Vec3i(-1, 0, 0)),
    EAST(Vec3i(1, 0, 0));

    companion object {
        fun calculateFacing(player: EntityClientPlayer, x: Int, y: Int, z: Int): EnumBlockFacing {
            // a simple check for which face nears player
            var face = EnumBlockFacing.UP
            var distance = 999.0
            EnumBlockFacing.values().forEach {
                val dist = player.position.distanceTo(Vec3d(x + 0.5 + it.offset.x, y.toDouble() + it.offset.y, z + 0.5 + it.offset.z))
                if (dist < distance) {
                    face = it
                    distance = dist
                }
            }
            return face
        }
    }
}