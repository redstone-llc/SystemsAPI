package llc.redstone.systemsapi.api

interface HouseSettings {
    suspend fun getHouseName(): String
    suspend fun setHouseName(newName: String)

    suspend fun getHouseTags(): Set<HouseTag>
    suspend fun setHouseTags(newTags: Set<HouseTag>)

    suspend fun getHouseLanguages(): Set<HouseLanguage>
    suspend fun setHouseLanguages(newLanguages: Set<HouseLanguage>)

    suspend fun getParkourAnnounce(): ParkourAnnounce
    suspend fun setParkourAnnounce(newPark: ParkourAnnounce)

    suspend fun getMaxPlayers(): MaxPlayers
    suspend fun setMaxPlayers(newMaxPlayers: MaxPlayers)

    suspend fun getDaylightCycle(): Boolean
    suspend fun setDaylightCycle(newDaylightCycle: Boolean)


    enum class HouseTag(val displayName: String) {
        ROLEPLAY("Roleplay (RP)"),
        GENERAL("General (GEN)"),
        PARKOUR("Parkour (P)"),
        MAZE("Maze (MAZE)"),
        CHAT("Chat (C)"),
        GUILD_HANGOUT("Guild Hangout (GH)"),
        BUILD("Build (B)"),
        MINIGAME("Minigame (M)")
    }
    enum class HouseLanguage(val displayName: String) {
        ENGLISH("English"),
        GERMAN("German"),
        FRENCH("French"),
        DUTCH("Dutch"),
        SPANISH("Spanish"),
        ITALIAN("Italian"),
        CHINESE("Chinese"),
        NORWEGIAN("Norwegian"),
        PORTUGUESE("Portuguese"),
        RUSSIAN("Russian"),
        SWEDISH("Swedish"),
        TURKISH("Turkish"),
        KOREAN("Korean"),
        JAPANESE("Japanese"),
        POLISH("Polish"),
        DANISH("Danish")
    }
    enum class ParkourAnnounce(val displayName: String) {
        ALL("ALL"),
        OFF("OFF"),
        RECORD("RECORD")
    }
    enum class MaxPlayers(val displayName: String) {
        FIFTEEN("15"),
        THIRTY("30"),
        NINETY("90"),
        ONE_TWENTY_FIVE("125"),
        ONE_FIFTY("150"),
        DYNAMIC("Dynamic")
    }
}