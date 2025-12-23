package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.api.HouseSettings
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.InputUtils.getDyeToggle
import llc.redstone.systemsapi.util.InputUtils.getInlineKeyedLoreCycle
import llc.redstone.systemsapi.util.InputUtils.getKeyedTitleCycle
import llc.redstone.systemsapi.util.InputUtils.getLoreCycle
import llc.redstone.systemsapi.util.InputUtils.setDyeToggle
import llc.redstone.systemsapi.util.InputUtils.setKeyedTitleCycle
import llc.redstone.systemsapi.util.InputUtils.setLoreCycle
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.ItemUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.ItemUtils.ItemSelector
import llc.redstone.systemsapi.util.ItemUtils.NameMatch.NameContains
import llc.redstone.systemsapi.util.ItemUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.item.Items
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

object HouseSettingsImporter : HouseSettings {
    private suspend fun openSettingsMenu() {
        when (getCurrentMenu()) {
            Menu.SETTINGS -> return
            Menu.TIME_SELECTOR, Menu.HOUSE_TAGS, Menu.HOUSE_LANGUAGE -> {
                MenuUtils.clickItems(MenuItems.mainMenu)
            }
            Menu.PLAYER_DATA, Menu.PVP_SETTINGS, Menu.FISHING_SETTINGS -> {
                MenuUtils.clickItems(MenuItems.back)
            }
            else -> {
                CommandUtils.runCommand("menu")
                MenuUtils.onOpen(Menu.MAIN.title)
                MenuUtils.clickItems(MenuItems.houseSettings)
            }
        }
        MenuUtils.onOpen(Menu.SETTINGS.title)
    }

    private suspend fun openPlayerDataMenu() {
        when (val currentMenu = getCurrentMenu()) {
            Menu.PLAYER_DATA -> return
            Menu.PLAYER_STATES, Menu.PLAYER_VARIABLE_DURATIONS, -> {
                MenuUtils.clickItems(MenuItems.back)
            }
            else -> {
                if (currentMenu != Menu.SETTINGS) openSettingsMenu()
                MenuUtils.clickItems(MenuItems.playerData)
            }
        }
        MenuUtils.onOpen(Menu.PLAYER_DATA.title)
    }

    override suspend fun getDaylightCycle(): Boolean {
        openSettingsMenu()

        MenuUtils.clickItems(MenuItems.timeSelector)
        MenuUtils.onOpen(Menu.TIME_SELECTOR.title)

        val current = getLoreCycle(
            MenuUtils.findSlots(MenuItems.daylightCycle).first(),
            listOf("Click to disable!", "Click to enable!")
        ).let { it == "Click to disable!" }

        openSettingsMenu()

        return current
    }

    override suspend fun setDaylightCycle(newDaylightCycle: Boolean) {
        openSettingsMenu()

        MenuUtils.clickItems(MenuItems.timeSelector)
        MenuUtils.onOpen(Menu.TIME_SELECTOR.title)

        val current = getLoreCycle(
            MenuUtils.findSlots(MenuItems.daylightCycle).first(),
            listOf("Click to disable!", "Click to enable!")
        ).let { it == "Click to disable!" }
        if (current == newDaylightCycle) return

        setLoreCycle(
            MenuUtils.findSlots(MenuItems.daylightCycle).first(),
            listOf("Click to disable!", "Click to enable!"),
            if (newDaylightCycle) "Click to disable!" else "Click to enable!",
            maxTries = 1
        )
    }

    override suspend fun getMaxPlayers(): HouseSettings.MaxPlayers {
        openSettingsMenu()

        return HouseSettings.MaxPlayers.entries.firstOrNull {
            it.displayName == getKeyedTitleCycle(
                MenuUtils.findSlots(MenuItems.maxPlayers).first(),
                (MenuItems.maxPlayers.name as NameContains).value
            )
        } ?: throw IllegalStateException("Could not find Max Players")
    }

