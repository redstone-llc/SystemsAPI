package llc.redstone.systemsapi.util

import llc.redstone.systemsapi.SystemsAPI.MC
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket
import net.minecraft.world.GameMode

object ItemStackUtils {
    fun ItemStack.giveItem(slot: Int) {
        val gameMode = MC.player
            ?.gameMode ?: throw IllegalStateException("Could not determine player's game mode")
        if (gameMode != GameMode.CREATIVE) CommandUtils.runCommand("/gmc")

        val pkt = CreativeInventoryActionC2SPacket(
            slot,
            this
        )
        MC.networkHandler?.sendPacket(pkt)
            ?: throw IllegalStateException("Something went wrong while creating item ${item.name}")

        when (gameMode) {
            GameMode.SURVIVAL -> CommandUtils.runCommand("/gms")
            GameMode.ADVENTURE -> CommandUtils.runCommand("/gma")
            else -> {}
        }
    }

    fun ItemStack.getProperty(key: String): String? {
        return this.get(DataComponentTypes.LORE)
            ?.lines
            ?.firstNotNullOfOrNull { line ->
                line.string.substringAfter("$key: ").takeIf { it != line.string }
            }
    }

    fun ItemStack.getLoreLine(line: Int, color: Boolean): String {
        return this.loreLines(color)
            .getOrNull(line)
            ?: throw IllegalStateException("Lore index out of bounds")
    }

    fun ItemStack.getLoreLineMatches(color: Boolean, filter: (String) -> Boolean = { true }): String {
        return this.loreLines(color)
            .firstOrNull { line -> filter(line) }
            ?: throw IllegalStateException("No lore lines for item $this passed filter")
    }

    fun ItemStack.loreLines(color: Boolean): List<String> {
        val loreLines = this.get(DataComponentTypes.LORE)?.lines
            ?: return emptyList()
        return loreLines.map { TextUtils.convertTextToString(it, color)!! }
    }
}