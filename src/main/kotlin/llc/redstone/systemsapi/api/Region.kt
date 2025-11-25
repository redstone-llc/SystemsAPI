package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.importer.ActionContainer
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot

interface Region {
    var name: String

    // Region must be selected to run this.
    suspend fun createIfNotExists(): Boolean

    suspend fun getName(): String = name
    suspend fun setName(newName: String)

    suspend fun teleportToRegion()

    // Region must be selected to run this.
    suspend fun moveRegion()

    suspend fun getPvpSettings(): MutableMap<PvpSettings, Boolean>
    suspend fun setPvpSettings(newPvpSettings: MutableMap<PvpSettings, Boolean>)

    suspend fun getEntryActionContainer(): ActionContainer
    suspend fun getExitActionContainer(): ActionContainer

    suspend fun delete()

    enum class PvpSettings(val item: MenuSlot) {
        PVP(MenuSlot(null, "PvP/Damage")),
        DOUBLE_JUMP(MenuSlot(null, "Double Jump")),
        FIRE_DAMAGE(MenuSlot(null, "Fire Damage")),
        FALL_DAMAGE(MenuSlot(null, "Fall Damage")),
        POISON_WITHER_DAMAGE(MenuSlot(null, "Poison/Wither Damage")),
        SUFFOCATION(MenuSlot(null, "Suffocation")),
        HUNGER(MenuSlot(null, "Hunger")),
        NATURAL_REGENERATION(MenuSlot(null, "Natural Regeneration")),
        DEATH_MESSAGES(MenuSlot(null, "Death Messages")),
        INSTANT_RESPAWN(MenuSlot(null, "Instant Respawn")),
        KEEP_INVENTORY(MenuSlot(null, "Keep Inventory")),
    }
}