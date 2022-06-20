package test.rosetta

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.event.ChatReceiveEvent
import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.event.ListenerSet
import me.liuli.rosetta.bot.event.TickEvent

class EventListener(val bot: MinecraftBot) : ListenerSet() {

    @Listen
    fun onDisconnect(event: DisconnectEvent) {
        println("Disconnect: ${event.reason}")
    }

    @Listen
    fun onChat(event: ChatReceiveEvent) {
        println(event.message)
        bot.player.sneaking = !bot.player.sneaking
    }

//    @Listen
//    fun onTitle(event: TitleEvent) {
//        println("${event.type} ${event.message}")
//    }

    @Listen
    fun onTick(event: TickEvent) {
    }

//    @Listen
//    fun onPacketReceive(event: PacketReceiveEvent) {
//        println(event.packet)
//    }
}