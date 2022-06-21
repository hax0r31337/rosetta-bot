package test.rosetta

import me.liuli.rosetta.bot.MinecraftAccount
import me.liuli.rosetta.bot.MinecraftBot
import test.rosetta.conv.BlockConverter
import test.rosetta.proto.AdaptProtocol

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        // initialize minecraft data
        kotlin.run {
            val time = System.currentTimeMillis()
            BlockConverter
            println("init minecraft data cost: ${System.currentTimeMillis() - time}ms")
        }

        joinServer()
    }

    private fun joinServer() {
        val proto = AdaptProtocol() // create a protocol instance to communicate with the server
        val bot = MinecraftBot(MinecraftAccount.offline("RosettaBot"), proto) // create a bot instance with the account and protocol

//        bot.registerListener(FuncListener(DisconnectEvent::class.java) {
//            println("Disconnected: ${it.reason}")
//        })
        bot.registerListeners(*(EventListener(bot).listeners)) // setup event listeners to handle events

        bot.connectAsync("127.0.0.1", 25565) // connect to the server and don't block the current thread
    }
}