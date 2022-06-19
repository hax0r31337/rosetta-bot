package test.rosetta

import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.MinecraftAccount
import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.event.FuncListener
import test.rosetta.proto.AdaptProtocol
import kotlin.concurrent.thread

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
        bot.registerListeners(*(EventListener().listeners))

        bot.connectAsync("127.0.0.1", 25565)
    }
}