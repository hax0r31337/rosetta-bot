package me.liuli.rosetta.world

import me.liuli.rosetta.entity.Entity
import me.liuli.rosetta.util.vec.Vec3i
import me.liuli.rosetta.world.block.AxisAlignedBB
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.block.ComplexShape
import me.liuli.rosetta.world.data.*
import kotlin.math.ceil
import java.util.*
import kotlin.math.floor

class World {

    val entities = mutableMapOf<Int, Entity>()
    // TODO: tile entities
    val playerList = mutableMapOf<UUID, NetworkPlayerInfo>()
    var playerListInfo = Pair("", "")
    val bossBar = mutableMapOf<UUID, BossBar>()

    var dimension = 0
    val border = WorldBorder()

    // scoreboard
    val scoreboard = mutableMapOf<String, Scoreboard>()
    val teams = mutableMapOf<String, Team>()
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
        entities.forEach { (_, e) -> e.tick() }
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

    fun findTeam(player: String): Team? {
        return teams.values.firstOrNull { it.players.contains(player) }
    }

    fun getSurroundingBBs(queryBB: AxisAlignedBB): List<AxisAlignedBB> {
        return getSurroundingBBs(queryBB) { _, _, _, _ -> true }
    }

    fun getSurroundingBBs(queryBB: AxisAlignedBB, blockOk: (Block, Int, Int, Int) -> Boolean): List<AxisAlignedBB> {
        val list = mutableListOf<AxisAlignedBB>()

        for (y in (floor(queryBB.minY) - 1).toInt()..ceil(queryBB.maxY).toInt()) {
            for (x in floor(queryBB.minX).toInt()..ceil(queryBB.maxX).toInt()) {
                for (z in floor(queryBB.minZ).toInt()..ceil(queryBB.maxZ).toInt()) {
                    val shape = getBlockAt(x, y, z)?.let { if(blockOk(it, x, y, z)) it else null }?.shape
                    if (shape is ComplexShape) {
                        for (subshape in shape.shapes) {
                            list.add(AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), subshape))
                        }
                    } else if (shape != null) {
                        list.add(AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), shape))
                    }
                }
            }
        }

        return list
    }

    fun getSurroundingBlocks(queryBB: AxisAlignedBB): List<Vec3i> {
        return getSurroundingBlocks(queryBB) { _, _, _, _ -> true }
    }

    fun getSurroundingBlocks(queryBB: AxisAlignedBB, blockOk: (Block, Int, Int, Int) -> Boolean): List<Vec3i> {
        val list = mutableListOf<Vec3i>()

        for (y in (floor(queryBB.minY) - 1).toInt()..ceil(queryBB.maxY).toInt()) {
            for (x in floor(queryBB.minX).toInt()..ceil(queryBB.maxX).toInt()) {
                for (z in floor(queryBB.minZ).toInt()..ceil(queryBB.maxZ).toInt()) {
                    getBlockAt(x, y, z)?.also {
                        if (blockOk(it, x, y, z)) {
                            list.add(Vec3i(x, y, z))
                        }
                    }
                }
            }
        }

        return list
    }
}