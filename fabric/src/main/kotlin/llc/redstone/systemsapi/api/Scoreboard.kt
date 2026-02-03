package llc.redstone.systemsapi.api

interface Scoreboard {
    suspend fun getLines(): List<LineType>
    suspend fun setLines(newLines: List<LineType>): Scoreboard


    sealed class VariableType(val displayName: String) {
        object Player : VariableType("Player")
        object Global : VariableType("Global")
        data class Team(val team: String) : VariableType("Team")
    }

    sealed class LineType(val displayName: String, val lines: Int) {
        object BlankLine : LineType("Blank Line", 1)
        data class CustomLine(val text: String) : LineType("Custom Line", 1)
        object HouseName : LineType("House Name", 2)
        object GuestsCount : LineType("Guests Count", 1)
        object CookieCount : LineType("Cookie Count", 1)
        object PlayerGroup : LineType("Player Group", 1)
        object PvpEnabledState : LineType("PvP Enabled State", 1)
        object Gamemode : LineType("Gamemode", 1)
        object HouseMode : LineType("House Mode", 1)
        object ParkourTime : LineType("Parkour Time", 1)
        data class VariableValue(val scope: Scoreboard.VariableType, val key: String) : LineType("Variable Value", 1)

        companion object {
            val typesByDisplayName: Map<String, LineType> by lazy {
                LineType::class.sealedSubclasses
                    .mapNotNull { it.objectInstance }
                    .associateBy { it.displayName }
            }
        }
    }
}