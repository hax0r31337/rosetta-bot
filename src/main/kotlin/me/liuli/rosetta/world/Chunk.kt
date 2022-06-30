package me.liuli.rosetta.world

import me.liuli.rosetta.world.block.Block

open class Chunk(val x: Int, val z: Int, val height: Int = 256) {

    open val blocks = Array(16 * 16 * height) { Block.AIR }
    open val code = code(x, z)

    open fun getBlockAt(x: Int, y: Int, z: Int): Block {
        if (y !in 0..255) {
            return Block.AIR
        }
        return blocks[y * 256 + (z shl 4 or x)]
    }

    open fun setBlockAt(x: Int, y: Int, z: Int, block: Block) {
        blocks[y * 256 + (z shl 4 or x)] = block
    }

    companion object {
        fun code(x: Int, z: Int): Long {
            return x.toLong() shl 32 or (z.toLong() and 0xffffffffL)
        }
    }
}