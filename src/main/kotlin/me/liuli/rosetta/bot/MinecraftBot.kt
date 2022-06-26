package me.liuli.rosetta.bot

import me.liuli.rosetta.bot.event.*
import me.liuli.rosetta.entity.client.EntityClientPlayer
import me.liuli.rosetta.entity.client.PlayerController
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
    val controller = PlayerController()

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
        world.tick()

        // update player
        if (player.health <= 0 && player.isAlive) {
            player.isAlive = false
            emit(DeathEvent())
        } else if (player.health > 0 && !player.isAlive) {
            player.isAlive = true
        }

        val event = PreMotionEvent()
        if (!player.isAlive) event.isCancelled = true
        emit(event)
        if (event.isCancelled) return

        if (player.needAbilitiesUpdate) {
            protocol.abilities(player.invincible, player.flying, player.canFly, player.baseWalkSpeed, player.baseFlySpeed)
            player.needAbilitiesUpdate = false
        }
        if (lastSlot != player.heldItemSlot) {
            protocol.heldItemChange(player.heldItemSlot)
            lastSlot = player.heldItemSlot
        }
        if (player.isAlive && world.getChunkAt(player.position.x.toInt() shr 4, player.position.z.toInt() shr 4) != null) {
            if (player.riding == null) {
                protocol.move(player.position.x, player.position.y, player.position.z,
                    player.rotation.x, player.rotation.y, player.onGround, player.sprinting, player.sneaking)
            } else {
                protocol.moveVehicle(player.position.x, player.position.y, player.position.z, player.rotation.x, player.rotation.y,
                    controller.strafeValue, controller.forwardValue, controller.jump, player.sneaking)
            }
        }
        emit(PostMotionEvent())
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