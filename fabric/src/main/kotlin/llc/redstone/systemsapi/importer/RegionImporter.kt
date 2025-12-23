package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Region
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.InputUtils.getDyeToggle
import llc.redstone.systemsapi.util.InputUtils.setDyeToggle
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.MenuUtils
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
        MenuUtils.onOpen("Edit $name Region")
        delay(50)
    }

    private suspend fun openEntryActionsEditMenu() {
        openRegionEditMenu()

        MenuUtils.clickItems(MenuItems.entryActions)
        MenuUtils.onOpen("Edit Actions")
    }

    private suspend fun openExitActionsEditMenu() {
        openRegionEditMenu()

        MenuUtils.clickItems(MenuItems.exitActions)
        MenuUtils.onOpen("Edit Actions")
    }

    override suspend fun setName(newName: String) {
        if (newName.length !in 1..50) throw IllegalArgumentException("Region name length must be in range 1..50")
        openRegionEditMenu()

        MenuUtils.clickItems(MenuItems.rename)
        InputUtils.textInput(newName, 100L)

        name = newName
    }

    override suspend fun teleportToRegion() {
        openRegionEditMenu()
        MenuUtils.clickItems(MenuItems.teleport)
    }

    override suspend fun moveRegion() {
        openRegionEditMenu()
        MenuUtils.clickItems(MenuItems.move)
    }

    override suspend fun getPvpSettings(): MutableMap<Region.PvpSettings, Boolean?> {
        openRegionEditMenu()
        MenuUtils.clickItems(MenuItems.pvpSettings)
        MenuUtils.onOpen("PVP + Damage Settings - $name")

        val map = mutableMapOf<Region.PvpSettings, Boolean?>()
        val keys: Array<Region.PvpSettings> = Region.PvpSettings.entries.toTypedArray()

        for (pvpSetting in keys) {
            val slot = MenuUtils.findSlots(pvpSetting.displayName).first()
            map.putIfAbsent(pvpSetting, getDyeToggle(slot))
        }

        return map
    }

    override suspend fun setPvpSettings(newPvpSettings: MutableMap<Region.PvpSettings, Boolean?>) {
        openRegionEditMenu()
        MenuUtils.clickItems(MenuItems.pvpSettings)
        MenuUtils.onOpen("PVP + Damage Settings - $name")

        val keys: Array<Region.PvpSettings> = Region.PvpSettings.entries.toTypedArray()
        for (pvpSetting in keys) {
            val slot = MenuUtils.findSlots(pvpSetting.displayName).first()
            val current = getDyeToggle(slot)
            // Unset settings which aren't provided
            if (!newPvpSettings.contains(pvpSetting) || newPvpSettings[pvpSetting] == null) {
                setDyeToggle(slot, null)
                continue
            }
            // Set values which are provided
            val newValue = newPvpSettings.getValue(pvpSetting)
            if (newValue == current) continue
            setDyeToggle(slot, newValue)
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


    private object MenuItems {
        val rename = ItemSelector(
            name = NameExact("Rename Region"),
            item = ItemExact(Items.NAME_TAG)
        )
        val teleport = ItemSelector(
            name = NameExact("Teleport to Region"),
            item = ItemExact(Items.ENDER_PEARL)
        )
        val move = ItemSelector(
            name = NameExact("Move Region"),
            item = ItemExact(Items.STICK)
        )
        val pvpSettings = ItemSelector(
            name = NameExact("PvP Settings"),
            item = ItemExact(Items.IRON_SWORD)
        )
        val entryActions = ItemSelector(
            name = NameExact("Entry Actions"),
            item = ItemExact(Items.PAPER)
        )
        val exitActions = ItemSelector(
            name = NameExact("Exit Actions"),
            item = ItemExact(Items.PAPER)
        )
    }

}