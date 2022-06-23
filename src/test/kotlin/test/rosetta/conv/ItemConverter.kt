package test.rosetta.conv

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack
import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag
import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag
import com.github.steveice10.opennbt.tag.builtin.ListTag
import com.github.steveice10.opennbt.tag.builtin.Tag
import com.github.steveice10.opennbt.tag.builtin.custom.LongArrayTag
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.liuli.rosetta.world.item.Item
import test.rosetta.loadJsonFromWeb
import java.util.Base64

object ItemConverter {

    private val itemCache = mutableMapOf<Int, ItemCache>()

    init {
        val items = loadJsonFromWeb("https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/1.12/items.json", "items.json").asJsonArray
        val types = Item.Type.values().filter { it != Item.Type.ITEM && it != Item.Type.BLOCK }
        itemCache[0] = ItemCache("air", Item.Type.ITEM)
        items.forEach {
            val obj = it.asJsonObject
            val id = obj.get("id").asInt
            val name = obj.get("name").asString
            var type = types.firstOrNull { name.contains(it.name, true) } ?: if(id <= 255) Item.Type.BLOCK else Item.Type.ITEM
            itemCache[id] = ItemCache(name, type)
        }
    }

    fun conv(stack: ItemStack): Item {
        val cache = itemCache[stack.id] ?: itemCache[1]!!
        val id = stack.id
        return Item(id, stack.amount, stack.data, cache.type).also {
            if (stack.nbt != null) {
                it.compoundTag = compoundTagToJson(stack.nbt)
            }
        }
    }

    fun compoundTagToJson(nbt: CompoundTag): JsonObject {
        val obj = JsonObject()
        nbt.forEach {
            obj.add(it.name, tagToJson(it) ?: return@forEach)
        }
        return obj
    }

    fun tagToJson(tag: Tag): JsonElement? {
        val value = tag.value
        return if (value is Number) {
            JsonPrimitive(value)
        } else if (value is Char) {
            JsonPrimitive(value)
        } else if (value is String) {
            JsonPrimitive(value)
        } else if (value is Boolean) {
            JsonPrimitive(value)
        } else if (tag is ListTag) {
            val arr = JsonArray()
            tag.value.forEach { arr.add(tagToJson(it) ?: return@forEach) }
            arr
        } else if (tag is CompoundTag) {
            compoundTagToJson(tag)
        } else if (tag is IntArrayTag) {
            val arr = JsonArray()
            tag.value.forEach { arr.add(it) }
            arr
        } else if (tag is LongArrayTag) {
            val arr = JsonArray()
            tag.value.forEach { arr.add(it) }
            arr
        } else if (tag is ByteArrayTag) {
            JsonPrimitive("bytearray: ${Base64.getEncoder().encodeToString(tag.value)}")
        } else null
    }

    class ItemCache(val name: String, val type: Item.Type)
}