package llc.redstone.systemapi.api

import net.minecraft.item.Item

interface Scoreboard {
    suspend fun getLines(): List<String>
    suspend fun setLines(newLines: List<String>)
}