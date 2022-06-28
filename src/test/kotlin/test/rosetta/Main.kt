package test.rosetta

import com.google.gson.Gson
import com.google.gson.JsonParser
import me.liuli.elixir.manage.AccountSerializer
import me.liuli.rosetta.bot.MinecraftAccount
import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.entity.move.Physics
import test.rosetta.conv.BlockConverter
import test.rosetta.conv.ItemConverter
import test.rosetta.proto.AdaptProtocol
import test.rosetta.proto.AdaptWorldIdentifier
import java.io.File
import java.util.*

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

    /**
     * get login credentials through https://github.com/CCBlueX/Elixir
     */
    private fun getAccount(): MinecraftAccount {
        val accountFile = File("./.cache/account.json")
        val account = if (!accountFile.exists()) {
            // login your account here
            AccountSerializer.accountInstance(name = "RosettaBot", password = "")
        } else {
            AccountSerializer.fromJson(JsonParser.parseReader(accountFile.reader(Charsets.UTF_8)).asJsonObject)
        }
        val session = account.session

        // save updated account credentials
        accountFile.writeText(Gson().toJson(AccountSerializer.toJson(account)))

        return MinecraftAccount(session.username, UUID.fromString(session.uuid), session.token)
    }

    private fun joinServer() {
        val proto = AdaptProtocol() // create a protocol instance to communicate with the server
        val bot = MinecraftBot(getAccount(), proto) // create a bot instance with the account and protocol
//        bot.tickDelay = 200

//        bot.registerListener(FuncListener(DisconnectEvent::class.java) {
//            println("Disconnected: ${it.reason}")
//        })
        bot.registerListeners(*(EventListener(bot).listeners)) // setup event listeners to handle events
        Physics(bot, AdaptWorldIdentifier()).setupTickListener()

        bot.connect("127.0.0.1", 25565) // connect to the server and don't block the current thread
    }
}