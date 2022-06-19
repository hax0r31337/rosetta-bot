package test.rosetta.protocol

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.MinecraftConstants
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand
import com.github.steveice10.mc.protocol.data.game.setting.ChatVisibility
import com.github.steveice10.mc.protocol.data.game.setting.SkinPart
import com.github.steveice10.mc.protocol.packet.MinecraftPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientPluginMessagePacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientSettingsPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket
import com.github.steveice10.packetlib.Client
import com.github.steveice10.packetlib.event.session.DisconnectedEvent
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent
import com.github.steveice10.packetlib.event.session.SessionAdapter
import com.github.steveice10.packetlib.tcp.TcpSessionFactory
import me.liuli.rosetta.bot.BotProtocolHandler
import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.MinecraftProtocol
import test.rosetta.conv.CommonConverter
import java.net.Proxy

class AdaptProtocol : MinecraftProtocol {

    private lateinit var handler: BotProtocolHandler
    private lateinit var client: Client

    override fun setHandler(handler: BotProtocolHandler) {
        this.handler = handler
    }

    override fun connect(host: String, port: Int, proxy: Proxy) {
        if (!this::handler.isInitialized) {
            throw IllegalStateException("handler is not initialized, please call setHandler first")
        }
        val account = handler.bot.account
        val proto = com.github.steveice10.mc.protocol.MinecraftProtocol(GameProfile(account.uuid, account.username), account.accessToken)
        this.client = Client(host, port, proto, TcpSessionFactory(proxy))
        client.session.setFlag(MinecraftConstants.AUTH_PROXY_KEY, proxy)

        client.session.addListener(object : SessionAdapter() {
            override fun packetReceived(event: PacketReceivedEvent) {
                handlePacketIn(event.getPacket())
            }

            override fun disconnected(event: DisconnectedEvent) {
                handler.onDisconnect(event.reason)
            }
        })

        client.session.connect()
    }

    override fun disconnect() {
        client.session.disconnect("")
        handler.onDisconnect("client disconnect", true)
    }

    private fun handlePacketIn(pk: MinecraftPacket) {
        if (pk is ServerChunkDataPacket) {
            return
        }
        when(pk) {
            is ServerJoinGamePacket -> {
                handler.onJoinGame(pk.entityId, CommonConverter.gamemode(pk.gameMode), CommonConverter.difficulty(pk.difficulty))
                client.session.send(ClientSettingsPacket("en_US", 8, ChatVisibility.FULL, true, SkinPart.values(), Hand.MAIN_HAND))
                client.session.send(ClientPluginMessagePacket("MC|Brand", "vanilla".toByteArray()))
                handler.onConnected()
            }
            is ServerPlayerPositionRotationPacket -> {
                if(handler.onTeleport(pk.x, pk.y, pk.z, pk.yaw, pk.pitch)) {
                    client.session.send(ClientTeleportConfirmPacket(pk.teleportId))
                    client.session.send(ClientPlayerPositionRotationPacket(false, pk.x, pk.y, pk.z, pk.yaw, pk.pitch))
                }
            }
            is ServerChunkDataPacket -> {

            }
        }
//        println(pk)
    }
}