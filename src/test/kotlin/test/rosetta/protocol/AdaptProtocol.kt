package test.rosetta.protocol

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.MinecraftConstants
import com.github.steveice10.mc.protocol.packet.MinecraftPacket
import com.github.steveice10.packetlib.Client
import com.github.steveice10.packetlib.event.session.DisconnectedEvent
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent
import com.github.steveice10.packetlib.event.session.SessionAdapter
import com.github.steveice10.packetlib.tcp.TcpSessionFactory
import me.liuli.rosetta.bot.BotProtocolHandler
import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.MinecraftProtocol
import java.net.Proxy

class AdaptProtocol : MinecraftProtocol {

    private lateinit var handler: BotProtocolHandler

    override fun setHandler(handler: BotProtocolHandler) {
        this.handler = handler
    }

    override fun connect(host: String, port: Int, proxy: Proxy) {
        if (!this::handler.isInitialized) {
            throw IllegalStateException("handler is not initialized, please call setHandler first")
        }
        val account = handler.bot.account
        val proto = com.github.steveice10.mc.protocol.MinecraftProtocol(GameProfile(account.uuid, account.username), account.accessToken)
        val client = Client(host, port, proto, TcpSessionFactory(proxy))
        client.session.setFlag(MinecraftConstants.AUTH_PROXY_KEY, proxy)

        client.session.addListener(object : SessionAdapter() {
            override fun packetReceived(event: PacketReceivedEvent) {
                val pk = event.getPacket<MinecraftPacket>()
            }

            override fun disconnected(event: DisconnectedEvent) {
                handler.onDisconnect(event.reason)
            }
        })

        client.session.connect()
    }
}