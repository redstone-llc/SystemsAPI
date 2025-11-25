package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Command
import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.ItemUtils.loreLine
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Items

internal class CommandImporter(override var name: String) : Command {
    private fun isCommandEditMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Edit /$name") // absence of colon is intentional; hypixel weird
    }

    private fun isActionsMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Actions: /$name")
    }

    private suspend fun openCommandEditMenu() {
        if (isCommandEditMenuOpen()) return

        CommandUtils.runCommand("command edit $name")
        MenuUtils.onOpen("Edit /$name")
        delay(50)
    }

    private suspend fun openActionsEditMenu() {
        if (isActionsMenuOpen()) return

        CommandUtils.runCommand("command actions $name")
        MenuUtils.onOpen("Actions: /$name")
        delay(50)
    }

    override suspend fun setName(newName: String) {
        openCommandEditMenu()

        MenuUtils.clickMenuSlot(MenuItems.RENAME_COMMAND)
        TextUtils.input(newName, 100L)

        name = newName
    }

    override suspend fun createIfNotExists(): Boolean {
        val commands = getTabCompletions("command edit")
        if (commands.contains(name)) return false

        CommandUtils.runCommand("command create $name")
        return true
    }

    override suspend fun getCommandMode(): Command.CommandMode {
        openCommandEditMenu()

        val mode = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.TOGGLE_COMMAND_MODE) }
            ?.stack
            ?.loreLine(1, false)
            ?.split(" ")
            ?.getOrNull(1)
            ?.let { value -> if (value == "Self") Command.CommandMode.SELF else Command.CommandMode.TARGETED }
            ?: error("[Command $name] Failed to get required group priority.")

        return mode
    }

    override suspend fun setCommandMode(newCommandMode: Command.CommandMode) {
        openCommandEditMenu()

        val mode = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.TOGGLE_COMMAND_MODE) }
            ?.stack
            ?.loreLine(1, false)
            ?.split(" ")
            ?.getOrNull(1)
            ?.let { value -> if (value == "Self") Command.CommandMode.SELF else Command.CommandMode.TARGETED }
            ?: error("[Command $name] Failed to set required group priority to ${newCommandMode.name}.")
        if (mode == newCommandMode) return

        MenuUtils.clickMenuSlot(MenuItems.TOGGLE_COMMAND_MODE)
    }

    override suspend fun getRequiredGroupPriority(): Int {
        openCommandEditMenu()

        val priority = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.REQUIRED_GROUP_PRIORITY) }
            ?.stack
            ?.loreLine(4, false)
            ?.split(" ")
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: error("[Command $name] Failed to get required group priority.")

        return priority
    }

    override suspend fun setRequiredGroupPriority(newPriority: Int) {
        openCommandEditMenu()

        val priority = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.REQUIRED_GROUP_PRIORITY) }
            ?.stack
            ?.loreLine(4, false)
            ?.split(" ")
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: error("[Command $name] Failed to set required group priority to $newPriority.")
        if (priority == newPriority) return

        MenuUtils.clickMenuSlot(MenuItems.REQUIRED_GROUP_PRIORITY)
        TextUtils.input(newPriority.toString(), 100L)
    }

    override suspend fun getListed(): Boolean {
        openCommandEditMenu()

        val listed = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.LISTED) }
            ?.stack
            ?.item
            ?.let { item -> item == Items.LIME_DYE }
            ?: error("[Command $name] Failed to get listed.")

        return listed
    }
    override suspend fun setListed(newListed: Boolean) {
        openCommandEditMenu()

        val listed = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.LISTED) }
            ?.stack
            ?.item
            ?.let { item -> item == Items.LIME_DYE }
            ?: error("[Command $name] Failed to set listed to ${newListed}.")
        if (listed == newListed) return

        MenuUtils.clickMenuSlot(MenuItems.LISTED)
    }

    override suspend fun getActionContainer(): ActionContainer {
        openActionsEditMenu()
        return ActionContainer("Actions: /$name")
    }

    override suspend fun delete() {
        CommandUtils.runCommand("command delete $name")
    }

    object MenuItems {
        val RENAME_COMMAND = MenuSlot(Items.ANVIL, "Rename Command")
        val TOGGLE_COMMAND_MODE = MenuSlot(null, "Toggle Command Mode")
        val REQUIRED_GROUP_PRIORITY = MenuSlot(Items.FILLED_MAP, "Required Group Priority")
        val LISTED = MenuSlot(null, "Listed")
    }
}