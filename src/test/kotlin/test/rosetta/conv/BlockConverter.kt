package test.rosetta.conv

import com.github.steveice10.mc.protocol.data.game.world.block.BlockState
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import me.liuli.rosetta.world.block.AxisAlignedBB
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.block.MultipleBB
import test.rosetta.loadJsonFromWeb

/**
 * block data from mc-data
 * https://github.com/PrismarineJS/minecraft-data
 */
object BlockConverter {

    val collisionJson: JsonObject
    val blocksJson = mutableMapOf<Int, JsonObject>()

    private val shapes = mutableMapOf<Int, AxisAlignedBB?>()
    private val blockCache = mutableMapOf<Int, Block>()

    init {
        val collision = loadJsonFromWeb("https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/1.12/blockCollisionShapes.json", "collision.json").asJsonObject
        collision.getAsJsonObject("shapes").entrySet().forEach {
            val arr = it.value.asJsonArray
            shapes[it.key.toInt()] = if(arr.size() == 0) {
                null
            } else if(arr.size() == 1) {
                parseBB(arr[0].asJsonArray)
            } else {
                MultipleBB(*(arr.map { parseBB(it.asJsonArray) }.toTypedArray()))
            }
        }
        collisionJson = collision.getAsJsonObject("blocks")

        val blocks = loadJsonFromWeb("https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/1.12/blocks.json", "blocks.json").asJsonArray
        blocks.forEach {
            it.asJsonObject.also {
                blocksJson[it.get("id").asInt] = it
            }
        }

    }

    private fun parseBB(json: JsonArray): AxisAlignedBB {
        return AxisAlignedBB(
            json[0].asDouble,
            json[1].asDouble,
            json[2].asDouble,
            json[3].asDouble,
            json[4].asDouble,
            json[5].asDouble
        )
    }

    fun conv(block: BlockState): Block {
        val newId = block.id shl 4 or block.data
        if(blockCache.containsKey(newId)) {
            return blockCache[newId]!!
        }
        val data = blocksJson[block.id] ?: blocksJson[0]!!.also {
            println("unknown block: $block")
        }
        val harvest = if(data.has("harvestTools")) {
            data.getAsJsonObject("harvestTools").keySet().map { it.toInt() }.toTypedArray()
        } else null
        val mat = if (block.id == 0) {
            Block.Material.AIR
        } else if(data.has("material")) {
            val matName = data.get("material").asString
            Block.Material.values().firstOrNull { it.name.equals(matName, true) } ?: Block.Material.OTHER
        } else {
            Block.Material.OTHER
        }
        val bbData = collisionJson.get(data.get("name").asString)

        val bb = if(bbData == null) {
            null
        } else if(bbData.isJsonArray) {
            bbData.asJsonArray.get(block.data)?.asInt?.let {
                shapes[it]
            }
        } else {
            shapes[bbData.asInt]
        }
        val blockResult = Block(newId, mat,
            if(data.has("hardness") && data.get("hardness") !is JsonNull) data.get("hardness").asFloat else 0f,
            data.get("diggable").asBoolean, harvest, bb)
        blockCache[newId] = blockResult
        return blockResult
    }
}