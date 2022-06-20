package me.liuli.rosetta.world

import me.liuli.rosetta.world.block.Block

class Chunk(val x: Int, val z: Int, val height: Int = 256) {

    val blocks = Array(16 * 16 * height) { Block.AIR }
    val code = code(x, z)

    fun getBlockAt(x: Int, y: Int, z: Int): Block {
        return blocks[y * 256 + (x shl 4 or z)]
    }

    fun setBlockAt(x: Int, y: Int, z: Int, block: Block) {
        blocks[y * 256 + (x shl 4 or z)] = block
    }

    companion object {
        fun code(x: Int, z: Int): Long {
            return x.toLong() shl 32 or (z.toLong() and 0xffffffffL)
        }
    }
}