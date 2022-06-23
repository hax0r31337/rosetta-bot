package test.rosetta.data

import com.github.steveice10.mc.protocol.data.game.entity.attribute.AttributeModifier
import com.github.steveice10.mc.protocol.data.game.entity.attribute.ModifierOperation
import com.github.steveice10.mc.protocol.data.game.entity.attribute.ModifierType
import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.entity.move.IMoveSpeedModifier
import java.util.UUID

class MoveModifier(val uuid: UUID, val amount: Float, val type: ModifierType, val operation: ModifierOperation) : IMoveSpeedModifier {

    override fun getSpeed(baseSpeed: Float, isFlying: Boolean, entity: Entity): Float {
        if (isFlying || entity !is EntityClientPlayer || (type == ModifierType.SPRINT_SPEED_BOOST && !entity.sprinting)) {
            return baseSpeed
        }
        return when(operation) {
            ModifierOperation.MULTIPLY -> baseSpeed * (1 + amount)
            ModifierOperation.ADD -> baseSpeed + amount
            ModifierOperation.ADD_MULTIPLIED -> baseSpeed + amount // todo: verify this operation
        }
    }

    companion object {
        fun build(modifier: AttributeModifier): MoveModifier? {
            if (modifier.type == ModifierType.SPEED_POTION_MODIFIER || modifier.type == ModifierType.SPRINT_SPEED_BOOST) {
                return MoveModifier(modifier.uuid, modifier.amount.toFloat(), modifier.type, modifier.operation)
            }
            return null
        }
    }
}