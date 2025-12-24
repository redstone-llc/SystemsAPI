package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.api.Npc

interface ArmorStandNpc: Npc, Npc.NpcCapabilities.Equippable {
    suspend fun hasArms(): Boolean
    suspend fun setArms(hasArms: Boolean)

    suspend fun hasBasePlate(): Boolean
    suspend fun setBasePlate(hasBasePlate: Boolean)

    suspend fun isVisible(): Boolean
    suspend fun setVisible(isVisible: Boolean)

    suspend fun isSmall(): Boolean
    suspend fun setSmall(isSmall: Boolean)
}