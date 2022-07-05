package test.rosetta.proto

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.pathfinding.PathfinderSettings
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.item.Item

class AdaptPathfinderSettings(bot: MinecraftBot, identifier: AdaptWorldIdentifier) : PathfinderSettings(bot, identifier) {
    override fun canBreakBlock(block: Block): Boolean {
        return block.diggable && !block.name.contains("chest")
    }

    override fun needAvoidBlock(block: Block): Boolean {
        return block.name == "fire" || block.name == "wheat" || block.name.contains("lava") || block.name == "cobweb"
    }

    override fun replaceableBlock(block: Block): Boolean {
        return block.name == "air" || block == Block.AIR || block.name.contains("water") || block.name.contains("lava")
    }

    override fun bridgeableItem(item: Item): Boolean {
        return item.id == 1 || item.id == 3
    }
}