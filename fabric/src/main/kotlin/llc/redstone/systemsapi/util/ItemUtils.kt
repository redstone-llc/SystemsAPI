package llc.redstone.systemsapi.util

import llc.redstone.systemsapi.SystemsAPI.MC
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket
import net.minecraft.server.command.CommandManager
import net.minecraft.world.GameMode
import kotlin.jvm.optionals.getOrNull

object ItemUtils {
    fun ItemStack.giveItem(slot: Int) {
        val player = MC.player ?: error("[giveItem] Could not get the player")
        val gameMode = player.gameMode ?: error("[giveItem] Could not get the player's gamemode")
        if (gameMode != GameMode.CREATIVE) CommandUtils.runCommand("/gmc")

        val pkt = CreativeInventoryActionC2SPacket(
            slot,
            this
        )
        MC.networkHandler?.sendPacket(pkt) ?: error("Something went wrong while creating item $item")

        when (gameMode) {
            GameMode.SURVIVAL -> CommandUtils.runCommand("/gms")
            GameMode.ADVENTURE -> CommandUtils.runCommand("/gma")
            else -> {}
        }
    }

    fun ItemStack.loreLine(line: Int, color: Boolean, filter: (String) -> Boolean = { true }): String {
        val loreLine = get(DataComponentTypes.LORE)?.lines?.getOrNull(line)
            ?: error("Could not find lore line $line for item $this")
        val loreString = TextUtils.convertTextToString(loreLine, color)
        return loreString.takeIf(filter)
            ?: error("Lore line '$loreString' for item $this did not pass filter.")
    }

    fun ItemStack.loreLine(color: Boolean, filter: (String) -> Boolean = { true }): String? {
        val loreLines = get(DataComponentTypes.LORE)?.lines
            ?: error("Could not find lore line in item $this which matched filter.")
        val loreLine = loreLines.firstOrNull { line -> filter(TextUtils.convertTextToString(line, false)) }
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