package me.liuli.rosetta.bot

import java.net.Proxy

interface MinecraftProtocol {

    fun setHandler(handler: BotProtocolHandler)

    fun connect(host: String, port: Int, proxy: Proxy)
}