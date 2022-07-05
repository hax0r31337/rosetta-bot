package test.rosetta

import com.google.gson.Gson
import com.google.gson.JsonParser
import me.liuli.elixir.manage.AccountSerializer
import me.liuli.rosetta.bot.MinecraftAccount
import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.event.ChatReceiveEvent
import me.liuli.rosetta.bot.event.FuncListener
import me.liuli.rosetta.bot.event.PreMotionEvent
import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.entity.move.Physics
import me.liuli.rosetta.pathfinding.Pathfinder
import me.liuli.rosetta.pathfinding.algorithm.AStar
import me.liuli.rosetta.pathfinding.goals.GoalFollow
import me.liuli.rosetta.pathfinding.path.Move
import test.rosetta.conv.BlockConverter
import test.rosetta.conv.ItemConverter
import test.rosetta.proto.AdaptPathfinderSettings
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
        val identifier = AdaptWorldIdentifier()
        val physics = Physics(bot, identifier)
//        val pathSettings = AdaptPathfinderSettings(bot, identifier)
//        val pathfinder = Pathfinder(pathSettings)

        bot.registerListeners(*(EventListener(bot).listeners)) // setup event listeners to handle events
        bot.registerListener(physics.getListener())

//        var path = mutableListOf<Move>()
//        bot.registerListener(FuncListener(ChatReceiveEvent::class.java) {
//            if (it.message.contains("doRoute")) {
//                val astar = AStar(Move(bot.player.position.x.toInt(), bot.player.position.y.toInt(), bot.player.position.z.toInt(),
//                    0, 1f), pathSettings,
//                    GoalFollow(bot.world.entities.values.firstOrNull { it != bot.player && it is EntityPlayer } ?: return@FuncListener, 3.0))
//                val result = astar.compute()
//                bot.chat("${result.status.name} ${result.timeCost}")
//                path = result.path
//            }
//        })
//        bot.registerListener(FuncListener(PreMotionEvent::class.java) {
//            if (path.isEmpty()) return@FuncListener
//            val node = path.first()
//            bot.player.position.set(node.x + 0.5, node.y.toDouble(), node.z + 0.5)
//            path.remove(node)
//        })

        bot.connect("127.0.0.1", 25565) // connect to the server and don't block the current thread
    }
}