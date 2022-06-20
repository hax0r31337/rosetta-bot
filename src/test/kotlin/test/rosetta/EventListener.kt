package test.rosetta

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.event.ListenerSet
import me.liuli.rosetta.bot.event.TickEvent
import me.liuli.rosetta.util.stripColor

class EventListener(val bot: MinecraftBot) : ListenerSet() {

    @Listen
    fun onDisconnect(event: DisconnectEvent) {
        println("Disconnect: ${event.reason}")
    }

    @Listen
    fun onTick(event: TickEvent) {
    }
}