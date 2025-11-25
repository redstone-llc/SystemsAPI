package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Region
import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.MenuUtils.Target
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Items

internal class RegionImporter(override var name: String) : Region {
    private fun isRegionEditMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Edit $name Region")
    }

    private suspend fun openRegionEditMenu() {
        if (isRegionEditMenuOpen()) return

        CommandUtils.runCommand("region edit $name")
        MenuUtils.onOpen("Edit: $name")
        delay(50)
    }

    private suspend fun openEntryActionsEditMenu() {
        openRegionEditMenu()

        MenuUtils.clickMenuSlot(MenuItems.ENTRY_ACTIONS)
        MenuUtils.onOpen("Edit Actions")
    }

    private suspend fun openExitActionsEditMenu() {
        openRegionEditMenu()

        MenuUtils.clickMenuSlot(MenuItems.EXIT_ACTIONS)
        MenuUtils.onOpen("Edit Actions")
    }

    override suspend fun createIfNotExists(): Boolean {
        val regions = getTabCompletions("region edit")
        if (regions.contains(name)) return false

        CommandUtils.runCommand("command create $name")
        return true
    }

    override suspend fun setName(newName: String) {
        if (newName.length !in 1..50) error(("[Region $name] Invalid title '$newName'. must be between 1 and 50 characters long."))
        openRegionEditMenu()

        MenuUtils.clickMenuSlot(MenuItems.RENAME_FUNCTION)
        TextUtils.input(newName, 100L)

        name = newName
    }

    override suspend fun teleportToRegion() {
        openRegionEditMenu()
        MenuUtils.clickMenuSlot(MenuItems.TELEPORT_TO_REGION)
    }

    override suspend fun moveRegion() {
        openRegionEditMenu()
        MenuUtils.clickMenuSlot(MenuItems.MOVE_REGION)
    }

    override suspend fun getPvpSettings(): MutableMap<Region.PvpSettings, Boolean> {
        openRegionEditMenu()
        MenuUtils.clickMenuSlot(MenuItems.PVP_SETTINGS)

        val map = mutableMapOf<Region.PvpSettings, Boolean>()
        val keys: Array<Region.PvpSettings> = Region.PvpSettings.entries.toTypedArray()

        for (pvpSetting in keys) {
            val setting = MenuUtils.findSlot(MC.currentScreen as GenericContainerScreen, pvpSetting.item)
                ?: error("Couldn't find slot for $pvpSetting")
            when (setting.stack.item) {
                Items.LIME_DYE -> map.putIfAbsent(pvpSetting, true)
                Items.LIGHT_GRAY_DYE -> map.putIfAbsent(pvpSetting, false)
            }
        }

        return map
    }

    override suspend fun setPvpSettings(newPvpSettings: MutableMap<Region.PvpSettings, Boolean>) {
        openRegionEditMenu()
        MenuUtils.clickMenuSlot(MenuItems.PVP_SETTINGS)

        val keys: Array<Region.PvpSettings> = Region.PvpSettings.entries.toTypedArray()
        for (pvpSetting in keys) {
            val settingItem = MenuUtils.findSlot(MC.currentScreen as GenericContainerScreen, pvpSetting.item)
                ?: error("Couldn't find slot for $pvpSetting")
            val settingValue = when (settingItem.stack.item) {
                Items.LIME_DYE -> true
                Items.LIGHT_GRAY_DYE -> false
                else -> null
            }
            // Unset settings which aren't provided
            if (!newPvpSettings.contains(pvpSetting)) {
                if (settingValue != null) MenuUtils.clickMenuTargets(Target(pvpSetting.item, 1))
                continue
            }
            // Set values which are provided
            val newSetting = newPvpSettings[pvpSetting]
            if (newSetting == settingValue) continue
            MenuUtils.clickMenuSlot(pvpSetting.item)
        }
    }

    override suspend fun getEntryActionContainer(): ActionContainer {
        openEntryActionsEditMenu()
        return ActionContainer("Edit Actions")
    }

    override suspend fun getExitActionContainer(): ActionContainer {
        openExitActionsEditMenu()
        return ActionContainer("Edit Actions")
    }

    override suspend fun delete() {
        CommandUtils.runCommand("region delete $name")
    }

    object MenuItems {
        val RENAME_FUNCTION = MenuSlot(Items.NAME_TAG, "Rename Region")
        val TELEPORT_TO_REGION = MenuSlot(Items.ENDER_PEARL, "Teleport to Region")
        val MOVE_REGION = MenuSlot(Items.STICK, "Move Region")
        val PVP_SETTINGS = MenuSlot(Items.IRON_SWORD, "PvP Settings")
        val ENTRY_ACTIONS = MenuSlot(Items.PAPER, "Entry Actions")
        val EXIT_ACTIONS = MenuSlot(Items.PAPER, "Exit Actions")
    }
}