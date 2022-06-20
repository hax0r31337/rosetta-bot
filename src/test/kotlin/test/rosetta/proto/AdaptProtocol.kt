package test.rosetta.proto

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.MinecraftConstants
import com.github.steveice10.mc.protocol.data.game.*
import com.github.steveice10.mc.protocol.data.game.entity.attribute.Attribute
import com.github.steveice10.mc.protocol.data.game.entity.attribute.AttributeType
import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand
import com.github.steveice10.mc.protocol.data.game.scoreboard.ObjectiveAction
import com.github.steveice10.mc.protocol.data.game.scoreboard.ScoreboardAction
import com.github.steveice10.mc.protocol.data.game.scoreboard.ScoreboardPosition
import com.github.steveice10.mc.protocol.data.game.scoreboard.TeamAction
import com.github.steveice10.mc.protocol.data.game.setting.ChatVisibility
import com.github.steveice10.mc.protocol.data.game.setting.SkinPart
import com.github.steveice10.mc.protocol.data.game.world.notify.ClientNotification
import com.github.steveice10.mc.protocol.data.game.world.notify.RainStrengthValue
import com.github.steveice10.mc.protocol.data.game.world.notify.ThunderStrengthValue
import com.github.steveice10.mc.protocol.packet.MinecraftPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientPluginMessagePacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientResourcePackStatusPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientSettingsPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerMovementPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerRotationPacket
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
import me.liuli.rosetta.world.data.BossBar
import me.liuli.rosetta.world.data.EnumTitleType
import me.liuli.rosetta.world.data.NetworkPlayerInfo
import me.liuli.rosetta.world.data.Scoreboard
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
        this.client = Client(host, port, proto, TcpSessionFactory(CommonConverter.proxy(proxy)))
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
        if (this::client.isInitialized) {
            client.session.disconnect("")
            handler.onDisconnect("client disconnect", true)
        }
    }

    // movement
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0
    private var lastYaw = 0f
    private var lastPitch = 0f
    private var positionUpdateTicks = 0

    // title
    private var titleFadeIn = 10
    private var titleStay = 70
    private var titleFadeOut = 20

    override fun move(x: Double, y: Double, z: Double, yawIn: Float, pitchIn: Float, onGround: Boolean) {
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
            client.session.send(ClientPlayerMovementPacket(onGround))
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

    override fun chat(message: String) {
        client.session.send(ClientChatPacket(message))
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
            is ServerSpawnObjectPacket -> {
                val entity = Entity()
                entity.id = pk.entityId
                entity.position.set(pk.x, pk.y, pk.z)
                entity.rotation.set(pk.yaw, pk.pitch)
                handler.spawnEntity(entity)
            }
            is ServerSpawnGlobalEntityPacket -> {
                val entity = Entity()
                entity.id = pk.entityId
                entity.position.set(pk.x, pk.y, pk.z)
                handler.spawnEntity(entity)
            }
            is ServerEntityVelocityPacket -> {
                handler.onSetMotion(pk.entityId, pk.motionX.toFloat(), pk.motionY.toFloat(), pk.motionZ.toFloat())
            }
            is ServerEntityMetadataPacket -> {
                handleMetadata(pk.entityId, pk.metadata)
            }
            is ServerSpawnPlayerPacket -> {
                val player = EntityPlayer()
                player.id = pk.entityId
                player.uuid = pk.uuid
                player.position.set(pk.x, pk.y, pk.z)
                player.rotation.set(pk.yaw, pk.pitch)
                handler.spawnEntity(player)
                handleMetadata(pk.entityId, pk.metadata)
            }
            is ServerEntityTeleportPacket -> {
                handler.onTeleport(pk.entityId, pk.x, pk.y, pk.z, pk.yaw, pk.pitch, pk.isOnGround)
            }
//            is ServerPlayerChangeHeldItemPacket
            is ServerEntityMovementPacket -> {
                val entity = handler.bot.world.entities[pk.entityId] ?: return
                val pos = entity.position
                if (pk is ServerEntityPositionPacket) {
                    handler.onMovement(pk.entityId, pk.isOnGround, pos.x + pk.movementX, pos.y + pk.movementY, pos.z + pk.movementZ)
                } else if (pk is ServerEntityPositionRotationPacket) {
                    handler.onMovement(pk.entityId, pk.isOnGround, pos.x + pk.movementX, pos.y + pk.movementY, pos.z + pk.movementZ, pk.yaw, pk.pitch)
                } else if (pk is ServerEntityRotationPacket) {
                    handler.onMovement(pk.entityId, pk.isOnGround, pk.yaw, pk.pitch)
                } else {
                    handler.onMovement(pk.entityId, pk.isOnGround)
                }
            }
            is ServerEntityDestroyPacket -> {
                pk.entityIds.forEach { handler.onRemoveEntity(it) }
            }
            is ServerPlayerPositionRotationPacket -> {
                if(handler.onPlayerTeleport(pk.x, pk.y, pk.z, pk.yaw, pk.pitch)) {
                    client.session.send(ClientTeleportConfirmPacket(pk.teleportId))
                    client.session.send(ClientPlayerPositionRotationPacket(false, pk.x, pk.y, pk.z, pk.yaw, pk.pitch))
                }
            }
//            is ServerMultiBlockChangePacket
            is ServerChunkDataPacket -> {

            }
//            is ServerUnloadChunkPacket
//            is ServerBlockChangePacket
//            is ServerEntityCollectItemPacket
            is ServerChatPacket -> {
                handler.onChat(pk.message.fullText, pk.message.toJsonString())
            }
//            is ServerEntityAnimationPacket
//            is ServerPlayerUseBedPacket
            is ServerSpawnMobPacket -> {
                val entity = EntityLiving()
                entity.id = pk.entityId
                entity.position.set(pk.x, pk.y, pk.z)
                entity.rotation.set(pk.yaw, pk.pitch)
                handler.spawnEntity(entity)
                handleMetadata(pk.entityId, pk.metadata)
            }
            is ServerUpdateTimePacket -> {
                handler.onTimeUpdate(pk.time)
            }
            is ServerSpawnPositionPacket -> {
                handler.onSpawnPositionChange(pk.position.x, pk.position.y, pk.position.z)
            }
//            is ServerEntitySetPassengersPacket
//            is ServerEntityAttachPacket
//            is ServerEntityStatusPacket
            is ServerPlayerHealthPacket -> {
                handler.onHealthChange(handler.bot.player.id, pk.health, handler.bot.player.maxHealth, handler.bot.player.absorption)
                handler.onFoodChange(pk.food.toFloat(), pk.saturation)
            }
            is ServerPlayerSetExperiencePacket -> {
                handler.onExperienceChange(pk.slot, pk.level)
            }
//            is ServerRespawnPacket
            is ServerExplosionPacket -> {
                if (pk.x == 0f && pk.y == 0f && pk.z == 0f) {
                    return
                }
                val entity = handler.bot.player
                handler.onSetMotion(entity.id, entity.motion.x, entity.motion.y, entity.motion.z)
            }
//            is ServerOpenWindowPacket
//            is ServerSetSlotPacket
            is ServerConfirmTransactionPacket -> {
                if (!pk.accepted) {
                    client.session.send(ClientConfirmTransactionPacket(pk.windowId, pk.actionId, true))
                }
            }
//            is ServerWindowItemsPacket
//            is ServerOpenTileEntityEditorPacket
//            is ServerUpdateTileEntityPacket
//            is ServerWindowPropertyPacket
//            is ServerEntityEquipmentPacket
//            is ServerCloseWindowPacket
//            is ServerBlockValuePacket
//            is ServerBlockBreakAnimPacket
            is ServerNotifyClientPacket -> {
                if(pk.notification == ClientNotification.RAIN_STRENGTH) {
                    handler.onWeatherUpdate((pk.value as RainStrengthValue).strength, handler.bot.world.thunderStrength)
                } else if (pk.notification == ClientNotification.THUNDER_STRENGTH) {
                    handler.onWeatherUpdate(handler.bot.world.rainStrength, (pk.value as ThunderStrengthValue).strength)
                }
            }
//            is ServerMapDataPacket
//            is ServerPlayEffectPacket
//            is ServerAdvancementsPacket
//            is ServerAdvancementTabPacket
//            is ServerStatisticsPacket
//            is ServerUnlockRecipesPacket
            is ServerEntityEffectPacket -> {

            }
//            is ServerCombatPacket
            is ServerDifficultyPacket -> {
                handler.onDifficultyChange(CommonConverter.difficulty(pk.difficulty))
            }
//            is ServerSwitchCameraPacket -> {
//
//            }
//            is ServerWorldBorderPacket
            is ServerTitlePacket -> {
                when(pk.action) {
                    TitleAction.TITLE -> handler.onTitle(EnumTitleType.TITLE, pk.title?.fullText ?: "", this.titleFadeIn, this.titleStay, this.titleFadeOut)
                    TitleAction.SUBTITLE -> handler.onTitle(EnumTitleType.SUBTITLE, pk.subtitle?.fullText ?: "", this.titleFadeIn, this.titleStay, this.titleFadeOut)
                    TitleAction.ACTION_BAR -> handler.onTitle(EnumTitleType.ACTIONBAR, pk.actionBar?.fullText ?: "", this.titleFadeIn, this.titleStay, this.titleFadeOut)
                    TitleAction.RESET -> {
                        handler.onTitle(EnumTitleType.TITLE, "", -1, -1, -1)
                        handler.onTitle(EnumTitleType.SUBTITLE, "", -1, -1, -1)
                        this.titleFadeIn = 10
                        this.titleStay = 70
                        this.titleFadeOut = 20
                    }
                    TitleAction.CLEAR -> {
                        handler.onTitle(EnumTitleType.TITLE, "", -1, -1, -1)
                        handler.onTitle(EnumTitleType.SUBTITLE, "", -1, -1, -1)
                    }
                    TitleAction.TIMES -> {
                        this.titleFadeIn = pk.fadeIn
                        this.titleStay = pk.stay
                        this.titleFadeOut = pk.fadeOut
                    }
                }
            }
            is ServerPlayerListDataPacket -> {
                handler.onPlayerListInfoUpdate(pk.header.fullText, pk.footer.fullText)
            }
            is ServerEntityRemoveEffectPacket -> {

            }
            is ServerPlayerListEntryPacket -> {
                handlePlayerList(pk.action, pk.entries)
            }
            is ServerPlayerAbilitiesPacket -> {
                handler.onAbilitiesChange(pk.flying, pk.canFly, pk.invincible)
                handler.onMoveSpeedChange(pk.walkSpeed, pk.flySpeed)
            }
//            is ServerTabCompletePacket
//            is ServerPlaySoundPacket
//            is ServerPlayBuiltinSoundPacket
            is ServerResourcePackSendPacket -> {
                client.session.send(ClientResourcePackStatusPacket(ResourcePackStatus.FAILED_DOWNLOAD))
            }
            is ServerBossBarPacket -> {
                var bar = handler.bot.world.bossBar[pk.uuid]
                when(pk.action) {
                    BossBarAction.ADD -> {
                        bar = BossBar(pk.uuid, pk.title.fullText, CommonConverter.bossBarColor(pk.color), pk.health)
                    }
                    BossBarAction.UPDATE_HEALTH -> {
                        bar ?: return
                        bar.health = pk.health
                    }
                    BossBarAction.UPDATE_STYLE -> {
                        bar ?: return
                        bar.color = CommonConverter.bossBarColor(pk.color)
                    }
                    BossBarAction.UPDATE_TITLE -> {
                        bar ?: return
                        bar.title = pk.title.fullText
                    }
                    BossBarAction.REMOVE -> {
                        handler.removeBossBar(pk.uuid)
                        return
                    }
                }
                handler.setBossBar(bar ?: return)
            }
//            is ServerVehicleMovePacket
//            is ServerPluginMessagePacket
            is ServerScoreboardObjectivePacket -> {
                when(pk.action) {
                    ObjectiveAction.ADD -> {
                        val sb = Scoreboard(pk.name, pk.displayName, Scoreboard.Sort.DESCENDING)
                        handler.setScoreboard(sb)
                    }
                    ObjectiveAction.REMOVE -> handler.removeScoreboard(pk.name)
                    ObjectiveAction.UPDATE -> {
                        val sb = handler.bot.world.scoreboard[pk.name] ?: return
                        sb.displayName = pk.displayName
                        handler.setScoreboard(sb)
                    }
                }
            }
            is ServerUpdateScorePacket -> {
                val sb = handler.bot.world.scoreboard[pk.objective] ?: return
                if (pk.action == ScoreboardAction.ADD_OR_UPDATE) {
                    val score = sb.score[pk.entry] ?: Scoreboard.Score(pk.entry, pk.value).also { sb.score[pk.entry] = it }
                    score.score = pk.value
                } else {
                    sb.score.remove(pk.entry)
                }
            }
            is ServerDisplayScoreboardPacket -> {
                if (pk.position == ScoreboardPosition.SIDEBAR) {
                    handler.displayScoreboard(pk.scoreboardName)
                }
            }
            is ServerTeamPacket -> {
                val sb = handler.bot.world.displayScoreboard ?: return
                if (pk.action == TeamAction.CREATE) {
                    sb.teams[pk.teamName] = Scoreboard.Team(pk.teamName, pk.displayName, pk.prefix, pk.suffix, pk.players.toMutableList())
                    return
                } else if (pk.action == TeamAction.REMOVE) {
                    sb.teams.remove(pk.teamName)
                    return
                }
                var team = sb.teams[pk.teamName] ?: return
                when(pk.action) {
                    TeamAction.ADD_PLAYER -> pk.players.forEach { team.players.add(it) }
                    TeamAction.REMOVE_PLAYER -> pk.players.forEach { team.players.remove(it) }
                    TeamAction.UPDATE -> {
                        team.displayName = pk.displayName
                        team.prefix = pk.prefix
                        team.suffix = pk.suffix
                    }
                }
            }
//            is ServerSpawnParticlePacket
            is ServerEntityPropertiesPacket -> {
                handleProperties(pk.entityId, pk.attributes)
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
        val entity = handler.bot.world.entities[entityId] ?: return
        metadata.forEach {
            if (it.id == 2) { // nametag
                entity.displayName = it.value.toString()
            }
            if (entity is EntityLiving) {
                if (it.id == 11) { // absorption
                    handler.onHealthChange(entityId, handler.bot.player.health, handler.bot.player.maxHealth, it.value as Float)
                } else if (it.id == 7) { // health
                    handler.onHealthChange(entityId, it.value as Float, handler.bot.player.maxHealth, handler.bot.player.absorption)
                } else if (it.id == 0) { // TODO pose
                }
            }
        }
    }

    private fun handleProperties(entityId: Int, properties: List<Attribute>) {
        properties.forEach {
            // TODO modifier
            when (it.type) {
                AttributeType.GENERIC_FLYING_SPEED -> {
                    if(entityId == handler.bot.player.id) {
                        handler.onMoveSpeedChange(handler.bot.player.walkSpeed, it.value.toFloat())
                    }
                }
                AttributeType.GENERIC_MOVEMENT_SPEED -> {
                    if(entityId == handler.bot.player.id) {
                        handler.onMoveSpeedChange(it.value.toFloat(), handler.bot.player.flySpeed)
                    }
                }
                AttributeType.GENERIC_MAX_HEALTH -> {
                    val entity = handler.bot.world.entities[entityId] ?: return
                    if (entity is EntityLiving) {
                        handler.onHealthChange(entityId, entity.health, it.value.toFloat(), entity.absorption)
                    }
                }
            }
        }
    }
}