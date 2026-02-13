package llc.redstone.systemsapi.api

/**
 * Represents a team within Housing.
 *
 * Provides accessors and mutators for the team's metadata (name, tag, color),
 * friendly-fire setting and lifecycle operations.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Team {
    /**
     * The unique name of the team.
     */
    var name: String

    /**
     * Returns the current team name.
     *
     * @return the team's name
     */
    suspend fun getName(): String = name

    /**
     * Sets a new name for the team.
     *
     * @param newName the new name to set (1..16 characters)
     * @throws IllegalArgumentException if newName length is out of acceptable range
     * @return the updated [Team] instance
     */
    suspend fun setName(newName: String): Team

    /**
     * Returns the team's chat/tag string, omitting the brackets.
     *
     * @return the tag displayed for the team
     */
    suspend fun getTag(): String

    /**
     * Sets the team's chat/tag string.
     *
     * @param newTag the tag to set for the team, omitting the brackets (1..10 characters)
     * @throws IllegalArgumentException if newTag length is not an acceptable length
     * @return the [Team] instance
     */
    suspend fun setTag(newTag: String): Team

    /**
     * Returns the team's display color.
     *
     * @return the team's [TeamColor]
     */
    suspend fun getColor(): TeamColor

    /**
     * Sets the team's display color.
     *
     * @param newColor the new [TeamColor] to apply
     * @return the [Team] instance
     */
    suspend fun setColor(newColor: TeamColor): Team

    /**
     * Returns whether friendly fire is enabled for this team.
     *
     * @return true if friendly fire is enabled, false otherwise
     */
    suspend fun getFriendlyFire(): Boolean

    /**
     * Sets the friendly fire flag for this team.
     *
     * @param newFriendlyFire true to enable friendly fire, false to disable
     * @return the [Team] instance
     */
    suspend fun setFriendlyFire(newFriendlyFire: Boolean): Team

    /**
     * Deletes this team.
     */
    suspend fun delete()

    /**
     * Available display colors for a team.
     *
     * @property displayName name of color shown in menus
     */
    enum class TeamColor(val displayName: String) {
        /** Dark blue display color. */
        DARK_BLUE("Dark Blue"),
        /** Dark green display color. */
        DARK_GREEN("Dark Green"),
        /** Dark aqua display color. */
        DARK_AQUA("Dark Aqua"),
        /** Dark red display color. */
        DARK_RED("Dark Red"),
        /** Dark purple display color. */
        DARK_PURPLE("Dark Purple"),
        /** Gold display color. */
        GOLD("Gold"),
        /** Gray display color. */
        GRAY("Gray"),
        /** Dark gray display color. */
        DARK_GRAY("Dark Gray"),
        /** Blue display color. */
        BLUE("Blue"),
        /** Green display color. */
        GREEN("Green"),
        /** Aqua display color. */
        AQUA("Aqua"),
        /** Red display color. */
        RED("Red"),
        /** Light purple display color. */
        LIGHT_PURPLE("Light Purple"),
        /** Yellow display color. */
        YELLOW("Yellow"),
        /** White display color. */
        WHITE("White")
    }
}