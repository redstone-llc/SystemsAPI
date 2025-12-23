package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.api.Group
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.InputUtils.getInlineKeyedLoreCycle
import llc.redstone.systemsapi.util.InputUtils.getKeyedTitleCycle
import llc.redstone.systemsapi.util.InputUtils.setInlineKeyedLoreCycle
import llc.redstone.systemsapi.util.InputUtils.setKeyedTitleCycle
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.ItemUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.ItemUtils.ItemSelector
import llc.redstone.systemsapi.util.ItemUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.item.Items

class GroupImporter(override var name: String) : Group {
    private fun getCurrentMenu(): String? {
        val title = runCatching { MenuUtils.currentMenu().title.string }.getOrNull()
        return title
    }


    private suspend fun openGroupsMenu() {
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
        openGroupsMenu()
        if (getCurrentMenu() == "Permissions and Groups") {
            MenuUtils.clickItems(name)
        }
        if (getCurrentMenu()?.startsWith("Permissions $name") == true) {
            MenuUtils.clickItems(MenuItems.back)
        }
        MenuUtils.onOpen("Edit Group")
    }

    override suspend fun setName(newName: String) {
        require(newName.length in 1..16) { "Group name length must be in range 1..16" }
        openGroupMenu()
        MenuUtils.clickItems(MenuItems.name)
        InputUtils.textInput(newName, 100)
        MenuUtils.onOpen("Edit Group")

        this.name = newName
    }

    override suspend fun getTag(): String? {
        openGroupMenu()
        return MenuUtils.findSlots(MenuItems.tag).first()
            .stack
            .getProperty("Current Tag")!!
            .let { if (it == "Not Set") null else it.removeSurrounding("[", "]") }
    }

    override suspend fun setTag(newTag: String) {
        require(newTag.length in 1..10) { "Group tag length must be in range 1.10" }
        openGroupMenu()

        val current = MenuUtils.findSlots(MenuItems.tag).first()
            .stack
            .getProperty("Current Tag")!!
            .let { if (it == "Not Set") null else it.removeSurrounding("[", "]") }
        if (current == newTag) return

        MenuUtils.clickItems(MenuItems.tag)
        InputUtils.textInput(newTag, 100)
        MenuUtils.onOpen("Edit Group")
    }

