package me.liuli.rosetta.bot.event

class ConnectedEvent : Event()

class DisconnectEvent(val reason: String, val isClient: Boolean) : Event()

class TeleportEvent(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float) : EventCancellable()