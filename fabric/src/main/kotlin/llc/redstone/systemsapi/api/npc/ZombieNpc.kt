package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.api.Npc

interface ZombieNpc: Npc, Npc.NpcCapabilities.Ageable, Npc.NpcCapabilities.Equippable {
    suspend fun isZombieVillager(): Boolean
    suspend fun setZombieVillager(isVillager: Boolean)
}