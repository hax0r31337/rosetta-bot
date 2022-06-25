package me.liuli.rosetta.bot.event

import me.liuli.rosetta.world.data.EnumTitleType

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
 * called every tick before stream position to server
 */
class PreMotionEvent : Event()

/**
 * called every tick after stream position to server
 */
class PostMotionEvent : Event()

/**
 * called when display the title
 */
class TitleEvent(val type: EnumTitleType, val message: String, val fadeIn: Int, val stay: Int, val fadeOut: Int) : Event()

/**
 * called when player death
 */
class DeathEvent(val cause: String = "unknown") : Event()