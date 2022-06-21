package test.rosetta.proto

import com.github.steveice10.mc.protocol.data.game.*
import com.github.steveice10.mc.protocol.data.game.chunk.Column
import com.github.steveice10.mc.protocol.data.game.entity.attribute.Attribute
import com.github.steveice10.mc.protocol.data.game.entity.attribute.AttributeType
import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata
import com.github.steveice10.mc.protocol.data.game.entity.player.Animation
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand
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
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientPluginMessagePacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientResourcePackStatusPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientSettingsPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket
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
import me.liuli.rosetta.bot.BotProtocolHandler
import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.entity.EntityLiving
import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.world.Chunk
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.data.*
import test.rosetta.conv.CommonConverter

class PacketProcess(private val handler: BotProtocolHandler, private val client: Client) {

    // title
    private var titleFadeIn = 10
    private var titleStay = 70
    private var titleFadeOut = 20

    fun handlePacketIn(pk: MinecraftPacket) {
        when(pk) {
            is ServerJoinGamePacket -> {
                handler.onJoinGame(pk.entityId, pk.dimension)
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
            is ServerMultiBlockChangePacket -> {
                pk.records.forEach {
                    handleBlockChange(it.block, it.position.x, it.position.y, it.position.z)
                }
            }
            is ServerChunkDataPacket -> {
                handleChunk(pk.column)
            }
            is ServerUnloadChunkPacket -> {
                handler.unloadChunk(pk.x, pk.z)
            }
            is ServerBlockChangePacket -> {
                handleBlockChange(pk.record.block, pk.record.position.x, pk.record.position.y, pk.record.position.z)
            }
//            is ServerEntityCollectItemPacket
            is ServerChatPacket -> {
                if (pk.type == MessageType.NOTIFICATION) {
                    handler.onTitle(EnumTitleType.ACTIONBAR, pk.message.fullText, this.titleFadeIn, this.titleStay, this.titleFadeOut)
                } else {
                    handler.onChat(pk.message.fullText, pk.message.toJsonString())
                }
            }
            is ServerEntityAnimationPacket -> {
                if(pk.animation == Animation.SWING_ARM) {
                    handler.onSwing(pk.entityId)
                }
            }
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
            is ServerRespawnPacket -> {
                val diff = CommonConverter.difficulty(pk.difficulty)
                if (handler.bot.world.difficulty != diff) {
                    handler.onDifficultyChange(diff)
                }
                val gamemode = CommonConverter.gamemode(pk.gameMode)
                if (handler.bot.world.gamemode != gamemode) {
                    handler.onGamemodeChange(gamemode)
                }
                handler.onRespawn(pk.dimension, handler.bot.world.dimension != pk.dimension)
            }
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
                } else if (pk.notification == ClientNotification.CHANGE_GAMEMODE) {
                    handler.onGamemodeChange(CommonConverter.gamemode(pk.value as GameMode))
                }
            }
//            is ServerMapDataPacket
//            is ServerPlayEffectPacket
//            is ServerAdvancementsPacket
//            is ServerAdvancementTabPacket
//            is ServerStatisticsPacket
//            is ServerUnlockRecipesPacket
            is ServerEntityEffectPacket -> {
                handler.addEffect(pk.entityId, PotionEffect(CommonConverter.effect(pk.effect), pk.amplifier, pk.duration))
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
                handler.removeEffect(pk.entityId, CommonConverter.effect(pk.effect))
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
                if (it.id == 11 && it.value is Float) { // absorption
                    handler.onHealthChange(entityId, handler.bot.player.health, handler.bot.player.maxHealth, it.value as Float)
                } else if (it.id == 7 && it.value is Float) { // health
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

    private val airState = BlockState(0, 10)

    private fun handleChunk(column: Column) {
        val chunk = Chunk(column.x, column.z)
        var yPos = 0
        var i: Int
        column.chunks.forEach { c ->
            c ?: return@forEach
            val storage = c.blocks.storage
            val states = c.blocks.states
            val stateMode = c.blocks.bitsPerEntry <= 8
            for(y in 0 until 16) {
                for (i in 0 until 256) {
                    val id = storage.get(y shl 8 or i)
                    val state = if (stateMode) {
                        if(id in 0 until states.size) states[id] else airState
                    } else {
                        BlockState(id shr 4, id and 0xf)
                    }

                    chunk.blocks[yPos * 256 + i] = Block(state.id, state.data, CommonConverter.blockType(state.id))
                }
                yPos++
            }
        }
        handler.onChunk(chunk)
    }

    private fun handleBlockChange(state: BlockState, x: Int, y: Int, z: Int) {
        val block = Block(state.id, state.data, CommonConverter.blockType(state.id))
        handler.onBlockUpdate(x, y, z, block)
    }
}