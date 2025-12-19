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

    enum class TeamColor(val displayName: String) {
        DARK_BLUE("Dark Blue"),
        DARK_GREEN("Dark Green"),
        DARK_AQUA("Dark Aqua"),
        DARK_RED("Dark Red"),
        DARK_PURPLE("Dark Purple"),
        GOLD("Gold"),
        GRAY("Gray"),
        DARK_GRAY("Dark Gray"),
        BLUE("Blue"),
        GREEN("Green"),
        AQUA("Aqua"),
        RED("Red"),
        LIGHT_PURPLE("Light Purple"),
        YELLOW("Yellow"),
        WHITE("White")
    }
}