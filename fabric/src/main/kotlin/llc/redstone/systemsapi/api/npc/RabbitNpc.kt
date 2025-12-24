package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.api.Npc

interface RabbitNpc: Npc, Npc.NpcCapabilities.Ageable {
    suspend fun getRabbitType(): RabbitType
    suspend fun setRabbitType(newRabbitType: RabbitType)

    enum class RabbitType {
        BLACK,
        GOLD,
        BROWN,
        BLACK_AND_WHITE,
        SALT_AND_PEPPER,
        WHITE
    }
}