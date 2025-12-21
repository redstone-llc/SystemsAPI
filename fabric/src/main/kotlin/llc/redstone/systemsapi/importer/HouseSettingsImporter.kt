package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.api.HouseSettings
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot

object HouseSettingsImporter : HouseSettings {

    private fun isSettingsMenuOpen(): Boolean = runCatching { MenuUtils.currentMenu().title.string == "House Settings" }.getOrDefault(false)

    private suspend fun openSettingsMenu() {
        if (isSettingsMenuOpen()) return

        CommandUtils.runCommand("menu")
        MenuUtils.onOpen("Housing Menu")
        MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(Items.COMPARATOR, "House Settings"))
        MenuUtils.onOpen("House Settings")
    }

    override suspend fun getHouseName(): String {
        TODO("Not yet implemented")
    }

    override suspend fun setHouseName(newName: String) {
        if (newName.length !in 3..32) throw IllegalArgumentException("Name length must be in range 3..32")
        CommandUtils.runCommand("house name $newName")
    }

    override suspend fun getHouseTags(): Set<HouseSettings.HouseTag> {
        openSettingsMenu()

        MenuItems.HOUSE_TAGS.click()
        MenuUtils.onOpen("House Tags")

        val tags = MenuUtils.currentMenu().screenHandler.inventory.asSequence()
            .filter { it.item == Items.LIME_DYE }
            .map { it.name.string }
            .map { display ->
                HouseSettings.HouseTag.entries.firstOrNull { it.displayName == display }
                    ?: throw IllegalStateException("Unknown house tag '$display'")
            }
            .toSet()

        MenuItems.MAIN_MENU.click()
        MenuUtils.onOpen("House Settings")

        return tags
    }

    override suspend fun setHouseTags(newTags: Set<HouseSettings.HouseTag>) {
        openSettingsMenu()

        MenuItems.HOUSE_TAGS.click()
        MenuUtils.onOpen("House Tags")

        // Deselect first
        val toDeselect = MenuUtils.currentMenu().screenHandler.slots.asSequence()
            .filter { it.stack.item == Items.LIME_DYE }
            .filter { slot -> !newTags.map { it.displayName }.contains(slot.stack.name.string) }
        for (slot in toDeselect) {
            MenuUtils.packetClick(slot.id)
            delay(150)
        }

        // Select
        val toSelect = MenuUtils.currentMenu().screenHandler.slots.asSequence()
            .filter { it.stack.item == Items.GRAY_DYE }
            .filter { slot -> newTags.map { it.displayName }.contains(slot.stack.name.string) }
        for (slot in toSelect) {
            MenuUtils.packetClick(slot.id)
            delay(150)
        }
    }

    override suspend fun getHouseLanguages(): Set<HouseSettings.HouseLanguage> {
        openSettingsMenu()

        MenuItems.HOUSE_LANGUAGE.click()
        MenuUtils.onOpen("House Language")

        val languages = MenuUtils.currentMenu().screenHandler.inventory.asSequence()
            .filter { it.item == Items.LIME_DYE }
            .map { it.name.string }
            .map { display ->
                HouseSettings.HouseLanguage.entries.firstOrNull { it.displayName == display }
                    ?: throw IllegalStateException("Unknown house tag '$display'")
            }
            .toSet()

        MenuItems.MAIN_MENU.click()
        MenuUtils.onOpen("House Settings")

        return languages
    }

    override suspend fun setHouseLanguages(newLanguages: Set<HouseSettings.HouseLanguage>) {
        openSettingsMenu()

        MenuItems.HOUSE_LANGUAGE.click()
        MenuUtils.onOpen("House Language")

        // Deselect first
        val toDeselect = MenuUtils.currentMenu().screenHandler.slots.asSequence()
            .filter { it.stack.item == Items.LIME_DYE }
            .filter { slot -> !newLanguages.map { it.displayName }.contains(slot.stack.name.string) }
        for (slot in toDeselect) {
            MenuUtils.packetClick(slot.id)
            delay(150)
        }

        // Select
        val toSelect = MenuUtils.currentMenu().screenHandler.slots.asSequence()
            .filter { it.stack.item == Items.GRAY_DYE }
            .filter { slot -> newLanguages.map { it.displayName }.contains(slot.stack.name.string) }
        for (slot in toSelect) {
            MenuUtils.packetClick(slot.id)
            delay(150)
        }
    }

    override suspend fun getParkourAnnounce(): HouseSettings.ParkourAnnounce {

//        val slot = MenuUtils.findSlot()
//        HouseSettings.ParkourAnnounce.entries.firstOrNull { it.displayName == getTitledCycle() }

        TODO("Not yet implemented")
    }

    override suspend fun setParkourAnnounce(newPark: HouseSettings.ParkourAnnounce) {
        TODO("Not yet implemented")
    }

    override suspend fun getMaxPlayers(): HouseSettings.MaxPlayers {
        TODO("Not yet implemented")
    }

    override suspend fun setMaxPlayers(newMaxPlayers: HouseSettings.MaxPlayers) {
        TODO("Not yet implemented")
    }

    override suspend fun getDaylightCycle(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun setDaylightCycle(newDaylightCycle: Boolean) {
        TODO("Not yet implemented")
    }


    private enum class MenuItems(
        val label: String,
        val type: Item? = null
    ) {
        TIME_SELECTOR("Time Selector", Items.CLOCK),
        MAX_PLAYERS("Max Players *", Items.PLAYER_HEAD),
        HOUSE_TAGS("House Tags", Items.NAME_TAG),
        HOUSE_LANGUAGE("House Language", Items.BOOK),
        PARKOUR_ANNOUNCE("Parkour Announce *", Items.LIGHT_WEIGHTED_PRESSURE_PLATE),
        JOIN_LEAVE_MESSAGES("Join/Leave Messages *", Items.PAPER),
        DOOR_FENCE_AUTO_RESET("Door/Fence Auto-Reset *", Items.PAPER),
        PLAYER_DATA("Player Data", Items.FEATHER),
        PVP_DAMAGE_SETTINGS("PvP + Damage Settings", Items.STONE_SWORD),
        FISHING_SETTINGS("Fishing Settings", Items.FISHING_ROD),
        MAIN_MENU("Main Menu", Items.NETHER_STAR);

        fun click() = if (type != null) MenuUtils.clickItems(label, type) else MenuUtils.clickItems(label)
        fun find(): Slot = if (type != null) MenuUtils.findSlots(label, type).first() else MenuUtils.findSlots(label).first()
    }

}