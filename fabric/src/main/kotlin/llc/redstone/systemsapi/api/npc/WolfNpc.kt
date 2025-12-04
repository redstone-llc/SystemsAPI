package llc.redstone.systemsapi.api.npc

interface WolfNpc: AgedNpc {
    suspend fun getCollarColor(): CollarColor
    suspend fun setCollarColor(newCollarColor: CollarColor)

    suspend fun getSitting(): Boolean
    suspend fun setSitting(newSitting: Boolean)

    enum class CollarColor {
        WHITE,
        ORANGE,
        MAGENTA,
        LIGHT_BLUE,
        YELLOW,
        LIME,
        PINK,
        GRAY,
        SILVER,
        CYAN,
        PURPLE,
        BLUE,
        BROWN,
        GREEN,
        RED,
        BLACK
    }
}