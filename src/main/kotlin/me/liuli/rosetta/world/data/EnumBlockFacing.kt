package me.liuli.rosetta.world.data

import me.liuli.rosetta.util.vec.Vec3i

enum class EnumBlockFacing(val offset: Vec3i) {
    UP(Vec3i(0, 1, 0)),
    DOWN(Vec3i(0, -1, 0)),
    NORTH(Vec3i(0, 0, -1)),
    SOUTH(Vec3i(0, 0, 1)),
    WEST(Vec3i(-1, 0, 0)),
    EAST(Vec3i(1, 0, 0));
}