    override suspend fun setMaxPlayers(newMaxPlayers: HouseSettings.MaxPlayers) {
        openSettingsMenu()

        val current = getMaxPlayers()
        if (newMaxPlayers == current) return

        setKeyedTitleCycle(
            MenuUtils.findSlots(MenuItems.maxPlayers).first(),
            (MenuItems.maxPlayers.name as NameContains).value,
            newMaxPlayers.displayName
        )
    }

    override suspend fun getHouseName(): String {
        TODO("Not yet implemented")
    }

    override suspend fun setHouseName(newName: String) {
        require (newName.length in 3..32) {
            "Name length must be in range 3..32"
        }

        CommandUtils.runCommand("house name $newName")
    }

    override suspend fun getHouseTags(): Set<HouseSettings.HouseTag> {
        openSettingsMenu()

        MenuUtils.clickItems(MenuItems.houseTags)
        MenuUtils.onOpen(Menu.HOUSE_TAGS.title)

        val tags = MenuUtils.currentMenu().screenHandler.inventory.asSequence()
            .filter { it.item == Items.LIME_DYE }
            .map { it.name.string }
            .map { display ->
                HouseSettings.HouseTag.entries.firstOrNull { it.displayName == display }
                    ?: throw IllegalStateException("Unknown house tag '$display'")
            }
            .toSet()

        openSettingsMenu()

        return tags
    }

    override suspend fun setHouseTags(newTags: Set<HouseSettings.HouseTag>) {
        openSettingsMenu()

        MenuUtils.clickItems(MenuItems.houseTags)
        MenuUtils.onOpen(Menu.HOUSE_TAGS.title)

        // Deselect first
        val toDeselect = MenuUtils.currentMenu().screenHandler.slots.asSequence()
            .filter { it.stack.item == Items.LIME_DYE }
            .filter { slot -> !newTags.map { it.displayName }.contains(slot.stack.name.string) }
        for (slot in toDeselect) {
            setDyeToggle(slot, false)
            delay(100)
        }

        // Select
        val toSelect = MenuUtils.currentMenu().screenHandler.slots.asSequence()
            .filter { it.stack.item == Items.GRAY_DYE }
            .filter { slot -> newTags.map { it.displayName }.contains(slot.stack.name.string) }
        for (slot in toSelect) {
            setDyeToggle(slot, true)
            delay(100)
        }

        openSettingsMenu()
    }

    override suspend fun getHouseLanguages(): Set<HouseSettings.HouseLanguage> {
        openSettingsMenu()

        MenuUtils.clickItems(MenuItems.houseLanguage)
        MenuUtils.onOpen(Menu.HOUSE_LANGUAGE.title)

        val languages = MenuUtils.currentMenu().screenHandler.inventory.asSequence()
            .filter { it.item == Items.LIME_DYE }
            .map { it.name.string }
            .map { display ->
                HouseSettings.HouseLanguage.entries.firstOrNull { display.startsWith(it.displayName) }
                    ?: throw IllegalStateException("Unknown house language '$display'")
            }
            .toSet()

        openSettingsMenu()

        return languages
    }

    override suspend fun setHouseLanguages(newLanguages: Set<HouseSettings.HouseLanguage>) {
        openSettingsMenu()

        MenuUtils.clickItems(MenuItems.houseLanguage)
        MenuUtils.onOpen(Menu.HOUSE_LANGUAGE.title)

        val names = newLanguages.map { it.displayName }
        // Deselect first
        val toDeselect = MenuUtils.currentMenu().screenHandler.slots.asSequence()
            .filter { it.stack.item == Items.LIME_DYE }
            .filter { slot -> names.none { n -> slot.stack.name.string.startsWith(n) } }
        for (slot in toDeselect) {
            setDyeToggle(slot, false)
            delay(100)
        }

        // Select
        val toSelect = MenuUtils.currentMenu().screenHandler.slots.asSequence()
            .filter { it.stack.item == Items.GRAY_DYE }
            .filter { slot -> names.any { n -> slot.stack.name.string.startsWith(n) } }
        for (slot in toSelect) {
            setDyeToggle(slot, true)
            delay(100)
        }

        openSettingsMenu()
    }

