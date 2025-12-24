package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.api.Npc

interface PigNpc: Npc, Npc.NpcCapabilities.Ageable {
    suspend fun getSaddled(): Boolean
    suspend fun setSaddled(newSaddled: Boolean)
}