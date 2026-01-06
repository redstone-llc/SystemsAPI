package llc.redstone.systemsapi.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import kotlin.jvm.optionals.getOrNull

object ItemUtils {
    fun toNBT(itemStack: ItemStack): NbtCompound {
        return ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, itemStack).result()
            .getOrNull()?.asCompound()?.getOrNull()
            ?: throw IllegalStateException("Could not convert item to nbt")
    }

    fun createFromNBT(nbt: NbtCompound): ItemStack {
        return ItemStack.CODEC.decode(NbtOps.INSTANCE, nbt).result().getOrNull()?.first
            ?: throw IllegalArgumentException("Failed to decode ItemStack from NBT")
    }
}