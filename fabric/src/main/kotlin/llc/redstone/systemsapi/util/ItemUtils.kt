package llc.redstone.systemsapi.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import kotlin.jvm.optionals.getOrNull

object ItemUtils {

    data class ItemSelector(
        val name: NameMatch? = null,
        val item: ItemMatch? = null,
        val extra: ((ItemStack) -> Boolean)? = null,
    ) {
        fun toPredicate(): (ItemStack) -> Boolean = { stack ->
            val nameOk = name?.matches(stack.name.string) ?: true
            val itemOk = item?.matches(stack.item) ?: true
            val extraOk = extra?.invoke(stack) ?: true

            nameOk && itemOk && extraOk
        }
    }

    sealed interface NameMatch {
        data class NameExact(val value: String) : NameMatch
        data class NameWithin(val values: List<String>) : NameMatch
        data class NameContains(val value: String) : NameMatch

        fun matches(actual: String): Boolean = when (this) {
            is NameExact -> actual == this.value
            is NameWithin -> this.values.contains(actual)
            is NameContains -> actual.contains(this.value)
        }
    }

    sealed interface ItemMatch {
        data class ItemExact(val item: Item) : ItemMatch
        data class ItemWithin(val items: List<Item>) : ItemMatch

        fun matches(actual: Item): Boolean = when (this) {
            is ItemExact -> this.item == actual
            is ItemWithin -> this.items.contains(actual)
        }
    }

    fun toNBT(itemStack: ItemStack): NbtCompound {
        return ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, itemStack).result()
            .getOrNull()?.asCompound()?.getOrNull()
            ?: throw IllegalStateException("Could not convert item to nbt")
    }

    fun createFromNBT(nbt: NbtCompound): ItemStack {
        return ItemStack.CODEC.decode(NbtOps.INSTANCE, nbt).result().getOrNull()?.first
            ?: throw IllegalArgumentException("Failed to decode ItemStack from NBT")
    }

    fun createFromJsonString(json: String): ItemStack {
        val jsonObj = JsonParser.parseString(json).asJsonObject
        val nbt = createNbtFromJsonObject(jsonObj)
        if (nbt !is NbtCompound) throw IllegalArgumentException("Top level JSON element must be an object")
        return createFromNBT(nbt)
    }

    private fun createNbtFromJsonObject(asJsonObject: JsonObject): NbtElement {
        val nbt = NbtCompound()
        for ((key, value) in asJsonObject.entrySet()) {
            if (value.isJsonObject) {
                nbt.put(key, createNbtFromJsonObject(value.asJsonObject))
            } else if (value.isJsonPrimitive) {
                val prim = value.asJsonPrimitive
                when {
                    prim.isBoolean -> nbt.putBoolean(key, prim.asBoolean)
                    prim.isNumber -> nbt.putDouble(key, prim.asDouble)
                    prim.isString -> nbt.putString(key, prim.asString)
                }
            } else if (value.isJsonArray) {
                nbt.put(key, createNbtFromJsonArray(value.asJsonArray))
            }
        }
        return nbt
    }

    private fun createNbtFromJsonArray(asJsonArray: JsonArray): NbtElement {
        val listNbt = NbtCompound()
        for ((index, element) in asJsonArray.withIndex()) {
            if (element.isJsonObject) {
                listNbt.put("$index", createNbtFromJsonObject(element.asJsonObject))
            } else if (element.isJsonPrimitive) {
                val prim = element.asJsonPrimitive
                when {
                    prim.isBoolean -> listNbt.putBoolean("$index", prim.asBoolean)
                    prim.isNumber -> listNbt.putDouble("$index", prim.asDouble)
                    prim.isString -> listNbt.putString("$index", prim.asString)
                }
            } else if (element.isJsonArray) {
                listNbt.put("$index", createNbtFromJsonArray(element.asJsonArray))
            }
        }
        return listNbt
    }
}