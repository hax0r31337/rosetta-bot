package test.rosetta.conv

import com.github.steveice10.mc.protocol.data.game.world.block.BlockState
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import me.liuli.rosetta.world.block.AxisAlignedBB
import me.liuli.rosetta.world.block.Block
import me.liuli.rosetta.world.block.IBlockHardnessModifier
import me.liuli.rosetta.world.block.MultipleBB
import me.liuli.rosetta.world.item.Item
import test.rosetta.loadJsonFromWeb
import java.util.Collections

/**
 * block data from mc-data
 * https://github.com/PrismarineJS/minecraft-data
 */
object BlockConverter {

    val collisionJson: JsonObject
    val blocksInfo = mutableMapOf<Int, BlockInfo>()

    private val shapes = mutableMapOf<Int, AxisAlignedBB?>()
    private val blockCache = mutableMapOf<Int, Block>()
    private val hardnessModifiers = mutableMapOf<Block.Material, BlockHardnessModifier>()

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
            val data = it.asJsonObject
            // rewrite json to object for reduce memory usage
            blocksInfo[data.get("id").asInt] = BlockInfo(
                data.get("name").asString,
                if(data.has("material")) {
                    val matName = data.get("material").asString
                    Block.Material.values().firstOrNull { it.name.equals(matName, true) } ?: Block.Material.OTHER
                } else {
                    Block.Material.OTHER
                },
                if(data.has("hardness") && data.get("hardness") !is JsonNull) data.get("hardness").asFloat else 0f,
                data.get("diggable").asBoolean,
                if(data.has("harvestTools")) {
                    data.getAsJsonObject("harvestTools").keySet().map { it.toInt() }.toTypedArray()
                } else null
            )
        }

        val materials = loadJsonFromWeb("https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/1.12/materials.json", "materials.json").asJsonObject
        materials.entrySet().forEach { entry ->
            val material = Block.Material.values().firstOrNull { it.name.equals(entry.key, true) } ?: return@forEach
            val map = mutableMapOf<Int, Float>()
            entry.value.asJsonObject.entrySet().forEach {
                if (it.value.isJsonPrimitive) {
                    map[it.key.toInt()] = it.value.asFloat
                }
            }
            hardnessModifiers[material] = BlockHardnessModifier(Collections.unmodifiableMap(map))
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
        val newId = block.id shl 4 or (block.data and 0xF)
        if(blockCache.containsKey(newId)) {
            return blockCache[newId]!!
        }
        val data = blocksInfo[block.id] ?: blocksInfo[0]!!.also {
            println("unknown block: $block")
        }
        val mat = if (block.id == 0) {
            Block.Material.AIR
        } else {
            data.material
        }

        val bb = collisionJson.get(data.name)?.let { bbData ->
            if(bbData.isJsonArray) {
                bbData.asJsonArray.get(block.data)?.asInt?.let {
                    shapes[it]
                }
            } else {
                shapes[bbData.asInt]
            }
        }
        val blockResult = Block(newId, mat, data.name, data.hardness, data.diggable, data.harvest, bb)
        hardnessModifiers[mat]?.let {
            blockResult.hardnessModifier.add(it)
        }
        blockCache[newId] = blockResult
        return blockResult
    }

    class BlockInfo(val name: String, val material: Block.Material, val hardness: Float, val diggable: Boolean, val harvest: Array<Int>?)

    class BlockHardnessModifier(private val affectItems: Map<Int, Float>) : IBlockHardnessModifier {
        override fun getModifier(baseHardness: Float, item: Item): Float {
            return baseHardness * if (affectItems.containsKey(item.id)) {
                affectItems[item.id] ?: 1f
            } else 1f
        }
    }
}