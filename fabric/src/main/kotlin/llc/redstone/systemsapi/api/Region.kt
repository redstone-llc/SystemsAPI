package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer

interface Region {
    var name: String

    suspend fun getName(): String = name
    suspend fun setName(newName: String): Region

    // Region must be selected to run this.
    suspend fun moveRegion(): Region

    suspend fun getPvpSettings(): MutableMap<PvpSettings, Boolean?>
    suspend fun setPvpSettings(newPvpSettings: MutableMap<PvpSettings, Boolean?>): Region

    suspend fun getEntryActionContainer(): ActionContainer
    suspend fun getExitActionContainer(): ActionContainer

    suspend fun delete()

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
}