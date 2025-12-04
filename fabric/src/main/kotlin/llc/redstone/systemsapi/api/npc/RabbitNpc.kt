package llc.redstone.systemsapi.api.npc

interface RabbitNpc: AgedNpc {
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