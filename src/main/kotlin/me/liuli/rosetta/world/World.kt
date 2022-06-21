package me.liuli.rosetta.world

import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.data.*
import java.util.*

class World {

    val entities = mutableMapOf<Int, Entity>()
    // TODO: tile entities
    val playerList = mutableMapOf<UUID, NetworkPlayerInfo>()
    var playerListInfo = Pair("", "")
    val bossBar = mutableMapOf<UUID, BossBar>()

    var dimension = 0

    // scoreboard
    val scoreboard = mutableMapOf<String, Scoreboard>()
    var displayScoreboardName = ""
    val displayScoreboard: Scoreboard?
        get() = scoreboard[displayScoreboardName]

    var gamemode = EnumGameMode.SURVIVAL
    var difficulty = EnumDifficulty.NORMAL

    // ambience
    var time = 0L
    var rainStrength = 0.0f
    var thunderStrength = 0.0f

    // chunk
    val chunk = mutableMapOf<Long, Chunk>()

    val spawn = Vec3i()

    var tickExisted = 0L

    fun tick() {
        time++
        tickExisted++
    }

    fun getChunkAt(x: Int, z: Int): Chunk? {
        return chunk[Chunk.code(x, z)]
    }

    fun setChunk(chunk: Chunk) {
        this.chunk[chunk.code] = chunk
    }

    fun eraseChunkAt(x: Int, z: Int) {
        chunk.remove(Chunk.code(x, z))
    }

    fun getBlockAt(x: Int, y: Int, z: Int): Block? {
        val chunk = getChunkAt(x shr 4, z shr 4) ?: return null
        return chunk.getBlockAt(x and 0xF, y, z and 0xF)
    }

    fun setBlockAt(x: Int, y: Int, z: Int, block: Block) {
        val chunk = getChunkAt(x shr 4, z shr 4) ?: return
        chunk.setBlockAt(x and 0xF, y, z and 0xF, block)
    }

    fun putChunk(chunk: Chunk) {
        this.chunk[chunk.code] = chunk
    }
}