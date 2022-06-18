package me.liuli.rosetta.bot

import me.liuli.rosetta.bot.event.ConnectedEvent
import me.liuli.rosetta.bot.event.DisconnectEvent

class BotProtocolHandler(val bot: MinecraftBot) {

    fun onConnected() {
        bot.emit(ConnectedEvent())
    }

    fun onDisconnect(reason: String) {
        bot.isConnected = false
        bot.emit(DisconnectEvent(reason))
    }
}