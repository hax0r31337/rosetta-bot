package test.rosetta.proto

import me.liuli.rosetta.entity.EntityLiving
import me.liuli.rosetta.world.WorldIdentifier
import me.liuli.rosetta.world.block.Block

class AdaptWorldIdentifier : WorldIdentifier {

    override val climbUsingJump = false

    override val velocityBlocksOnCollision: Boolean
        get() = true

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
    override fun isBlockBounceable(block: Block): Boolean {
        return block.name == "slime"
    }

    override fun isVelocityBlock(block: Block): Boolean {
        return block.name == "soul_sand"
    }

    override fun isWeb(block: Block): Boolean {
        return block.name == "web"
    }

    override fun getBubbleStat(block: Block) = 0 // Not yet implemented in 1.12

    override fun jumpBoostLevel(entity: EntityLiving): Int {
        return (entity.effects.firstOrNull { it.name == "jump_boost" } ?: return 0).amplifier
    }

    override fun depthStriderEnchantLevel(entity: EntityLiving): Int {
        return 0 // TODO
    }

    override fun dolphinsGraceLevel(entity: EntityLiving) = 0  // Not yet implemented in 1.12

    override fun slowFallingLevel(entity: EntityLiving) = 0  // Not yet implemented in 1.12
    override fun levitationLevel(entity: EntityLiving): Int {
        return (entity.effects.firstOrNull { it.name == "levitation" } ?: return 0).amplifier
    }

    override fun getSlipperiness(block: Block) = when(block.name) {
        "slime" -> 0.8f
        "ice", "packed_ice", "frosted_ice" -> 0.98f
        else -> 0.6f
    }

    override fun isClimbable(block: Block): Boolean {
        return block.name == "ladder" || block.name == "vine"
    }
}