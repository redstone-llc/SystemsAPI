package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.api.Npc

interface VillagerNpc: Npc, Npc.NpcCapabilities.Ageable {
    suspend fun getProfession(): VillagerProfession
    suspend fun setProfession(newProfession: VillagerProfession)

    enum class VillagerProfession {
        FARMER,
        LIBRARIAN,
        PRIEST,
        BLACKSMITH,
        BUTCHER,
    }
}