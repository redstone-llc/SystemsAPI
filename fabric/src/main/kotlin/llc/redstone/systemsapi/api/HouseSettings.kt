package llc.redstone.systemsapi.api

import kotlin.time.Duration

interface HouseSettings {
    suspend fun getDaylightCycle(): Boolean
    suspend fun setDaylightCycle(newDaylightCycle: Boolean): HouseSettings

    suspend fun getMaxPlayers(): MaxPlayers
    suspend fun setMaxPlayers(newMaxPlayers: MaxPlayers): HouseSettings

    suspend fun getHouseName(): String
    suspend fun setHouseName(newName: String): HouseSettings

    suspend fun getHouseTags(): Set<HouseTag>
    suspend fun setHouseTags(newTags: Set<HouseTag>): HouseSettings

    suspend fun getHouseLanguages(): Set<HouseLanguage>
    suspend fun setHouseLanguages(newLanguages: Set<HouseLanguage>): HouseSettings

    suspend fun getParkourAnnounce(): ParkourAnnounce
    suspend fun setParkourAnnounce(newParkourAnnounce: ParkourAnnounce): HouseSettings

    suspend fun getPlayerStateDuration(): Duration
    suspend fun setPlayerStateDuration(newDuration: Duration): HouseSettings

    suspend fun getPlayerStateTypes(): MutableMap<PlayerStateType, Boolean>
    suspend fun setPlayerStateTypes(newStates: MutableMap<PlayerStateType, Boolean>): HouseSettings

    suspend fun clearPlayerStates(): HouseSettings

    suspend fun getDefaultVariableDuration(): Duration
    suspend fun setDefaultVariableDuration(newDuration: Duration): HouseSettings

    suspend fun getVariableDurationOverride(variable: String): Duration?
    suspend fun setVariableDurationOverride(variable: String, newDuration: Duration): HouseSettings
    suspend fun removeVariableDurationOverride(variable: String): HouseSettings

    suspend fun getPvpSettings(): MutableMap<PvpSettings, Boolean>
    suspend fun setPvpSettings(newPvpSettings: MutableMap<PvpSettings, Boolean>): HouseSettings

    suspend fun getFishingSettings(): MutableMap<FishingSettings, Boolean>
    suspend fun setFishingSettings(newFishingSettings: MutableMap<FishingSettings, Boolean>): HouseSettings


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
    enum class PlayerStateType(val displayName: String) {
        LOCATION("Location"),
        PARKOUR("Parkour"),
        HEALTH("Health"),
        INVENTORIES("Inventories"),
        POTIONS_METADATA("Potions and Metadata"),
        TEAM("Team")
    }
    enum class PvpSettings(val displayName: String) {
        PVP("PvP/Damage"),
        DOUBLE_JUMP("Double Jump"),
        FIRE_DAMAGE("Fire Damage"),
        FALL_DAMAGE("Fall Damage"),
        POISON_WITHER_DAMAGE("Poison/Wither Damage"),
        SUFFOCATION("Suffocation"),
        HUNGER("Hunger"),
        NATURAL_REGENERATION("Natural Regeneration"),
        DEATH_MESSAGES("Kill/Death Messages"),
        INSTANT_RESPAWN("Instant Respawn"),
        KEEP_INVENTORY("Keep Inventory")
    }
    enum class FishingSettings(val displayName: String) {
        ALLOW_USING_FISHING_RODS("Allow Using Fishing Rods"),
        LAVA_FISHING("Lava Fishing"),
        SHOW_CATCH_TIMER("Show Catch Timer"),
        PLAY_CAUGHT_SOUND("Play Caught Sound")
    }
}