package llc.redstone.systemsapi.util

import llc.redstone.systemsapi.SystemsAPI.MC
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket
import net.minecraft.world.GameMode
import kotlin.jvm.optionals.getOrNull

object ItemUtils {
    fun ItemStack.giveItem(slot: Int) {
        val gameMode = MC.player
            ?.gameMode ?: throw IllegalStateException("Could not determine player's game mode")
        if (gameMode != GameMode.CREATIVE) CommandUtils.runCommand("/gmc")

        val pkt = CreativeInventoryActionC2SPacket(
            slot,
            this
        )
        MC.networkHandler?.sendPacket(pkt) ?: throw IllegalStateException("Something went wrong while creating item ${item.name}")

        when (gameMode) {
            GameMode.SURVIVAL -> CommandUtils.runCommand("/gms")
            GameMode.ADVENTURE -> CommandUtils.runCommand("/gma")
            else -> {}
        }
    }

    fun ItemStack.loreLine(line: Int, color: Boolean, filter: (String) -> Boolean = { true }): String {
        val loreString = get(DataComponentTypes.LORE)
            ?.lines
            ?.getOrNull(line)
            ?.let { loreLine -> TextUtils.convertTextToString(loreLine, color) }
                         ?: throw IllegalStateException("Could not find lore line")
        return loreString.takeIf(filter)
               ?: throw IllegalStateException("Lore line '$loreString' for item $this did not pass filter")
    }

    fun ItemStack.loreLine(color: Boolean, filter: (String) -> Boolean = { true }): String? {
        val loreLines = get(DataComponentTypes.LORE)?.lines
            ?: throw IllegalStateException("Could not find lore line in item \$this which matched filter")
        val loreLine = loreLines.firstOrNull { line -> filter(TextUtils.convertTextToString(line, color)) }
            ?: return null
        return TextUtils.convertTextToString(loreLine, color)
    }

    fun ItemStack.loreLineWithIndex(color: Boolean, filter: (String) -> Boolean = { true }): Pair<String, Int>? {
        val lore = this.get(DataComponentTypes.LORE) ?: return null
        for ((index, lore) in lore.lines.withIndex()) {
            val loreString = TextUtils.convertTextToString(lore, color)
            if (filter(loreString)) return Pair(loreString, index)
        }
        return null
    }

    fun ItemStack.loreLines(color: Boolean): List<String> {
        val loreLines = get(DataComponentTypes.LORE)?.lines
            ?: return emptyList()
        return loreLines.map { TextUtils.convertTextToString(it, color) }
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

    fun placeInventory(itemStack: ItemStack, slot: Int) {
        val player = MinecraftClient.getInstance().player ?: return
        val packet = CreativeInventoryActionC2SPacket(slot, itemStack)
        player.networkHandler.sendPacket(packet)
    }
}