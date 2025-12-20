package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.api.HouseSettings
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.item.Items

object HouseSettingsImporter : HouseSettings {

    private fun isSettingsMenuOpen(): Boolean = try {
        MenuUtils.currentMenu().title.string == "House Settings"
    } catch (e: Exception) {
        false
    }

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

        MenuUtils.clickMenuSlot(MenuItems.HOUSE_TAGS)
        MenuUtils.onOpen("House Tags")

        val tags = MenuUtils.currentMenu().screenHandler.inventory.asSequence()
            .filter { it.item == Items.LIME_DYE }
            .map { it.name.string }
            .map { display ->
                HouseSettings.HouseTag.entries.firstOrNull { it.displayName == display }
                    ?: throw IllegalStateException("Unknown house tag '$display'")
            }
            .toSet()

        MenuUtils.clickMenuSlot(MenuItems.MAIN_MENU)
        MenuUtils.onOpen("House Settings")

        return tags
    }

    override suspend fun setHouseTags(newTags: Set<HouseSettings.HouseTag>) {
        openSettingsMenu()

        MenuUtils.clickMenuSlot(MenuItems.HOUSE_TAGS)
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

        MenuUtils.clickMenuSlot(MenuItems.HOUSE_LANGUAGE)
        MenuUtils.onOpen("House Language")

        val languages = MenuUtils.currentMenu().screenHandler.inventory.asSequence()
            .filter { it.item == Items.LIME_DYE }
            .map { it.name.string }
            .map { display ->
                HouseSettings.HouseLanguage.entries.firstOrNull { it.displayName == display }
                    ?: throw IllegalStateException("Unknown house tag '$display'")
            }
            .toSet()

        MenuUtils.clickMenuSlot(MenuItems.MAIN_MENU)
        MenuUtils.onOpen("House Settings")

        return languages
    }

    override suspend fun setHouseLanguages(newLanguages: Set<HouseSettings.HouseLanguage>) {
        openSettingsMenu()

        MenuUtils.clickMenuSlot(MenuItems.HOUSE_LANGUAGE)
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


    private object MenuItems {
        val TIME_SELECTOR = MenuUtils.MenuSlot(Items.CLOCK, "Time Selector")
        val MAX_PLAYERS = MenuUtils.MenuSlot(Items.PLAYER_HEAD, "Max Players *")
        val HOUSE_TAGS = MenuUtils.MenuSlot(Items.NAME_TAG, "House Tags")
        val HOUSE_LANGUAGE = MenuUtils.MenuSlot(Items.BOOK, "House Language")
        val PARKOUR_ANNOUNCE = MenuUtils.MenuSlot(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, "Parkour Announce *")
        val JOIN_LEAVE_MESSAGES = MenuUtils.MenuSlot(Items.PAPER, "Join/Leave Messages *")
        val DOOR_FENCE_AUTO_RESET = MenuUtils.MenuSlot(Items.PAPER, "Door/Fence Auto-Reset *")
        val PLAYER_DATA = MenuUtils.MenuSlot(Items.FEATHER, "Player Data")
        val PVP_DAMAGE_SETTINGS = MenuUtils.MenuSlot(Items.STONE_SWORD, "PvP + Damage Settings")
        val FISHING_SETTINGS = MenuUtils.MenuSlot(Items.FISHING_ROD, "Fishing Settings")
        val MAIN_MENU = MenuUtils.MenuSlot(Items.NETHER_STAR, "Main Menu")
    }
}