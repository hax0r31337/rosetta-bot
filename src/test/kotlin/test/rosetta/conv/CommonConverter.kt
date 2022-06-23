package test.rosetta.conv

import com.github.steveice10.mc.protocol.data.game.BossBarColor
import com.github.steveice10.mc.protocol.data.game.entity.Effect
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode
import com.github.steveice10.mc.protocol.data.game.setting.Difficulty
import com.github.steveice10.mc.protocol.data.game.world.block.BlockFace
import com.github.steveice10.packetlib.ProxyInfo
import me.liuli.rosetta.world.data.EnumBlockFacing
import me.liuli.rosetta.world.data.EnumDifficulty
import me.liuli.rosetta.world.data.EnumGameMode
import java.awt.Color
import java.net.Proxy

object CommonConverter {

    fun gamemode(gm: GameMode): EnumGameMode {
        return when (gm) {
            GameMode.SURVIVAL -> EnumGameMode.SURVIVAL
            GameMode.CREATIVE -> EnumGameMode.CREATIVE
            GameMode.ADVENTURE -> EnumGameMode.ADVENTURE
            GameMode.SPECTATOR -> EnumGameMode.SPECTATOR
        }
    }

    fun difficulty(diff: Difficulty): EnumDifficulty {
        return when (diff) {
            Difficulty.PEACEFUL -> EnumDifficulty.PEACEFUL
            Difficulty.EASY -> EnumDifficulty.EASY
            Difficulty.NORMAL -> EnumDifficulty.NORMAL
            Difficulty.HARD -> EnumDifficulty.HARD
        }
    }

    fun proxy(proxy: Proxy): ProxyInfo? {
        return when(proxy.type()) {
            Proxy.Type.HTTP -> ProxyInfo(ProxyInfo.Type.HTTP, proxy.address())
            Proxy.Type.SOCKS -> ProxyInfo(ProxyInfo.Type.SOCKS4, proxy.address())
            Proxy.Type.DIRECT -> null
        }
    }

    fun bossBarColor(color: BossBarColor): Color {
        return when (color) {
            BossBarColor.PINK -> Color(255, 51, 255)
            BossBarColor.CYAN -> Color(0, 255, 255)
            BossBarColor.RED -> Color(255, 0, 0)
            BossBarColor.LIME -> Color(0, 255, 0)
            BossBarColor.YELLOW -> Color(255, 255, 0)
            BossBarColor.PURPLE -> Color(255, 0, 255)
            BossBarColor.WHITE -> Color(255, 255, 255)
        }
    }

    fun effect(effect: Effect): String {
        return effect.name.lowercase()
    }

    fun enumBlockFacing(facing: EnumBlockFacing): BlockFace {
        return when(facing) {
            EnumBlockFacing.NORTH -> BlockFace.NORTH
            EnumBlockFacing.EAST -> BlockFace.EAST
            EnumBlockFacing.SOUTH -> BlockFace.SOUTH
            EnumBlockFacing.WEST -> BlockFace.WEST
            EnumBlockFacing.UP -> BlockFace.UP
            EnumBlockFacing.DOWN -> BlockFace.DOWN
        }
    }
}