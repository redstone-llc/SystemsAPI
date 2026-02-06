package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.api.Group
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.InputUtils.getInlineKeyedLoreCycle
import llc.redstone.systemsapi.util.InputUtils.getKeyedTitleCycle
import llc.redstone.systemsapi.util.InputUtils.setInlineKeyedLoreCycle
import llc.redstone.systemsapi.util.InputUtils.setKeyedTitleCycle
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot

class GroupImporter(override var name: String) : Group {
    private fun getCurrentMenu(): String? = runCatching { MenuUtils.currentMenu().title.string }.getOrNull()

    private suspend fun openGroupListMenu() {
        if (getCurrentMenu() == "Edit Group") return
        if (getCurrentMenu() == null) {
            CommandUtils.runCommand("menu")
            MenuUtils.onOpen("Housing Menu")
        }
        if (getCurrentMenu() == "Housing Menu") {
            MenuUtils.clickItems(MenuItems.groups)
            MenuUtils.onOpen("Permissions and Groups")
        }
    }

    private suspend fun openGroupMenu() {
        openGroupListMenu()
        if (getCurrentMenu() == "Permissions and Groups") {
            MenuUtils.clickItems(name)
        }
        if (getCurrentMenu()?.startsWith("Permissions $name") == true) {
            MenuUtils.clickItems(MenuItems.back)
        }
        MenuUtils.onOpen("Edit Group")
    }

    override suspend fun setName(newName: String): Group {
        require(newName.length in 1..16) { "Group name length must be in range 1..16" }
        openGroupMenu()
        MenuUtils.clickItems(MenuItems.name)
        InputUtils.textInput(newName)
        MenuUtils.onOpen("Edit Group")

        this.name = newName
        return this
    }

    override suspend fun getTag(): String? {
        openGroupMenu()
        return MenuUtils.findSlots(MenuItems.tag).first()
            .stack
            .getProperty("Current Tag")!!
            .let { if (it == "Not Set") null else it.removeSurrounding("[", "]") }
    }

    override suspend fun setTag(newTag: String): Group {
        require(newTag.length in 1..10) { "Group tag length must be in range 1.10" }
        openGroupMenu()

        val current = MenuUtils.findSlots(MenuItems.tag).first()
            .stack
            .getProperty("Current Tag")!!
            .let { if (it == "Not Set") null else it.removeSurrounding("[", "]") }
        if (current == newTag) return this

        MenuUtils.clickItems(MenuItems.tag)
        InputUtils.textInput(newTag)
        MenuUtils.onOpen("Edit Group")

        return this
    }

