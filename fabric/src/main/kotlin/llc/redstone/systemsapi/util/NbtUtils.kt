package llc.redstone.systemsapi.util

import net.benwoodworth.knbt.*
import net.minecraft.nbt.NbtByte as McByte
import net.minecraft.nbt.NbtByteArray as McByteArray
import net.minecraft.nbt.NbtCompound as McCompound
import net.minecraft.nbt.NbtDouble as McDouble
import net.minecraft.nbt.NbtElement as McElement
import net.minecraft.nbt.NbtFloat as McFloat
import net.minecraft.nbt.NbtInt as McInt
import net.minecraft.nbt.NbtIntArray as McIntArray
import net.minecraft.nbt.NbtList as McList
import net.minecraft.nbt.NbtLong as McLong
import net.minecraft.nbt.NbtLongArray as McLongArray
import net.minecraft.nbt.NbtShort as McShort
import net.minecraft.nbt.NbtString as McString

object NbtUtils {

    // ── knbt → Minecraft ──

    fun toMinecraftNbt(tag: NbtTag): McElement = when (tag) {
        is NbtByte      -> McByte.of(tag.value)
        is NbtShort     -> McShort.of(tag.value)
        is NbtInt       -> McInt.of(tag.value)
        is NbtLong      -> McLong.of(tag.value)
        is NbtFloat     -> McFloat.of(tag.value)
        is NbtDouble    -> McDouble.of(tag.value)
        is NbtString    -> McString.of(tag.value)
        is NbtByteArray -> McByteArray(tag.toByteArray())
        is NbtIntArray  -> McIntArray(tag.toIntArray())
        is NbtLongArray -> McLongArray(tag.toLongArray())
        is NbtList<*>   -> toMinecraftList(tag)
        is NbtCompound  -> toMinecraftCompound(tag)
    }

    fun toMinecraftCompound(nbt: NbtCompound): McCompound {
        val mc = McCompound()
        for ((key, value) in nbt) {
            mc.put(key, toMinecraftNbt(value))
        }
        return mc
    }

    private fun toMinecraftList(list: NbtList<*>): McList {
        val mc = McList()
        for (element in list) {
            mc.add(toMinecraftNbt(element))
        }
        return mc
    }

    // ── Minecraft → knbt ──

    fun fromMinecraftNbt(tag: McElement): NbtTag = when (tag) {
        is McByte      -> NbtByte(tag.byteValue())
        is McShort     -> NbtShort(tag.shortValue())
        is McInt       -> NbtInt(tag.intValue())
        is McLong      -> NbtLong(tag.longValue())
        is McFloat     -> NbtFloat(tag.floatValue())
        is McDouble    -> NbtDouble(tag.doubleValue())
        is McString    -> NbtString(tag.value)
        is McByteArray -> NbtByteArray(tag.byteArray)
        is McIntArray  -> NbtIntArray(tag.intArray)
        is McLongArray -> NbtLongArray(tag.longArray)
        is McList      -> fromMinecraftList(tag)
        is McCompound  -> fromMinecraftCompound(tag)
        else -> error("Unsupported NBT element type: ${tag::class}")
    }

    fun fromMinecraftCompound(mc: McCompound): NbtCompound {
        return NbtCompound(buildMap {
            for (key in mc.keys) {
                put(key, fromMinecraftNbt(mc.get(key)!!))
            }
        })
    }

    private fun fromMinecraftList(mc: McList): NbtList<NbtTag> {
        val elements = mc.map { fromMinecraftNbt(it) }
        return NbtList.of(*elements.toTypedArray())
    }
}