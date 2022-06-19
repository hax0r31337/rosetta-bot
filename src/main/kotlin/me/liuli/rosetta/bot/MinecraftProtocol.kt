package me.liuli.rosetta.bot

import java.net.Proxy

interface MinecraftProtocol {

    fun setHandler(handler: BotProtocolHandler)

    fun connect(host: String, port: Int, proxy: Proxy)

    fun disconnect()

    fun move(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, onGround: Boolean)

    fun chat(message: String)
}