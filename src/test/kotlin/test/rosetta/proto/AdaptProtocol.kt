package test.rosetta.proto

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.MinecraftConstants
import com.github.steveice10.mc.protocol.data.game.*
import com.github.steveice10.mc.protocol.data.game.chunk.Column
import com.github.steveice10.mc.protocol.data.game.entity.attribute.Attribute
import com.github.steveice10.mc.protocol.data.game.entity.attribute.AttributeType
import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position
import com.github.steveice10.mc.protocol.data.game.entity.player.*
import com.github.steveice10.mc.protocol.data.game.scoreboard.ObjectiveAction
import com.github.steveice10.mc.protocol.data.game.scoreboard.ScoreboardAction
import com.github.steveice10.mc.protocol.data.game.scoreboard.ScoreboardPosition
import com.github.steveice10.mc.protocol.data.game.scoreboard.TeamAction
import com.github.steveice10.mc.protocol.data.game.setting.ChatVisibility
import com.github.steveice10.mc.protocol.data.game.setting.SkinPart
import com.github.steveice10.mc.protocol.data.game.world.block.BlockState
import com.github.steveice10.mc.protocol.data.game.world.notify.ClientNotification
import com.github.steveice10.mc.protocol.data.game.world.notify.RainStrengthValue
import com.github.steveice10.mc.protocol.data.game.world.notify.ThunderStrengthValue
import com.github.steveice10.mc.protocol.packet.MinecraftPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientPluginMessagePacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientResourcePackStatusPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientSettingsPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.player.*
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientConfirmTransactionPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.*
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.*
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerSetExperiencePacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnGlobalEntityPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerDisplayScoreboardPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerScoreboardObjectivePacket
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerTeamPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerUpdateScorePacket
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerConfirmTransactionPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.world.*
import com.github.steveice10.packetlib.Client
import com.github.steveice10.packetlib.event.session.DisconnectedEvent
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent
import com.github.steveice10.packetlib.event.session.SessionAdapter
import com.github.steveice10.packetlib.tcp.TcpSessionFactory
import me.liuli.rosetta.bot.BotProtocolHandler
import me.liuli.rosetta.bot.MinecraftProtocol
import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.entity.EntityLiving
import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.world.Chunk
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.data.*
import test.rosetta.conv.CommonConverter
import test.rosetta.event.PacketReceiveEvent
import java.net.Proxy

class AdaptProtocol : MinecraftProtocol {

    private lateinit var handler: BotProtocolHandler
    private lateinit var client: Client

    lateinit var packetProcessor: PacketProcess

    override fun setHandler(handler: BotProtocolHandler) {
        this.handler = handler
    }

    override fun connect(host: String, port: Int, proxy: Proxy) {
        if (!this::handler.isInitialized) {
            throw IllegalStateException("handler is not initialized, please call setHandler first")
        }
        val account = handler.bot.account
        val proto = com.github.steveice10.mc.protocol.MinecraftProtocol(GameProfile(account.uuid, account.username), account.accessToken)
        this.client = Client(host, port, proto, TcpSessionFactory(CommonConverter.proxy(proxy)))
        client.session.setFlag(MinecraftConstants.AUTH_PROXY_KEY, proxy)

        packetProcessor = PacketProcess(handler, client)

        client.session.addListener(object : SessionAdapter() {
            override fun packetReceived(event: PacketReceivedEvent) {
                val myEvent = PacketReceiveEvent(event.getPacket())
                handler.bot.emit(myEvent)
                if (!myEvent.isCancelled) {
                    packetProcessor.handlePacketIn(myEvent.packet)
                }
            }

            override fun disconnected(event: DisconnectedEvent) {
                handler.onDisconnect(event.reason)
                if (event.cause != null) {
                    event.cause.printStackTrace()
                }
            }
        })

        client.session.connect()
    }

    override fun disconnect() {
        if (this::client.isInitialized) {
            client.session.disconnect("")
            handler.onDisconnect("client disconnect", true)
        }
    }

    override fun swingItem() {
        client.session.send(ClientPlayerSwingArmPacket(Hand.MAIN_HAND))
    }

    // movement
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0
    private var lastYaw = 0f
    private var lastPitch = 0f
    private var positionUpdateTicks = 0
    private var lastSprint = false
    private var lastSneak = false

