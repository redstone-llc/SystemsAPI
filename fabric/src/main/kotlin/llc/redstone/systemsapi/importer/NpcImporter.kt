package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.LOGGER
import llc.redstone.systemsapi.api.Npc
import llc.redstone.systemsapi.api.Npc.NpcType
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.InputUtils.getKeyedLoreCycle
import llc.redstone.systemsapi.util.InputUtils.getKeyedTitleCycle
import llc.redstone.systemsapi.util.InputUtils.setKeyedLoreCycle
import llc.redstone.systemsapi.util.InputUtils.setKeyedTitleCycle
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameContains
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import net.minecraft.item.Items

open class NpcImporter(override var name: String) : Npc {
    private fun getCurrentMenu(): String? = runCatching { MenuUtils.currentMenu().title.string }.getOrNull()

    private suspend fun openNpcListMenu() {
        when (getCurrentMenu()) {
            "NPCs" -> return
            "Systems" -> {
                MenuUtils.clickItems(MenuItems.npcs)
                MenuUtils.onOpen("NPCs")
            }
            "Housing Menu" -> {
                MenuUtils.clickItems(MenuItems.systems)
                MenuUtils.onOpen("Systems")
                MenuUtils.clickItems(MenuItems.npcs)
                MenuUtils.onOpen("NPCs")
            }
            else -> {
                CommandUtils.runCommand("menu")
                MenuUtils.onOpen("Housing Menu")
                MenuUtils.clickItems(MenuItems.systems)
                MenuUtils.onOpen("Systems")
                MenuUtils.clickItems(MenuItems.npcs)
                MenuUtils.onOpen("NPCs")
            }
        }
    }

    private suspend fun openNpcMenu() {
        openNpcListMenu()
        MenuUtils.clickItems(name)
        MenuUtils.onOpen("Edit NPC - $name")
    }

    override suspend fun setName(newName: String) {
        require(newName.length in 1..32) { "Name length must be between 1 and 32" }
        openNpcMenu()

        MenuUtils.clickItems(MenuItems.name)
        LOGGER.info("test-1")
        InputUtils.textInput(newName, 100)
        MenuUtils.onOpen("Edit NPC - $newName")

        this.name = newName
    }

    override suspend fun getNpcType(): Npc.NpcType {
        openNpcMenu()

        return NpcType.entries.firstOrNull() {
            it.displayName == MenuUtils.findSlots(MenuItems.type).first()
                .stack
                .getProperty("Currently Selected")
        } ?: throw IllegalStateException("Could not find NPC type")
    }

    override suspend fun setNpcType(newNpcType: Npc.NpcType) {
        openNpcMenu()

        MenuUtils.clickItems(MenuItems.type)
        MenuUtils.onOpen("Change NPC Type")
        MenuUtils.clickItems(newNpcType.displayName, paginated = true)
        MenuUtils.onOpen("Edit NPC - $name")
    }

    override suspend fun getLookAtPlayers(): Boolean {
        openNpcMenu()
        return getKeyedTitleCycle(
            MenuUtils.findSlots(MenuItems.lookAtPlayers).first(),
            (MenuItems.lookAtPlayers.name as NameContains).value
        ).let { it == "On" }
    }

    override suspend fun setLookAtPlayers(newLookAtPlayers: Boolean) {
        openNpcMenu()
        val current = getLookAtPlayers()
        if (current == newLookAtPlayers) return

        setKeyedTitleCycle(
            MenuUtils.findSlots(MenuItems.lookAtPlayers).first(),
            (MenuItems.lookAtPlayers.name as NameContains).value,
            if (newLookAtPlayers) "On" else "Off"
        )
    }

    override suspend fun getHideNameTag(): Boolean {
        openNpcMenu()
        return getKeyedTitleCycle(
            MenuUtils.findSlots(MenuItems.hideNameTag).first(),
            (MenuItems.hideNameTag.name as NameContains).value
        ).let { it == "On" }
    }

