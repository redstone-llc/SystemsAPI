package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.api.Npc

interface SkeletonNpc: Npc, Npc.NpcCapabilities.Equippable {
    suspend fun getWithered(): Boolean
    suspend fun setWithered(newWithered: Boolean)
}