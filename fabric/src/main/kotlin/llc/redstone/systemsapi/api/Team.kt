package llc.redstone.systemsapi.api

interface Team {
    var name: String

    suspend fun getName(): String = name
    suspend fun setName(newName: String)

    suspend fun getTag(): String
    suspend fun setTag(newTag: String)

    suspend fun getColor(): TeamColor
    suspend fun setColor(newColor: TeamColor)

    suspend fun getFriendlyFire(): Boolean
    suspend fun setFriendlyFire(newFriendlyFire: Boolean)

    suspend fun delete()

    enum class TeamColor {
        DARK_BLUE,
        DARK_GREEN,
        DARK_AQUA,
        DARK_RED,
        DARK_PURPLE,
        GOLD,
        GRAY,
        DARK_GRAY,
        BLUE,
        GREEN,
        AQUA,
        RED,
        LIGHT_PURPLE,
        YELLOW,
    }
}