    override suspend fun setHideNameTag(newHideNameTag: Boolean) {
        openNpcMenu()
        val current = getLookAtPlayers()
        if (current == newHideNameTag) return

        setKeyedTitleCycle(
            MenuUtils.findSlots(MenuItems.hideNameTag).first(),
            (MenuItems.hideNameTag.name as NameContains).value,
            if (newHideNameTag) "On" else "Off"
        )
    }

    override suspend fun getLeftClickActionContainer(): ActionContainer {
        openNpcMenu()
        MenuUtils.clickItems(MenuItems.leftClickActions)
        MenuUtils.onOpen("Edit Actions")
        return ActionContainer("Edit Actions")
    }

    override suspend fun getRightClickActionContainer(): ActionContainer {
        openNpcMenu()
        MenuUtils.clickItems(MenuItems.rightClickActions)
        MenuUtils.onOpen("Edit Actions")
        return ActionContainer("Edit Actions")
    }

    override suspend fun getLeftClickRedirect(): Boolean {
        openNpcMenu()
        MenuUtils.clickItems(MenuItems.leftClickActions)
        MenuUtils.onOpen("Edit Actions")

        val current =  getKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.leftClickRedirect).first(),
            "Current Value"
        ).let { it == "Enabled" }

        MenuUtils.clickItems(MenuItems.back)
        MenuUtils.onOpen("Edit NPC - $current")

        return current
    }

    override suspend fun setLeftClickRedirect(newLeftClickRedirect: Boolean) {
        openNpcMenu()
        MenuUtils.clickItems(MenuItems.leftClickActions)
        MenuUtils.onOpen("Edit Actions")

        val current =  getKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.leftClickRedirect).first(),
            "Current Value"
        ).let { it == "Enabled" }
        if (current == newLeftClickRedirect) return

        setKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.leftClickRedirect).first(),
            "Current Value",
            if (newLeftClickRedirect) "Enabled" else "Disabled"
        )

        MenuUtils.clickItems(MenuItems.back)
        MenuUtils.onOpen("Edit NPC - $current")
    }

    override suspend fun delete() {
        openNpcMenu()
        MenuUtils.clickItems(MenuItems.delete)
    }


    suspend fun exists(): Boolean {
        openNpcListMenu()
        return MenuUtils.findSlots(name, paginated = true).isNotEmpty()
    }


    private object MenuItems {
        val systems = ItemSelector(
            name = NameExact("Systems"),
            item = ItemExact(Items.ACTIVATOR_RAIL)
        )
        val npcs = ItemSelector(
            name = NameExact("NPCs"),
            item = ItemExact(Items.PLAYER_HEAD)
        )
        val type = ItemSelector(
            name = NameExact("Change NPC Type")
        )
        val name = ItemSelector(
            name = NameExact("Rename NPC"),
            item = ItemExact(Items.ANVIL)
        )
        val leftClickActions = ItemSelector(
            name = NameExact("Left Click Actions"),
            item = ItemExact(Items.IRON_SWORD)
        )
        val rightClickActions = ItemSelector(
            name = NameExact("Left Click Actions"),
            item = ItemExact(Items.BOOK)
        )
        val lookAtPlayers = ItemSelector(
            name = NameContains("Look at Players"),
            item = ItemExact(Items.COMPASS)
        )
        val hideNameTag = ItemSelector(
            name = NameContains("Hide Name Tag"),
            item = ItemExact(Items.NAME_TAG)
        )
        val leftClickRedirect = ItemSelector(
            name = NameExact("Left Click Redirect"),
            item = ItemExact(Items.DETECTOR_RAIL)
        )
        val delete = ItemSelector(
            name = NameExact("Remove NPC"),
            item = ItemExact(Items.TNT)
        )
        val back = ItemSelector(
            name = NameExact("Go Back"),
            item = ItemExact(Items.ARROW)
        )
    }
}