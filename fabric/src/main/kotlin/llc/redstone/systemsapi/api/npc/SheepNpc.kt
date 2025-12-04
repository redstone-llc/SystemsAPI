package llc.redstone.systemsapi.api.npc

interface SheepNpc: AgedNpc {
    suspend fun getWoolColor(): WoolColor
    suspend fun setWoolColor(newWoolColor: WoolColor)

    suspend fun getSheared(): Boolean
    suspend fun setSheared(newSheared: Boolean)

    enum class WoolColor {
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