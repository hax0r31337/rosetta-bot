package test.rosetta

import me.liuli.rosetta.bot.MinecraftAccount
import me.liuli.rosetta.bot.MinecraftBot
import test.rosetta.proto.AdaptProtocol

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        joinServer()
    }

    private fun joinServer() {
        val proto = AdaptProtocol()
        val bot = MinecraftBot(MinecraftAccount.offline("RosettaBot"), proto)

//        bot.registerListener(FuncListener(DisconnectEvent::class.java) {
//            println("Disconnected: ${it.reason}")
//        })
        bot.registerListeners(*(EventListener(bot).listeners))

        bot.connectAsync("127.0.0.1", 25565)
    }
}