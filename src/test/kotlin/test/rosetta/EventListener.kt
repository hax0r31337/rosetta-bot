package test.rosetta

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.event.ListenerSet
import me.liuli.rosetta.bot.event.TickEvent

class EventListener(val bot: MinecraftBot) : ListenerSet() {

    @Listen
    fun onDisconnect(event: DisconnectEvent) {
        println("Disconnect: ${event.reason}")
    }

    @Listen
    fun onTick(event: TickEvent) {
        bot.player.position.x += (if (Math.random() > 0.5) 1 else -1) * Math.random()
//            bot.player.position.y += (if (Math.random() > 0.5) 1 else -1) * Math.random()
        bot.player.position.z += (if (Math.random() > 0.5) 1 else -1) * Math.random()
        bot.player.rotation.x = Math.random().toFloat() * 360
        bot.player.rotation.y = Math.random().toFloat() * 180 - 90
    }
}