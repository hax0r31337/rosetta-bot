package test.rosetta.protocol

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.MinecraftConstants
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction
import com.github.steveice10.mc.protocol.data.game.entity.attribute.Attribute
import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand
import com.github.steveice10.mc.protocol.data.game.setting.ChatVisibility
import com.github.steveice10.mc.protocol.data.game.setting.SkinPart
import com.github.steveice10.mc.protocol.packet.MinecraftPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientPluginMessagePacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientSettingsPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerDifficultyPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityMetadataPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPropertiesPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerSetExperiencePacket
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerSpawnPositionPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUpdateTimePacket
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerWorldBorderPacket
import com.github.steveice10.packetlib.Client
import com.github.steveice10.packetlib.event.session.DisconnectedEvent
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent
import com.github.steveice10.packetlib.event.session.SessionAdapter
import com.github.steveice10.packetlib.tcp.TcpSessionFactory
import me.liuli.rosetta.bot.BotProtocolHandler
import me.liuli.rosetta.bot.event.DisconnectEvent
import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.MinecraftProtocol
import me.liuli.rosetta.world.data.NetworkPlayerInfo
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
        when(pk) {
            is ServerJoinGamePacket -> {
                handler.onJoinGame(pk.entityId)
                handler.onGamemodeChange(CommonConverter.gamemode(pk.gameMode))
                handler.onDifficultyChange(CommonConverter.difficulty(pk.difficulty))
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
            is ServerDifficultyPacket -> {
                handler.onDifficultyChange(CommonConverter.difficulty(pk.difficulty))
            }
            is ServerChatPacket -> {
                handler.onChat(pk.message.fullText, pk.message.toJsonString())
            }
            is ServerUpdateTimePacket -> {
                handler.onTimeUpdate(pk.time)
            }
            is ServerPlayerAbilitiesPacket -> {
                handler.onAbilitiesChange(pk.flying, pk.canFly, pk.invincible)
                handler.onMoveSpeedChange(pk.walkSpeed, pk.flySpeed)
            }
            is ServerPlayerHealthPacket -> {
                handler.onHealthChange(pk.health, handler.bot.player.maxHealth, handler.bot.player.absorption)
                handler.onFoodChange(pk.food.toFloat(), pk.saturation)
            }
            is ServerSpawnPositionPacket -> {
                handler.onSpawnPositionChange(pk.position.x, pk.position.y, pk.position.z)
            }
            is ServerEntityPropertiesPacket -> {
                handleProperties(pk.entityId, pk.attributes)
            }
            is ServerEntityMetadataPacket -> {
                handleMetadata(pk.entityId, pk.metadata)
            }
            is ServerPlayerSetExperiencePacket -> {
                handler.onExperienceChange(pk.slot, pk.level)
            }
            is ServerPlayerListEntryPacket -> {
                handlePlayerList(pk.action, pk.entries)
            }
            is ServerChunkDataPacket -> {

            }
//            else -> println(pk)
        }
    }

    private fun handlePlayerList(action: PlayerListEntryAction, list: Array<PlayerListEntry>) {
        val result = mutableListOf<NetworkPlayerInfo>()
        val remove = mutableListOf<NetworkPlayerInfo>()
        list.forEach {
            var entry = handler.bot.world.playerList[it.profile.id]
            if (action == PlayerListEntryAction.ADD_PLAYER) {
                entry = NetworkPlayerInfo(it.profile.id, it.profile.name, CommonConverter.gamemode(it.gameMode), it.ping, it.displayName?.fullText)
            } else if (entry == null) {
                return@forEach // equals to java continue
            }
            when(action) {
                PlayerListEntryAction.UPDATE_GAMEMODE -> {
                    entry.gamemode = CommonConverter.gamemode(it.gameMode)
                }
                PlayerListEntryAction.UPDATE_LATENCY -> {
                    entry.latency = it.ping
                }
                PlayerListEntryAction.UPDATE_DISPLAY_NAME -> {
                    entry.displayName = it.displayName?.fullText
                }
                PlayerListEntryAction.REMOVE_PLAYER -> {
                    remove.add(entry)
                    return@forEach
                }
            }
            result.add(entry)
        }
        if (result.isNotEmpty()) {
            handler.onPlayerListUpdate(result)
        }
        if (remove.isNotEmpty()) {
            handler.onPlayerListRemove(remove)
        }
    }

    private fun handleMetadata(entityId: Int, metadata: Array<EntityMetadata>) {
//        println(entityId)
//        println(metadata)
    }

    private fun handleProperties(entityId: Int, properties: List<Attribute>) {
//        println(entityId)
//        println(properties)
    }
}