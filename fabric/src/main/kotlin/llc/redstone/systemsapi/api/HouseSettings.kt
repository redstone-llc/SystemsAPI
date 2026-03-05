package llc.redstone.systemsapi.api

import kotlin.time.Duration

/**
 * Represents miscellaneous house settings in Housing.
 *
 * Provides suspendable accessors and mutators for various house-level configuration values such as
 * daylight cycle, player limits, tags, languages, parkour announcement mode, and gameplay-related
 * toggles.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface HouseSettings {
    /**
     * Returns whether the daylight cycle is enabled for this house.
     *
     * @return true when daylight cycle is enabled, false otherwise
     */
    suspend fun getDaylightCycle(): Boolean

    /**
     * Sets whether the daylight cycle should be enabled.
     *
     * @param newDaylightCycle true to enable the daylight cycle, false to disable
     * @return the [HouseSettings] object
     */
    suspend fun setDaylightCycle(newDaylightCycle: Boolean): HouseSettings

    /**
     * Returns the configured maximum players setting for the house.
     *
     * @return the current [MaxPlayers] value
     */
    suspend fun getMaxPlayers(): MaxPlayers

    /**
     * Sets the maximum players configuration for the house.
     *
     * @param newMaxPlayers the new maximum players setting
     * @return the [HouseSettings] object
     */
    suspend fun setMaxPlayers(newMaxPlayers: MaxPlayers): HouseSettings

    /**
     * Returns the human-facing name of the house.
     *
     * @return the house name
     */
    suspend fun getHouseName(): String

    /**
     * Sets the human-facing name of the house.
     *
     * @param newName new name to assign to the house
     * @return the [HouseSettings] instance
     */
    suspend fun setHouseName(newName: String): HouseSettings

    /**
     * Returns the set of tags associated with the house.
     *
     * @return set of [HouseTag] currently applied
     */
    suspend fun getHouseTags(): Set<HouseTag>

    /**
     * Replaces the house tags with the provided set.
     *
     * @param newTags new set of tags to apply
     * @return the [HouseSettings] instance
     */
    suspend fun setHouseTags(newTags: Set<HouseTag>): HouseSettings

    /**
     * Returns the set of specified languages for the house.
     *
     * @return set of [HouseLanguage]
     */
    suspend fun getHouseLanguages(): Set<HouseLanguage>

    /**
     * Sets the specified languages for the house.
     *
     * @param newLanguages new set of languages to apply
     * @return the [HouseSettings] object
     */
    suspend fun setHouseLanguages(newLanguages: Set<HouseLanguage>): HouseSettings

    /**
     * Returns the parkour announce behavior for the house.
     *
     * @return the current [ParkourAnnounce] value
     */
    suspend fun getParkourAnnounce(): ParkourAnnounce

    /**
     * Sets the parkour announce behavior.
     *
     * @param newParkourAnnounce the new parkour announce mode
     * @return the [HouseSettings] object
     */
    suspend fun setParkourAnnounce(newParkourAnnounce: ParkourAnnounce): HouseSettings

    /**
     * Returns the default storage duration for player states.
     *
     * @return a [Duration] representing the configured player state duration
     */
    suspend fun getPlayerStateDuration(): Duration

    /**
     * Sets the default storage duration for player states.
     *
     * @param newDuration duration to apply
     * @return the [HouseSettings] object
     */
    suspend fun setPlayerStateDuration(newDuration: Duration): HouseSettings

    /**
     * Returns a map of player state types to their enabled/disabled state.
     *
     * @return mutable map where keys are [PlayerStateType] and values indicate enabled state
     */
    suspend fun getPlayerStateTypes(): MutableMap<PlayerStateType, Boolean>

    /**
     * Enables/disables the storage of specific player state types.
     *
     * @param newStates map of player state types to configure.
     * @return the [HouseSettings] object
     */
    suspend fun setPlayerStateTypes(newStates: MutableMap<PlayerStateType, Boolean>): HouseSettings

    /**
     * Clears all player states.
     *
     * !!! warning
     *     This process is irreversible!
     *
     * @return the [HouseSettings] object
     */
    suspend fun clearPlayerStates(): HouseSettings

    /**
     * Returns the default variable duration used when no override exists.
     *
     * @return default [Duration] for variables
     */
    suspend fun getDefaultVariableDuration(): Duration

    /**
     * Sets the default variable duration.
     *
     * @param newDuration duration to set as default
     * @return the [HouseSettings] object
     */
    suspend fun setDefaultVariableDuration(newDuration: Duration): HouseSettings

    /**
     * Returns an override duration for a specific variable, if present.
     *
     * @param variable variable name to look up
     * @return override [Duration] or null when no override exists
     */
    suspend fun getVariableDurationOverride(variable: String): Duration?

    /**
     * Sets an override duration for a specific variable.
     *
     * @param variable variable name to set the override for
     * @param newDuration duration to use as the override
     * @return the updated HouseSettings instance
     */
    suspend fun setVariableDurationOverride(variable: String, newDuration: Duration): HouseSettings

    /**
     * Removes a previously set variable duration override.
     *
     * @param variable variable name whose override should be removed
     * @return the [HouseSettings] object
     */
    suspend fun removeVariableDurationOverride(variable: String): HouseSettings

    /**
     * Returns the map of global PvP-related settings and whether each is enabled.
     *
     * @return mutable map with keys of [PvpSettings] and boolean enabled flags
     */
    suspend fun getPvpSettings(): MutableMap<PvpSettings, Boolean>

    /**
     * Replaces PvP-related settings with the provided map.
     *
     * @param newPvpSettings map of PvP settings to enabled/disabled flags
     * @return the [HouseSettings] object
     */
    suspend fun setPvpSettings(newPvpSettings: MutableMap<PvpSettings, Boolean>): HouseSettings

    /**
     * Returns the map of global fishing-related settings and whether each is enabled.
     *
     * @return mutable map with keys of [FishingSettings] and boolean enabled flags
     */
    suspend fun getFishingSettings(): MutableMap<FishingSettings, Boolean>

    /**
     * Replaces fishing-related settings with the provided map.
     *
     * @param newFishingSettings map of fishing settings to enabled/disabled flags
     * @return the updated HouseSettings instance
     */
    suspend fun setFishingSettings(newFishingSettings: MutableMap<FishingSettings, Boolean>): HouseSettings


    /**
     * House tags categorizing the house by activity/style.
     *
     * @property displayName name of tag in menus
     */
    enum class HouseTag(val displayName: String) {
        /** Roleplay-focused houses. */
        ROLEPLAY("Roleplay (RP)"),
        /** General discussion / social houses. */
        GENERAL("General (GEN)"),
        /** Parkour-focused houses. */
        PARKOUR("Parkour (P)"),
        /** Maze-style houses. */
        MAZE("Maze (MAZE)"),
        /** Chat-centric houses. */
        CHAT("Chat (C)"),
        /** Guild hangout houses. */
        GUILD_HANGOUT("Guild Hangout (GH)"),
        /** Build-focused houses. */
        BUILD("Build (B)"),
        /** Minigame houses. */
        MINIGAME("Minigame (M)")
    }

    /**
     * Specified house languages.
     *
     * @property displayName (beginning of) name of language in menus
     */
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

    /**
     * Options for how parkour completion announcements are handled.
     *
     * @property displayName name of selection in menus
     */
    enum class ParkourAnnounce(val displayName: String) {
        /** Announce to all players. */
        ALL("ALL"),
        /** Disable announcements. */
        OFF("OFF"),
        /** Announce only when a record is set. */
        RECORD("RECORD")
    }

    /**
     * Options for maximum player counts.
     *
     * @property displayName name of selection in menus
     */
    enum class MaxPlayers(val displayName: String) {
        FIFTEEN("15"),
        THIRTY("30"),
        NINETY("90"),
        ONE_TWENTY_FIVE("125"),
        ONE_FIFTY("150"),
        DYNAMIC("Dynamic")
    }

    /**
     * Types of player-state data that may be saved/restored.
     *
     * @property displayName name of state type in menus
     */
    enum class PlayerStateType(val displayName: String) {
        LOCATION("Location"),
        PARKOUR("Parkour"),
        HEALTH("Health"),
        INVENTORIES("Inventories"),
        POTIONS_METADATA("Potions and Metadata"),
        TEAM("Team")
    }

    /**
     * PvP and damage-related settings.
     *
     * @property displayName name of PvP setting in menus
     */
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

    /**
     * Fishing-related settings.
     *
     * @property displayName name of fishing setting in menus
     */
    enum class FishingSettings(val displayName: String) {
        ALLOW_USING_FISHING_RODS("Allow Using Fishing Rods"),
        LAVA_FISHING("Lava Fishing"),
        SHOW_CATCH_TIMER("Show Catch Timer"),
        PLAY_CAUGHT_SOUND("Play Caught Sound")
    }
}