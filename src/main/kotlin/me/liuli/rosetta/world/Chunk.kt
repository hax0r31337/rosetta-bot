package me.liuli.rosetta.world

import me.liuli.rosetta.world.block.Block

class Chunk(val x: Int, val z: Int, val height: Int = 256) {

    val blocks = Array(height) { Array(16 * 16) { Block(0, 0, Block.Type.AIR) } }
    val code = code(x, z)

    fun getBlockAt(x: Int, y: Int, z: Int): Block {
        return blocks[y][x + z * 16]
    }

    fun setBlockAt(x: Int, y: Int, z: Int, block: Block) {
        blocks[y][x + z * 16] = block
    }

    companion object {
        fun code(x: Int, z: Int): Long {
            return x.toLong() shl 32 or (z.toLong() and 0xffffffffL)
        }
    }
}