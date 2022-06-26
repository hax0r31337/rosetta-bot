package test.rosetta

import me.liuli.rosetta.bot.MinecraftAccount
import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.entity.move.Physics
import test.rosetta.conv.BlockConverter
import test.rosetta.conv.ItemConverter
import test.rosetta.proto.AdaptProtocol
import test.rosetta.proto.AdaptWorldIdentifier

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        // initialize minecraft data
        kotlin.run {
            val time = System.currentTimeMillis()
            BlockConverter
            ItemConverter
            println("init minecraft data cost: ${System.currentTimeMillis() - time}ms")
        }

        joinServer()
    }

    private fun joinServer() {
        val proto = AdaptProtocol() // create a protocol instance to communicate with the server
        val bot = MinecraftBot(MinecraftAccount.offline("RosettaBot"), proto) // create a bot instance with the account and protocol
//        bot.tickDelay = 200

//        bot.registerListener(FuncListener(DisconnectEvent::class.java) {
//            println("Disconnected: ${it.reason}")
//        })
        bot.registerListeners(*(EventListener(bot).listeners)) // setup event listeners to handle events
        Physics(bot, AdaptWorldIdentifier()).setupTickListener()

        bot.connect("127.0.0.1", 25565) // connect to the server and don't block the current thread
    }
}