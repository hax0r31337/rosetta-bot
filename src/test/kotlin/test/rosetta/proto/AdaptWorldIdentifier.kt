package test.rosetta.proto

import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.entity.EntityLiving
import me.liuli.rosetta.world.WorldIdentifier
import me.liuli.rosetta.world.block.Block

class AdaptWorldIdentifier : WorldIdentifier {

    override fun getWaterDepth(block: Block): Int {
        return if(block.name == "water") {
            block.id and 0xF
        } else if(block.name == "flowing_water") {
            block.id and 0xF
        } else -1
    }

    override fun isLava(block: Block): Boolean {
        return block.name == "lava" || block.name == "flowing_lava"
    }

    override fun isHoneyBlock(block: Block) = false // Not yet implemented in 1.12

    override fun jumpBoostLevel(entity: EntityLiving): Int {
        return (entity.effects.firstOrNull { it.name == "jump_boost" } ?: return 0).amplifier
    }

    override fun depthStrider(entity: EntityLiving): Int {
        return 0 // TODO
    }

    override fun dolphinsGrace(entity: EntityLiving) = 0  // Not yet implemented in 1.12

    override fun slowFalling(entity: EntityLiving) = 0  // Not yet implemented in 1.12
}