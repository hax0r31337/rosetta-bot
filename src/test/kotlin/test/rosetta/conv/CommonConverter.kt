package test.rosetta.conv

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode
import com.github.steveice10.mc.protocol.data.game.setting.Difficulty
import com.github.steveice10.packetlib.ProxyInfo
import me.liuli.rosetta.world.data.EnumDifficulty
import me.liuli.rosetta.world.data.EnumGameMode
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
        when(proxy.type()) {
            Proxy.Type.HTTP -> return ProxyInfo(ProxyInfo.Type.HTTP, proxy.address())
            Proxy.Type.SOCKS -> return ProxyInfo(ProxyInfo.Type.SOCKS4, proxy.address())
        }
        return null
    }
}