package test.rosetta

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.event.*
import me.liuli.rosetta.entity.EntityPlayer

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
    fun onPreMotion(event: PreMotionEvent) {
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