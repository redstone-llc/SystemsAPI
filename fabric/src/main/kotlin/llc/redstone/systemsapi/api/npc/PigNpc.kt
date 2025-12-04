package llc.redstone.systemsapi.api.npc

interface PigNpc: AgedNpc {
    suspend fun getSaddled(): Boolean
    suspend fun setSaddled(newSaddled: Boolean)
}