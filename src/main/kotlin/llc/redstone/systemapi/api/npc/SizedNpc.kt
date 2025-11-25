package llc.redstone.systemapi.api.npc

interface SizedNpc: Npc {
    suspend fun getSize(): Int
    suspend fun setSize(newSize: Int)
}