    override suspend fun getTagVisibleInChat(): Boolean {
        openGroupMenu()
        return getInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.tag).first(),
            "Tag Shows in Chat"
        ) == "Enabled"
    }

    override suspend fun setTagVisibleInChat(newVisibleInChat: Boolean): Group {
        openGroupMenu()

        val current = getInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.tag).first(),
            "Tag Shows in Chat"
        ) == "Enabled"
        if (current == newVisibleInChat) return this

        setInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.tag).first(),
            "Tag Shows in Chat",
            if (newVisibleInChat) "Enabled" else "Disabled",
            button = 1
        )

        return this
    }

    override suspend fun getColor(): Group.GroupColor {
        openGroupMenu()

        return Group.GroupColor.entries.firstOrNull() {
            it.displayName == MenuUtils.findSlots(MenuItems.color).first()
                .stack.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get group color")
    }

    override suspend fun setColor(newColor: Group.GroupColor): Group {
        openGroupMenu()

        val current = Group.GroupColor.entries.firstOrNull() {
            it.displayName == MenuUtils.findSlots(MenuItems.color).first()
                .stack.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get group color")
        if (current == newColor) return this

        MenuUtils.clickItems(MenuItems.color)
        MenuUtils.onOpen("Select Group Color")
        MenuUtils.clickItems(newColor.displayName)
        MenuUtils.onOpen("Edit Group")

        return this
    }

    override suspend fun getPriority(): Int {
        openGroupMenu()
        return MenuUtils.findSlots(MenuItems.priority).first()
            .stack
            .getProperty("Current Priority")
            ?.toInt() ?: throw IllegalStateException("Failed to get group priority")
    }

    override suspend fun setPriority(newPriority: Int): Group {
        require(newPriority in 1..20) { "Priority must be in range 1..20" }

        val current = MenuUtils.findSlots(MenuItems.priority).first()
            .stack
            .getProperty("Current Priority")
            ?.toInt() ?: throw IllegalStateException("Failed to get group priority")
        if (current == newPriority) return this

        MenuUtils.clickItems(MenuItems.priority)
        InputUtils.textInput(newPriority.toString())
        MenuUtils.onOpen("Edit Group")

        return this
    }

    override suspend fun getPermissions(): Group.PermissionSet {
        openGroupMenu()
        MenuUtils.clickItems(MenuItems.permissions)
        MenuUtils.onOpen("Permissions $name")

        val result = Group.PermissionSet()

        for (permission in Group.Permissions.all) {
            val current = getKeyedTitleCycle(
                MenuUtils.findSlots("${permission.displayName}: ", partial = true, paginated = true).first(),
                permission.displayName
            )

            // needs a capture helper because of generics issues
            captureAndParse(result, permission, current)
        }
        return result
    }

    override suspend fun setPermissions(newPermissions: Group.PermissionSet): Group {
        openGroupMenu()
        MenuUtils.clickItems(MenuItems.permissions)
        MenuUtils.onOpen("Permissions $name")

        for (permission in Group.Permissions.all) {
            if (!newPermissions.contains(permission)) continue

            val slot = MenuUtils.findSlots("${permission.displayName}: ", partial = true, paginated = true).first()
            val current = getKeyedTitleCycle(slot, permission.displayName)

            captureAndSet(newPermissions, permission, slot, current)
        }

        return this
    }

    override suspend fun clearGroupPlayers(): Group {
        openGroupMenu()
        MenuUtils.clickItems(MenuItems.clearPlayers)
        MenuUtils.onOpen("Are you sure?")
        MenuUtils.clickItems("Confirm")
        MenuUtils.onOpen("Edit Group")
        return this
    }

    suspend fun exists(): Boolean {
        openGroupListMenu()
        val slots = MenuUtils.findSlots(this.name)
        return slots.isNotEmpty()
    }
    suspend fun create() {
        openGroupListMenu()
        MenuUtils.clickItems(MenuItems.create)
        MenuUtils.onOpen("Edit Group")
    }
    override suspend fun delete() {
        openGroupMenu()
        MenuUtils.clickItems(MenuItems.delete)
        MenuUtils.onOpen("Are you sure?")
        MenuUtils.clickItems("Confirm")
        MenuUtils.onOpen("Permissions and Groups")
    }

    private fun <T> captureAndParse(set: Group.PermissionSet, key: Group.PermissionKey<T>, current: String) {
        try {
            val value = key.parseFromMenu(current)
            set[key] = value
        } catch (e: Exception) {
            println("Failed to parse ${key.displayName}: $current")
        }
    }

    private suspend fun <T> captureAndSet(set: Group.PermissionSet, key: Group.PermissionKey<T>, slot: Slot, current: String) {
        val newValue = key.toMenuText(set[key] ?: return)
        if (current == newValue) return

        setKeyedTitleCycle(
            slot,
            key.displayName,
            newValue,
            confirm = (key == Group.Permissions.BUILD && newValue == "On") ||
                      (key == Group.Permissions.OFFLINE_BUILD && newValue == "On")
        )
    }

    private object MenuItems {
        val groups = ItemSelector(
            name = NameExact("Permissions and Groups"),
            item = ItemExact(Items.FILLED_MAP)
        )
        val create = ItemSelector(
            name = NameExact("Create Group"),
            item = ItemExact(Items.PAPER)
        )
        val name = ItemSelector(
            name = NameExact("Rename Group"),
            item = ItemExact(Items.PAPER)
        )
        val tag = ItemSelector(
            name = NameExact("Change Tag"),
            item = ItemExact(Items.OAK_SIGN)
        )
        val color = ItemSelector(
            name = NameExact("Change Color"),
            item = ItemExact(Items.REDSTONE)
        )
        val priority = ItemSelector(
            name = NameExact("Change Priority"),
            item = ItemExact(Items.GOLD_INGOT)
        )
        val permissions = ItemSelector(
            name = NameExact("Edit Permissions"),
            item = ItemExact(Items.BOOK)
        )
        val clearPlayers = ItemSelector(
            name = NameExact("Clear Group Players"),
            item = ItemExact(Items.CAULDRON)
        )
        val delete = ItemSelector(
            name = NameExact("Delete Group"),
            item = ItemExact(Items.TNT)
        )
        val back = ItemSelector(
            name = NameExact("Go Back"),
            item = ItemExact(Items.ARROW)
        )
    }
}