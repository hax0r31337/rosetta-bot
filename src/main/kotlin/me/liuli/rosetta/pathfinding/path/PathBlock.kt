package me.liuli.rosetta.pathfinding.path

import me.liuli.rosetta.world.block.Block

data class PathBlock(val x: Int, val y: Int, val z: Int, val block: Block,
                     val replaceable: Boolean, val canFall: Boolean, val safe: Boolean,
                     val physical: Boolean, val liquid: Boolean, val climbable: Boolean,
                     var height: Double, val openable: Boolean)