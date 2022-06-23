package me.liuli.rosetta.world.block

import me.liuli.rosetta.world.item.Item

interface IBlockHardnessModifier {

    fun getModifier(baseHardness: Float, item: Item): Float
}