    override suspend fun getParkourAnnounce(): HouseSettings.ParkourAnnounce {
        openSettingsMenu()

        val slot = MenuUtils.findSlots(MenuItems.parkourAnnounce).first()

        return HouseSettings.ParkourAnnounce.entries.firstOrNull {
            it.displayName == getKeyedTitleCycle(
                slot,
                (MenuItems.parkourAnnounce.name as NameContains).value
            )
        } ?: throw IllegalStateException("Could not find Parkour Announce")
    }

    override suspend fun setParkourAnnounce(newParkourAnnounce: HouseSettings.ParkourAnnounce) {
        openSettingsMenu()

        val slot = MenuUtils.findSlots(MenuItems.parkourAnnounce).first()

        val current = HouseSettings.ParkourAnnounce.entries.firstOrNull {
            it.displayName == getKeyedTitleCycle(
                slot,
                (MenuItems.parkourAnnounce.name as NameContains).value
            )
        } ?: throw IllegalStateException("Could not find Parkour Announce")
        if (newParkourAnnounce == current) return

        setKeyedTitleCycle(
            slot,
            (MenuItems.parkourAnnounce.name as NameContains).value,
            newParkourAnnounce.displayName
        )
    }

    override suspend fun getPlayerStateDuration(): Duration {
        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerStates)
        MenuUtils.onOpen(Menu.PLAYER_STATES.title)

