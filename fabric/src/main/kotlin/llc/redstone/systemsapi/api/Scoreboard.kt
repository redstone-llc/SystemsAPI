package llc.redstone.systemsapi.api

/**
 * Representation of a configurable scoreboard for a house/workspace.
 *
 * A Scoreboard exposes the configured lines shown to players and allows reading and
 * replacing the full set of lines.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Scoreboard {
    /**
     * Returns the currently configured scoreboard lines in their display order.
     *
     * @return list of configured [LineType] entries
     */
    suspend fun getLines(): List<LineType>

    /**
     * Replaces the scoreboard lines with the provided list. Be careful, as certain built-in line types
     * take up multiple actual scoreboard lines.
     *
     * @param newLines the new list of [LineType] entries to set (total visual lines must be within 1..10)
     * @throws IllegalArgumentException if newLines amount to more than 10 total lines
     * @return the [Scoreboard] object
     */
    suspend fun setLines(newLines: List<LineType>): Scoreboard


    /**
     * Possible scopes of variables referenced by scoreboard lines.
     *
     * Variable types distinguish between per-player, global or team-scoped variables.
     *
     * @property displayName name of option shown in menus
     */
    sealed class VariableType(val displayName: String) {
        /** A player-scoped variable (value differs per player). */
        object Player : VariableType("Player")

        /** A global variable shared across all players. */
        object Global : VariableType("Global")

        /**
         * A team-scoped variable referenced by team identifier.
         *
         * @property team the team's name
         */
        data class Team(val team: String) : VariableType("Team")
    }

    /**
     * A single line entry on the scoreboard.
     *
     * Each LineType defines how many display lines it occupies and contains any
     * additional data needed to render the line (for example custom text or variable keys).
     *
     * @property displayName name of option shown in menus
     * @property lines number of visual lines occupied by this entry
     */
    sealed class LineType(val displayName: String, val lines: Int) {
        /** An empty/blank scoreboard line. */
        object BlankLine : LineType("Blank Line", 1)

        /**
         * A custom text line defined by the user.
         *
         * @property text string to display on this custom line
         */
        data class CustomLine(val text: String) : LineType("Custom Line", 1)

        /** Displays the house name using two visual lines. */
        object HouseName : LineType("House Name", 2)

        /** Displays the current guests count. */
        object GuestsCount : LineType("Guests Count", 1)

        /** Displays the current cookie count. */
        object CookieCount : LineType("Cookie Count", 1)

        /** Displays the player's group name. */
        object PlayerGroup : LineType("Player Group", 1)

        /** Shows whether PvP is enabled. */
        object PvpEnabledState : LineType("PvP Enabled State", 1)

        /** Shows the player's current gamemode. */
        object Gamemode : LineType("Gamemode", 1)

        /** Shows the current house mode. */
        object HouseMode : LineType("House Mode", 1)

        /** Displays a player's parkour time. */
        object ParkourTime : LineType("Parkour Time", 1)

        /**
         * Shows the value of a variable.
         *
         * @property scope the variable scope [VariableType] (player/global/team)
         * @property key the variable key/name
         */
        data class VariableValue(val scope: VariableType, val key: String) : LineType("Variable Value", 1)

        companion object {
            /**
             * Map of display names to singleton [LineType] instances for object-backed types.
             *
             * Only object instances (non-data subclasses) are present in this map. Custom or
             * parameterized line types (e.g. CustomLine, VariableValue) are not represented here.
             */
            val typesByDisplayName: Map<String, LineType> by lazy {
                LineType::class.sealedSubclasses
                    .mapNotNull { it.objectInstance }
                    .associateBy { it.displayName }
            }
        }
    }
}