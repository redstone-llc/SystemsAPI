package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.api.Npc

interface PlayerNpc: Npc, Npc.NpcCapabilities.Equippable {
    suspend fun getSkin(): String
    suspend fun setSkin(newSkin: String)
}