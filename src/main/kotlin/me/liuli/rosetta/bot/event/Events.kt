package me.liuli.rosetta.bot.event

class ConnectedEvent : Event()

class DisconnectEvent(val reason: String) : Event()