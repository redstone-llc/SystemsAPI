package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer

/**
 * Represents a region within housing.
 *
 * A Region provides metadata (name), PvP-related configuration and entry/exit action containers.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Region {
    /**
     * The unique name of the region.
     */
    var name: String

    /**
     * Returns the current region name.
     *
     * @return the region name
     */
    suspend fun getName(): String = name

    /**
     * Sets a new name for the region.
     *
     * @param newName the new name to set (1..50 characters)
     * @throws IllegalArgumentException if newName length is out of acceptable range
     * @return the updated [Region] instance
     */
    suspend fun setName(newName: String): Region

    /**
     * Moves the region bounds. The caller must have a region selected for this to succeed.
     *
     * @return the [Region] instance
     */
    suspend fun moveRegion(): Region

    /**
     * Returns the PvP-related settings for this region.
     *
     * The map contains PvP setting keys mapped to optional boolean values. A null value
     * indicates the setting is unset and inherits the global default.
     *
     * @return mutable map of PvP settings to their enabled state or null
     */
    suspend fun getPvpSettings(): MutableMap<PvpSettings, Boolean?>

    /**
     * Updates the PvP settings for this region.
     *
     * @param newPvpSettings map of PvP settings to apply (key may be absent to ignore, and value may be null to make the setting inherit the global default)
     * @return the updated [Region] instance
     */
    suspend fun setPvpSettings(newPvpSettings: MutableMap<PvpSettings, Boolean?>): Region

    /**
     * Returns the action container executed when a player enters the region.
     *
     * @return the entry [ActionContainer]
     */
    suspend fun getEntryActionContainer(): ActionContainer

    /**
     * Returns the action container executed when a player exits the region.
     *
     * @return the exit [ActionContainer]
     */
    suspend fun getExitActionContainer(): ActionContainer

    /**
     * Deletes this region.
     */
    suspend fun delete()

    /**
     * PvP and damage-related toggleable settings for a region.
     *
     * @property displayName name of setting shown in menus
     */
    enum class PvpSettings(val displayName: String) {
        /** PvP and general damage toggles. */
        PVP("PvP/Damage"),
        /** Double jump behavior. */
        DOUBLE_JUMP("Double Jump"),
        /** Fire-related damage. */
        FIRE_DAMAGE("Fire Damage"),
        /** Fall damage. */
        FALL_DAMAGE("Fall Damage"),
        /** Poison and wither damage. */
        POISON_WITHER_DAMAGE("Poison/Wither Damage"),
        /** Suffocation damage. */
        SUFFOCATION("Suffocation"),
        /** Hunger changes. */
        HUNGER("Hunger"),
        /** Natural health regeneration. */
        NATURAL_REGENERATION("Natural Regeneration"),
        /** Show kill/death messages. */
        DEATH_MESSAGES("Kill/Death Messages"),
        /** Instant respawn behavior. */
        INSTANT_RESPAWN("Instant Respawn"),
        /** Keep inventory on death. */
        KEEP_INVENTORY("Keep Inventory")
    }
}