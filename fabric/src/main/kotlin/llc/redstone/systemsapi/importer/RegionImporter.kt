package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Region
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot

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

        MenuItems.ENTRY_ACTIONS.click()
        MenuUtils.onOpen("Edit Actions")
    }

    private suspend fun openExitActionsEditMenu() {
        openRegionEditMenu()

        MenuItems.EXIT_ACTIONS.click()
        MenuUtils.onOpen("Edit Actions")
    }

    override suspend fun setName(newName: String) {
        if (newName.length !in 1..50) throw IllegalArgumentException("Region name length must be in range 1..50")
        openRegionEditMenu()

        MenuItems.RENAME_REGION.click()
        InputUtils.textInput(newName, 100L)

        name = newName
    }

    override suspend fun teleportToRegion() {
        openRegionEditMenu()
        MenuItems.TELEPORT_TO_REGION.click()
    }

    override suspend fun moveRegion() {
        openRegionEditMenu()
        MenuItems.MOVE_REGION.click()
    }

    override suspend fun getPvpSettings(): MutableMap<Region.PvpSettings, Boolean> {
        openRegionEditMenu()
        MenuItems.PVP_SETTINGS.click()

        val map = mutableMapOf<Region.PvpSettings, Boolean>()
        val keys: Array<Region.PvpSettings> = Region.PvpSettings.entries.toTypedArray()

        for (pvpSetting in keys) {
            val setting = MenuUtils.findSlots(pvpSetting.label).first()
            when (setting.stack.item) {
                Items.LIME_DYE -> map.putIfAbsent(pvpSetting, true)
                Items.LIGHT_GRAY_DYE -> map.putIfAbsent(pvpSetting, false)
            }
        }

        return map
    }

    override suspend fun setPvpSettings(newPvpSettings: MutableMap<Region.PvpSettings, Boolean>) {
        openRegionEditMenu()
        MenuItems.PVP_SETTINGS.click()

        val keys: Array<Region.PvpSettings> = Region.PvpSettings.entries.toTypedArray()
        for (pvpSetting in keys) {
            val settingItem = MenuUtils.findSlots(pvpSetting.label).first()
            val settingValue = when (settingItem.stack.item) {
                Items.LIME_DYE -> true
                Items.LIGHT_GRAY_DYE -> false
                else -> null
            }
            // Unset settings which aren't provided
            if (!newPvpSettings.contains(pvpSetting)) {
                if (settingValue != null) MenuUtils.clickItems(pvpSetting.label, button = 1)
                continue
            }
            // Set values which are provided
            val newSetting = newPvpSettings[pvpSetting]
            if (newSetting == settingValue) continue
            MenuUtils.clickItems(pvpSetting.label)
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

    suspend fun exists(): Boolean = getTabCompletions("region edit").contains(name)
    fun create() = CommandUtils.runCommand("region create $name")
    override suspend fun delete() {
        CommandUtils.runCommand("region delete $name")
    }

    private enum class MenuItems(
        val label: String,
        val type: Item? = null
    ) {
        RENAME_REGION("Rename Region", Items.NAME_TAG),
        TELEPORT_TO_REGION("Teleport to Region", Items.ENDER_PEARL),
        MOVE_REGION("Move Region", Items.STICK),
        PVP_SETTINGS("PvP Settings", Items.IRON_SWORD),
        ENTRY_ACTIONS("Entry Actions", Items.PAPER),
        EXIT_ACTIONS("Exit Actions", Items.PAPER);

        suspend fun click() = if (type != null) MenuUtils.clickItems(label, type) else MenuUtils.clickItems(label)
        fun find(): Slot = if (type != null) MenuUtils.findSlots(label, type).first() else MenuUtils.findSlots(label).first()
    }

}