package me.liuli.rosetta.bot

import me.liuli.rosetta.bot.event.Event
import me.liuli.rosetta.bot.event.Listener
import me.liuli.rosetta.bot.event.TickEvent
import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.world.World
import java.net.Proxy
import kotlin.concurrent.thread

class MinecraftBot(val account: MinecraftAccount, val protocol: MinecraftProtocol) {

    val player = EntityClientPlayer()
    val world = World()
    var isConnected = false
    var tickDelay = 50

    private val handler = BotProtocolHandler(this)

    private val listeners = mutableMapOf<Class<out Event>, MutableList<Listener<*>>>()

    init {
        protocol.setHandler(handler)
    }

    fun connect(host: String, port: Int, proxy: Proxy = Proxy.NO_PROXY) {
        if (isConnected) {
            throw IllegalStateException("Already connected")
        }
        protocol.connect(host, port, proxy)
    }

    fun connectAsync(host: String, port: Int, proxy: Proxy = Proxy.NO_PROXY) {
        thread {
            connect(host, port, proxy)
        }
        while(!isConnected) {
            Thread.sleep(10)
        }
    }

    fun startTick() {
        var lastTime: Long
        while (isConnected) {
            lastTime = System.currentTimeMillis() // TODO: better way to do this?
            this.tick()
            Thread.sleep(tickDelay - (System.currentTimeMillis() - lastTime).coerceAtLeast(0))
        }
    }

    fun tick() {
        emit(TickEvent())
        world.tick()
        protocol.move(player.position.x, player.position.y, player.position.z, player.rotation.x, player.rotation.y, player.onGround)
    }

    fun disconnect() {
        protocol.disconnect()
    }

    fun chat(message: String) {
        protocol.chat(message)
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