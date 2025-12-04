package llc.redstone.systemsapi.api.npc

interface AgedNpc: Npc {
    suspend fun getAge(): Age
    suspend fun setAge(newAge: Age)

    enum class Age {
        ADULT,
        BABY
    }
}