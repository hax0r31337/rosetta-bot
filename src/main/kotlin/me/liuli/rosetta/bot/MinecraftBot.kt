package me.liuli.rosetta.bot

import me.liuli.rosetta.bot.event.DeathEvent
import me.liuli.rosetta.bot.event.Event
import me.liuli.rosetta.bot.event.Listener
import me.liuli.rosetta.bot.event.TickEvent
import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.world.World
import java.net.Proxy
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class MinecraftBot(val account: MinecraftAccount, val protocol: MinecraftProtocol) {

    val player = EntityClientPlayer()
    val world = World()
    var isConnected = false
    var tickDelay = 50L

    private val handler = BotProtocolHandler(this)
    private var executor: ScheduledExecutorService? = null

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
        thread(name = "rosetta-${account.username}") {
            connect(host, port, proxy)
        }
    }

    fun startTick() {
        if (executor != null) {
            executor!!.shutdown()
        }
        executor = Executors.newSingleThreadScheduledExecutor()
        executor!!.scheduleAtFixedRate({
            tick()
        }, 0, tickDelay, TimeUnit.MILLISECONDS)
    }

    private var lastSlot = 0

    fun tick() {
        emit(TickEvent())
        world.tick()

        // update player
        // TODO: stream player ability
        if (player.health <= 0 && player.isAlive) {
            player.isAlive = false
            emit(DeathEvent())
        } else if (player.health > 0 && !player.isAlive) {
            player.isAlive = true
        }
        if (lastSlot != player.heldItemSlot) {
            protocol.heldItemChange(player.heldItemSlot)
            lastSlot = player.heldItemSlot
        }
        if (player.isAlive) {
            protocol.move(player.position.x, player.position.y, player.position.z,
                player.rotation.x, player.rotation.y, player.onGround, player.sprinting, player.sneaking)
        }
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