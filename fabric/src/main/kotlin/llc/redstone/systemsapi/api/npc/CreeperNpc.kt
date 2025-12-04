package llc.redstone.systemsapi.api.npc

interface CreeperNpc: Npc {
    suspend fun setCharged(isCharged: Boolean)
    suspend fun isCharged(): Boolean
}