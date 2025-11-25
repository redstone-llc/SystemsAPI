package llc.redstone.systemapi.api.npc

interface PigNpc: AgedNpc {
    suspend fun getSaddled(): Boolean
    suspend fun setSaddled(newSaddled: Boolean)
}