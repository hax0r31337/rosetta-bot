package test.rosetta

import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.event.ListenerSet

class EventListener : ListenerSet() {

    @Listen
    fun onDisconnect(event: DisconnectEvent) {
        println("Disconnect: ${event.reason}")
    }
}