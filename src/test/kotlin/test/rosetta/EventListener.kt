package test.rosetta

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.event.*
import me.liuli.rosetta.entity.EntityVehicle
import me.liuli.rosetta.world.data.EnumBlockFacing

class EventListener(val bot: MinecraftBot) : ListenerSet() {

    @Listen
    fun onDisconnect(event: DisconnectEvent) {
        println("Disconnect: ${event.reason}")
    }

    @Listen
    fun onChat(event: ChatReceiveEvent) {
        println(event.message)
        if (event.message.contains("/register")) {
            bot.chat("/register passwd0000 passwd0000")
        } else if (event.message.contains("/login")) {
            bot.chat("/login passwd0000")
        }
    }

//    @Listen
//    fun onTitle(event: TitleEvent) {
//        println("${event.type} ${event.message}")
//    }

    @Listen
    fun onTick(event: TickEvent) {
//        if (bot.player.openWindow == null && bot.world.tickExisted != 0L && bot.world.tickExisted % 100 == 0L) {
//            bot.protocol.useItem(127, 74, 172, EnumBlockFacing.UP)
//        }
//        val window = bot.player.openWindow ?: return
//        window.storage.forEachIndexed { index, item ->
//            if (item.id != 0) {
//                println("$index $item")
//            }
//        }
    }

//    @Listen
//    fun onPacketReceive(event: PacketReceiveEvent) {
//        println(event.packet)
//    }

    @Listen
    fun onDeath(event: DeathEvent) {
        println("Death: ${event.cause}")
        bot.protocol.respawn()
    }
}