        val current = getInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.playerStateDuration).first(),
            "Current Duration"
        ).let { if (it == "Disabled") Duration.ZERO else Duration.parse(it) }

        MenuUtils.clickItems(MenuItems.back)
        MenuUtils.onOpen(Menu.PLAYER_DATA.title)

        return current
    }

    override suspend fun setPlayerStateDuration(newDuration: Duration) {
        require(newDuration in Duration.ZERO..365.days) {
            "Duration must be between 0 and 365 days."
        }

        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerStates)
        MenuUtils.onOpen(Menu.PLAYER_STATES.title)

        val current = getInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.playerStateDuration).first(),
            "Current Duration"
        ).let { if (it == "Disabled") Duration.ZERO else Duration.parse(it) }

        if (current != newDuration) {
            MenuUtils.clickItems(MenuItems.playerStateDuration)
            InputUtils.textInput(
                if (newDuration == Duration.ZERO) "Disable" else "${newDuration.toString(DurationUnit.SECONDS)}s",
                100
            )
            MenuUtils.onOpen(Menu.PLAYER_STATES.title)
        }

        openPlayerDataMenu()
    }

    override suspend fun getPlayerStateTypes(): MutableMap<HouseSettings.PlayerStateType, Boolean> {
        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerStates)
        MenuUtils.onOpen(Menu.PLAYER_STATES.title)
        MenuUtils.clickItems(MenuItems.playerStateTypes)
        MenuUtils.onOpen(Menu.STATE_TYPES.title)

        val map = mutableMapOf<HouseSettings.PlayerStateType, Boolean>()
        val keys: Array<HouseSettings.PlayerStateType> = HouseSettings.PlayerStateType.entries.toTypedArray()

        for (stateType in keys) {
            val slot = MenuUtils.findSlots(stateType.displayName).first()
            map.putIfAbsent(stateType, getDyeToggle(slot)!!)
        }

        MenuUtils.clickItems(MenuItems.back)
        MenuUtils.onOpen(Menu.PLAYER_STATES.title)
        openPlayerDataMenu()

        return map
    }

    override suspend fun setPlayerStateTypes(newStates: MutableMap<HouseSettings.PlayerStateType, Boolean>) {
        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerStates)
        MenuUtils.onOpen(Menu.PLAYER_STATES.title)

        MenuUtils.clickItems(MenuItems.playerStateTypes)
        MenuUtils.onOpen(Menu.STATE_TYPES.title)

        val keys = HouseSettings.PlayerStateType.entries.toTypedArray()
        for (stateType in keys) {
            val slot = MenuUtils.findSlots(stateType.displayName).first()
            val current = getDyeToggle(slot)
            // Unset settings which aren't provided
            if (!newStates.contains(stateType)) {
                setDyeToggle(slot, false)
                continue
            }
            // Set values which are provided
            val newValue = newStates.getValue(stateType)
            if (newValue == current) continue
            setDyeToggle(slot, newValue)
        }

        MenuUtils.clickItems(MenuItems.back)
        MenuUtils.onOpen(Menu.PLAYER_STATES.title)
        openPlayerDataMenu()
    }

    override suspend fun clearPlayerStates() {
        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerStates)
        MenuUtils.onOpen(Menu.PLAYER_STATES.title)


        MenuUtils.clickItems(MenuItems.clearPlayerStates)
        var slots = MenuUtils.findSlots("Confirm")
        repeat(10) {
            slots = MenuUtils.findSlots("Confirm")
            if (slots.isNotEmpty()) return@repeat
            delay(1000)
        }
        if (slots.isEmpty()) throw IllegalStateException("Failed to clear player states")
        MenuUtils.clickItems("Confirm")
    }

    override suspend fun getDefaultVariableDuration(): Duration {
        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerVariableDurations)
        MenuUtils.onOpen(Menu.PLAYER_VARIABLE_DURATIONS.title)

        val current = getInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.defaultVariableDuration).first(),
            "Current Duration"
        ).let { if (it == "Disabled") Duration.ZERO else Duration.parse(it) }

        openPlayerDataMenu()

        return current
    }

    override suspend fun setDefaultVariableDuration(newDuration: Duration) {
        require(newDuration in Duration.ZERO..365.days) {
            "Duration must be between 0 and 365 days."
        }

        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerVariableDurations)
        MenuUtils.onOpen(Menu.PLAYER_VARIABLE_DURATIONS.title)

        val current = getInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.defaultVariableDuration).first(),
            "Current Duration"
        ).let { if (it == "Disabled") Duration.ZERO else Duration.parse(it) }

        if (current != newDuration) {
            MenuUtils.clickItems(MenuItems.defaultVariableDuration)
            InputUtils.textInput(
                if (newDuration == Duration.ZERO) "Disable" else "${newDuration.toString(DurationUnit.SECONDS)}s",
                100
            )
            MenuUtils.onOpen(Menu.PLAYER_VARIABLE_DURATIONS.title)
        }

        openPlayerDataMenu()
    }

    override suspend fun getVariableDurationOverride(variable: String): Duration? {
        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerVariableDurations)
        MenuUtils.onOpen(Menu.PLAYER_VARIABLE_DURATIONS.title)

        val slots = MenuUtils.findSlots(variable, Items.BOOK)
        if (slots.isEmpty()) return null

        openPlayerDataMenu()

        return slots.first()
            .stack
            .getProperty("Duration")
            ?.let { if (it == "Disabled") Duration.ZERO else Duration.parse(it) }
    }

    override suspend fun setVariableDurationOverride(variable: String, newDuration: Duration) {
        require(newDuration in Duration.ZERO..365.days) {
            "Duration must be between 0 and 365 days."
        }

        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerVariableDurations)
        MenuUtils.onOpen(Menu.PLAYER_VARIABLE_DURATIONS.title)

        if (MenuUtils.findSlots(variable, Items.BOOK).isEmpty()) {
            MenuUtils.clickItems(MenuItems.playerVariableDurationOverride)
            InputUtils.textInput(variable, 100)
            MenuUtils.onOpen(Menu.PLAYER_VARIABLE_DURATIONS.title)
        }

        MenuUtils.clickItems(variable, Items.BOOK)
        InputUtils.textInput(
            if (newDuration == Duration.ZERO) "Disable" else "${newDuration.toString(DurationUnit.SECONDS)}s",
            100
        )
        MenuUtils.onOpen(Menu.PLAYER_VARIABLE_DURATIONS.title)
        openPlayerDataMenu()
    }

    override suspend fun removeVariableDurationOverride(variable: String) {
        openPlayerDataMenu()
        MenuUtils.clickItems(MenuItems.playerVariableDurations)
        MenuUtils.onOpen(Menu.PLAYER_VARIABLE_DURATIONS.title)

        if (MenuUtils.findSlots(variable).isEmpty()) throw IllegalStateException("Tried to remove variable override but there is not one to remove!")
        MenuUtils.clickItems(variable, button = 1)

        openPlayerDataMenu()
    }

    // TODO: Support default inventory layout
    override suspend fun getPvpSettings(): MutableMap<HouseSettings.PvpSettings, Boolean> {
        openSettingsMenu()
        MenuUtils.clickItems(MenuItems.pvpDamageSettings)
        MenuUtils.onOpen("PvP + Damage Settings")

        val map = mutableMapOf<HouseSettings.PvpSettings, Boolean>()
        val keys: Array<HouseSettings.PvpSettings> = HouseSettings.PvpSettings.entries.toTypedArray()

        for (pvpSetting in keys) {
            val slot = MenuUtils.findSlots("${pvpSetting.displayName}: ", partial = true).first()
            map.putIfAbsent(pvpSetting, getDyeToggle(slot)!!)
        }

        openSettingsMenu()

        return map
    }

    override suspend fun setPvpSettings(newPvpSettings: MutableMap<HouseSettings.PvpSettings, Boolean>) {
        openSettingsMenu()
        MenuUtils.clickItems(MenuItems.pvpDamageSettings)
        MenuUtils.onOpen("PvP + Damage Settings")

        val keys: Array<HouseSettings.PvpSettings> = HouseSettings.PvpSettings.entries.toTypedArray()
        for (pvpSetting in keys) {
            val slot = MenuUtils.findSlots("${pvpSetting.displayName}: ", partial = true).first()
            val current = getDyeToggle(slot)
            // Unset settings which aren't provided
            if (!newPvpSettings.contains(pvpSetting)) {
                setDyeToggle(slot, false)
                continue
            }
            // Set values which are provided
            val newValue = newPvpSettings.getValue(pvpSetting)
            if (newValue == current) continue
            setDyeToggle(slot, newValue)
        }

        openSettingsMenu()
    }

    override suspend fun getFishingSettings(): MutableMap<HouseSettings.FishingSettings, Boolean> {
        openSettingsMenu()
        MenuUtils.clickItems(MenuItems.fishingSettings)
        MenuUtils.onOpen("Fishing Settings")

        val map = mutableMapOf<HouseSettings.FishingSettings, Boolean>()
        val keys: Array<HouseSettings.FishingSettings> = HouseSettings.FishingSettings.entries.toTypedArray()

        for (fishingSetting in keys) {
            val slot = MenuUtils.findSlots(fishingSetting.displayName).first()
            map.putIfAbsent(fishingSetting, getDyeToggle(slot)!!)
        }

        openSettingsMenu()

        return map
    }

    override suspend fun setFishingSettings(newFishingSettings: MutableMap<HouseSettings.FishingSettings, Boolean>) {
        openSettingsMenu()
        MenuUtils.clickItems(MenuItems.fishingSettings)
        MenuUtils.onOpen("Fishing Settings")

        val keys: Array<HouseSettings.FishingSettings> = HouseSettings.FishingSettings.entries.toTypedArray()
        for (fishingSetting in keys) {
            val slot = MenuUtils.findSlots(fishingSetting.displayName).first()
            val current = getDyeToggle(slot)
            // Unset settings which aren't provided
            if (!newFishingSettings.contains(fishingSetting)) {
                setDyeToggle(slot, false)
                continue
            }
            // Set values which are provided
            val newValue = newFishingSettings.getValue(fishingSetting)
            if (newValue == current) continue
            setDyeToggle(slot, newValue)
        }

        openSettingsMenu()
    }


    private object MenuItems {
        val houseSettings = ItemSelector(
            name = NameExact("House Settings"),
            item = ItemExact(Items.COMPARATOR)
        )
        val timeSelector = ItemSelector(
            name = NameExact("Time Selector"),
            item = ItemExact(Items.CLOCK)
        )
        val daylightCycle = ItemSelector(
            name = NameExact("Toggle Daylight Cycle"),
            item = ItemExact(Items.DAYLIGHT_DETECTOR)
        )
        val maxPlayers = ItemSelector(
            name = NameContains("Max Players"),
            item = ItemExact(Items.PLAYER_HEAD)
        )
        val houseTags = ItemSelector(
            name = NameExact("House Tags"),
            item = ItemExact(Items.NAME_TAG)
        )
        val houseLanguage = ItemSelector(
            name = NameExact("House Language"),
            item = ItemExact(Items.BOOK)
        )
        val parkourAnnounce = ItemSelector(
            name = NameContains("Parkour Announce"),
            item = ItemExact(Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
        )

        val playerData = ItemSelector(
            name = NameExact("Player Data"),
            item = ItemExact(Items.FEATHER)
        )
        val playerStates = ItemSelector(
            name = NameExact("Player States"),
            item = ItemExact(Items.CHEST)
        )
        val playerStateDuration = ItemSelector(
            name = NameExact("Player State Duration"),
            item = ItemExact(Items.NAME_TAG)
        )
        val playerStateTypes = ItemSelector(
            name = NameExact("State Types"),
            item = ItemExact(Items.ENDER_CHEST)
        )
        val clearPlayerStates = ItemSelector(
            name = NameExact("Clear Player States"),
            item = ItemExact(Items.TNT)
        )
        val playerVariableDurations = ItemSelector(
            name = NameExact("Player Variable Durations"),
            item = ItemExact(Items.FEATHER)
        )
        val defaultVariableDuration = ItemSelector(
            name = NameExact("Set Default Duration"),
            item = ItemExact(Items.NAME_TAG)
        )
        val playerVariableDurationOverride = ItemSelector(
            name = NameExact("Add Key Override"),
            item = ItemExact(Items.PAPER)
        )

        val pvpDamageSettings = ItemSelector(
            name = NameExact("PvP + Damage Settings"),
            item = ItemExact(Items.STONE_SWORD)
        )
        val fishingSettings = ItemSelector(
            name = NameExact("Fishing Settings"),
            item = ItemExact(Items.FISHING_ROD)
        )
        val mainMenu = ItemSelector(
            name = NameExact("Main Menu"),
            item = ItemExact(Items.NETHER_STAR)
        )
        val back = ItemSelector(
            name = NameExact("Go Back"),
            item = ItemExact(Items.ARROW)
        )
    }

    private enum class Menu(val title: String) {
        MAIN("Housing Menu"),
        SETTINGS("House Settings"),
        TIME_SELECTOR("Time Selector"),
        HOUSE_TAGS("House Tags"),
        HOUSE_LANGUAGE("House Language"),
        PLAYER_DATA("Player Data"),
        PLAYER_STATES("Player States"),
        STATE_TYPES("State Types"),
        PLAYER_VARIABLE_DURATIONS("Player Variable Durations"),
        PVP_SETTINGS("PvP + Damage Settings"),
        FISHING_SETTINGS("Fishing Settings"),
        UNKNOWN("");

        companion object {
            fun fromTitle(title: String?): Menu = entries.find { it.title == title } ?: UNKNOWN
        }
    }

    private fun getCurrentMenu(): Menu {
        val title = runCatching { MenuUtils.currentMenu().title.string }.getOrNull()
        return Menu.fromTitle(title)
    }

}