    override suspend fun getTagVisibleInChat(): Boolean {
        openGroupMenu()
        return getInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.tag).first(),
            "Tag Shows in Chat"
        ) == "Enabled"
    }

    override suspend fun setTagVisibleInChat(newVisibleInChat: Boolean) {
        openGroupMenu()

        val current = getInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.tag).first(),
            "Tag Shows in Chat"
        ) == "Enabled"
        if (current == newVisibleInChat) return

        setInlineKeyedLoreCycle(
            MenuUtils.findSlots(MenuItems.tag).first(),
            "Tag Shows in Chat",
            if (newVisibleInChat) "Enabled" else "Disabled",
            button = 1
        )
    }

    override suspend fun getColor(): Group.GroupColor {
        openGroupMenu()

        return Group.GroupColor.entries.firstOrNull() {
            it.displayName == MenuUtils.findSlots(MenuItems.color).first()
                .stack.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get group color")
    }

    override suspend fun setColor(newColor: Group.GroupColor) {
        openGroupMenu()

        val current = Group.GroupColor.entries.firstOrNull() {
            it.displayName == MenuUtils.findSlots(MenuItems.color).first()
                .stack.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get group color")
        if (current == newColor) return

        MenuUtils.clickItems(MenuItems.color)
        MenuUtils.onOpen("Select Group Color")
        MenuUtils.clickItems(newColor.displayName)
        MenuUtils.onOpen("Edit Group")
    }

    override suspend fun getPriority(): Int {
        openGroupMenu()
        return MenuUtils.findSlots(MenuItems.priority).first()
            .stack
            .getProperty("Current Priority")
            ?.toInt() ?: throw IllegalStateException("Failed to get group priority")
    }

    override suspend fun setPriority(newPriority: Int) {
        require(newPriority in 1..20) { "Priority must be in range 1..20" }

        val current = MenuUtils.findSlots(MenuItems.priority).first()
            .stack
            .getProperty("Current Priority")
            ?.toInt() ?: throw IllegalStateException("Failed to get group priority")
        if (current == newPriority) return

        MenuUtils.clickItems(MenuItems.priority)
        InputUtils.textInput(newPriority.toString(), 100)
        MenuUtils.onOpen("Edit Group")
    }

    override suspend fun getPermissions(): MutableMap<Group.GroupPermission, Group.PermissionValue> {
        openGroupMenu()
        MenuUtils.clickItems(MenuItems.permissions)
        MenuUtils.onOpen("Permissions $name")

        val map: MutableMap<Group.GroupPermission, Group.PermissionValue> = mutableMapOf()
        for (permission in Group.GroupPermission.entries) {
            val current = getKeyedTitleCycle(
                MenuUtils.findSlots("${permission.displayName}: ", partial = true, paginated = true).first(),
                permission.displayName
            )

            val currentTyped: Group.PermissionValue = when (permission.valueType) {
                Group.PermissionValue.BooleanValue::class -> {
                    Group.PermissionValue.BooleanValue(current == "Enabled")
                }
                Group.PermissionValue.ChatValue::class -> {
                    val chatVal = Group.PermissionValue.ChatValues.entries.firstOrNull { it.displayName == current }
                        ?: throw IllegalStateException("Unknown chat permission value '$current' for ${permission.name}")
                    Group.PermissionValue.ChatValue(chatVal)
                }
                Group.PermissionValue.GameModeValue::class -> {
                    val gmVal = Group.PermissionValue.GameModeValues.entries.firstOrNull { it.displayName == current }
                        ?: throw IllegalStateException("Unknown gamemode permission value '$current' for ${permission.name}")
                    Group.PermissionValue.GameModeValue(gmVal)
                }
                else -> throw IllegalStateException("Unsupported permission value type: ${permission.valueType}")
            }

            map.putIfAbsent(permission, currentTyped)
        }
        return map
    }

    override suspend fun setPermissions(newPermissions: MutableMap<Group.GroupPermission, Group.PermissionValue>) {
        openGroupMenu()
        MenuUtils.clickItems(MenuItems.permissions)
        MenuUtils.onOpen("Permissions $name")

        for (permission in Group.GroupPermission.entries) {
            if (!newPermissions.contains(permission)) continue

            val current = getKeyedTitleCycle(
                MenuUtils.findSlots("${permission.displayName}: ", partial = true, paginated = true).first(),
                permission.displayName
            )

            val currentTyped: Group.PermissionValue = when (permission.valueType) {
                Group.PermissionValue.BooleanValue::class -> {
                    Group.PermissionValue.BooleanValue(current == "Enabled")
                }
                Group.PermissionValue.ChatValue::class -> {
                    val chatVal = Group.PermissionValue.ChatValues.entries.firstOrNull { it.displayName == current }
                                  ?: throw IllegalStateException("Unknown chat permission value '$current' for ${permission.name}")
                    Group.PermissionValue.ChatValue(chatVal)
                }
                Group.PermissionValue.GameModeValue::class -> {
                    val gmVal = Group.PermissionValue.GameModeValues.entries.firstOrNull { it.displayName == current }
                                ?: throw IllegalStateException("Unknown gamemode permission value '$current' for ${permission.name}")
                    Group.PermissionValue.GameModeValue(gmVal)
                }
                else -> throw IllegalStateException("Unsupported permission value type: ${permission.valueType}")
            }

            if (currentTyped != newPermissions[permission]) {
                val slot = MenuUtils.findSlots("${permission.displayName}: ", partial = true, paginated = true).first()
                when (val newVal = newPermissions[permission]) {
                    is Group.PermissionValue.BooleanValue -> {
                        setKeyedTitleCycle(
                            slot,
                            permission.displayName,
                            if (newVal.value) "On" else "Off"
                        )
                    }
                    is Group.PermissionValue.ChatValue -> {
                        setKeyedTitleCycle(
                            slot,
                            permission.displayName,
                            newVal.value.displayName
                        )
                    }
                    is Group.PermissionValue.GameModeValue -> {
                        setKeyedTitleCycle(
                            slot,
                            permission.displayName,
                            newVal.value.displayName
                        )
                    }
                    else -> throw IllegalStateException("Unsupported permission value type: ${newVal?.javaClass}")
                }
            }
        }
    }

    override suspend fun clearGroupPlayers() {
        openGroupMenu()
        MenuUtils.clickItems(MenuItems.clearPlayers)
        MenuUtils.onOpen("Are you sure?")
        MenuUtils.clickItems("Confirm")
        MenuUtils.onOpen("Edit Group")
    }

    suspend fun exists(): Boolean {
        openGroupsMenu()
        val slots = MenuUtils.findSlots(this.name)
        return slots.isNotEmpty()
    }
    suspend fun create() {
        openGroupsMenu()
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
        val nextPage = ItemSelector(
            name = NameExact("Next Page"),
            item = ItemExact(Items.ARROW)
        )
    }
}