package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.api.Npc

interface CreeperNpc: Npc {
    suspend fun setCharged(isCharged: Boolean)
    suspend fun isCharged(): Boolean
}