    override fun move(x: Double, y: Double, z: Double, yawIn: Float, pitchIn: Float, onGround: Boolean, sprinting: Boolean, sneaking: Boolean) {
        if(sprinting != lastSprint) {
            if (sprinting) {
                client.session.send(ClientPlayerStatePacket(handler.bot.player.id, PlayerState.START_SPRINTING))
            } else {
                client.session.send(ClientPlayerStatePacket(handler.bot.player.id, PlayerState.STOP_SPRINTING))
            }
            lastSprint = sprinting
        }
        if(sneaking != lastSneak) {
            if (sneaking) {
                client.session.send(ClientPlayerStatePacket(handler.bot.player.id, PlayerState.START_SNEAKING))
            } else {
                client.session.send(ClientPlayerStatePacket(handler.bot.player.id, PlayerState.STOP_SNEAKING))
            }
            lastSneak = sneaking
        }

        // fix GCD sensitivity to bypass some anti-cheat measures
        val sensitivity = 0.5f
        val f = sensitivity * 0.6F + 0.2F
        val gcd = f * f * f * 1.2F

        // fix yaw
        var deltaYaw = yawIn - lastYaw
        deltaYaw -= deltaYaw % gcd

        // fix pitch
        var deltaPitch = pitchIn - lastPitch
        deltaPitch -= deltaPitch % gcd

        val yaw = lastYaw + deltaYaw
        val pitch = lastPitch + deltaPitch

        val xDiff = x - lastX
        val yDiff = y - lastY
        val zDiff = z - lastZ
        val moved = (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff) > 9.0E-4 || this.positionUpdateTicks >= 20
        val rotated = (yaw - lastYaw) != 0f || (pitch - lastPitch) != 0f

        // send packet to server
        if (moved && rotated) {
            client.session.send(ClientPlayerPositionRotationPacket(onGround, x, y, z, yaw, pitch))
        } else if (moved) {
            client.session.send(ClientPlayerPositionPacket(onGround, x, y, z))
        } else if (rotated) {
            client.session.send(ClientPlayerRotationPacket(onGround, yaw, pitch))
        } else {
//            client.session.send(ClientPlayerMovementPacket(onGround))
        }

        this.positionUpdateTicks++

        if (moved) {
            this.lastX = x
            this.lastY = y
            this.lastZ = z
            this.positionUpdateTicks = 0
        }
        if (rotated) {
            this.lastYaw = yaw
            this.lastPitch = pitch
        }
    }

    override fun heldItemChange(slot: Int) {
        client.session.send(ClientPlayerChangeHeldItemPacket(slot))
    }

    override fun abilities(invincible: Boolean, flying: Boolean, allowFlying: Boolean, walkSpeed: Float, flySpeed: Float) {
        client.session.send(ClientPlayerAbilitiesPacket(invincible, allowFlying, flying,
            handler.bot.world.gamemode == EnumGameMode.CREATIVE, walkSpeed, flySpeed))
    }

    override fun dig(x: Int, y: Int, z: Int, facing: EnumBlockFacing, mode: Int) {
        client.session.send(ClientPlayerActionPacket(when(mode) {
            0 -> PlayerAction.START_DIGGING
            1 -> PlayerAction.CANCEL_DIGGING
            2 -> PlayerAction.FINISH_DIGGING
            else -> throw IllegalArgumentException("invalid mode: $mode")
        }, Position(x, y, z), CommonConverter.enumBlockFacing(facing)))
    }

    override fun useItem() {
        client.session.send(ClientPlayerUseItemPacket(Hand.MAIN_HAND))
    }

    override fun useItem(x: Int, y: Int, z: Int, facing: EnumBlockFacing) {
        client.session.send(ClientPlayerPlaceBlockPacket(Position(x, y, z), CommonConverter.enumBlockFacing(facing), Hand.MAIN_HAND,
            0f, 0f, 0f)) // TODO: calculate cursor position
    }

    override fun useItem(entityId: Int, mode: Int) {
        client.session.send(ClientPlayerInteractEntityPacket(entityId, when(mode) {
            0 -> InteractAction.ATTACK
            1 -> InteractAction.INTERACT
            2 -> InteractAction.INTERACT_AT
            else -> throw IllegalArgumentException("invalid mode: $mode")
        }))
    }

    override fun respawn() {
        client.session.send(ClientRequestPacket(ClientRequest.RESPAWN))
    }

    override fun chat(message: String) {
        client.session.send(ClientChatPacket(message))
    }
}