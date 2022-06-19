package me.liuli.rosetta.bot

import me.liuli.rosetta.bot.event.Event
import me.liuli.rosetta.bot.event.Listener
import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.world.World
import java.net.Proxy

class MinecraftBot(val account: MinecraftAccount, val protocol: MinecraftProtocol) {

    val player = EntityClientPlayer()
    val world = World()
    var isConnected = false

    private val handler = BotProtocolHandler(this)

    private val listeners = mutableMapOf<Class<out Event>, MutableList<Listener<*>>>()

    init {
        protocol.setHandler(handler)
    }

    fun connect(host: String, port: Int, proxy: Proxy = Proxy.NO_PROXY) {
        if (isConnected) {
            throw IllegalStateException("Already connected")
        }
        isConnected = true
        protocol.connect(host, port, proxy)
    }

    fun emit(event: Event) {
        listeners[event.javaClass]?.forEach { Event.emit(it, event) }
    }

    fun registerListener(listener: Listener<*>) {
        listeners.computeIfAbsent(listener.eventType) { mutableListOf() }.add(listener)
    }
    
    fun registerListeners(vararg listeners: Listener<*>) {
        listeners.forEach { registerListener(it) }
    }
}