package me.liuli.rosetta.bot.event

// event from server

/**
 * called when bot connected to server
 */
class ConnectedEvent : Event()

/**
 * called when bot got kicked or disconnected
 */
class DisconnectEvent(val reason: String, val isClient: Boolean) : Event()

/**
 * called when bot got teleported by the server
 */
class TeleportEvent(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float) : EventCancellable()

/**
 * called when received chat message from server
 */
class ChatReceiveEvent(var message: String, var json: String) : Event()

/**
 * tick... tack...
 */
class TickEvent : Event()