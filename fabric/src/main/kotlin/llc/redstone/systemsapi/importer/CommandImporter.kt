package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Command
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot

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

        MenuItems.RENAME_COMMAND.click()
        InputUtils.textInput(newName, 100L)

        name = newName
    }

    override suspend fun getCommandMode(): Command.CommandMode {
        openCommandEditMenu()

        val mode = MenuItems.TOGGLE_COMMAND_MODE.find()
            .stack
            ?.getProperty("Current")
            ?.let { if (it == "Self") Command.CommandMode.SELF else Command.CommandMode.TARGETED }
            ?: throw IllegalStateException("Failed to get the command mode")
        return mode
    }

    override suspend fun setCommandMode(newCommandMode: Command.CommandMode) {
        openCommandEditMenu()

        val mode = MenuItems.TOGGLE_COMMAND_MODE.find()
            .stack
            ?.getProperty("Current")
            ?.let { if (it == "Self") Command.CommandMode.SELF else Command.CommandMode.TARGETED }
            ?: throw IllegalStateException("Failed to set the command mode to ${newCommandMode.name}")
        if (mode == newCommandMode) return

        MenuItems.TOGGLE_COMMAND_MODE.click()
    }

    override suspend fun getRequiredGroupPriority(): Int {
        openCommandEditMenu()

        val priority = MenuItems.REQUIRED_GROUP_PRIORITY.find()
            .stack
            ?.getProperty("Current")
            ?.toIntOrNull()
            ?: throw IllegalStateException("Failed to get the required group priority")

        return priority
    }

    override suspend fun setRequiredGroupPriority(newPriority: Int) {
        openCommandEditMenu()

        val priority = MenuItems.REQUIRED_GROUP_PRIORITY.find()
            .stack
            ?.getProperty("Current")
            ?.toIntOrNull()
            ?: throw IllegalStateException("Failed to set the required group priority to $newPriority.")
        if (priority == newPriority) return

        MenuItems.REQUIRED_GROUP_PRIORITY.click()
        InputUtils.textInput(newPriority.toString(), 100L)
    }

    override suspend fun getListed(): Boolean {
        openCommandEditMenu()

        val listed = MenuItems.LISTED.find()
            .stack
            ?.item
            ?.let { item -> item == Items.LIME_DYE }
            ?: throw IllegalStateException("Failed to get the listed value")

        return listed
    }

    override suspend fun setListed(newListed: Boolean) {
        openCommandEditMenu()

        val listed = MenuItems.LISTED.find()
            .stack
            ?.item
            ?.let { item -> item == Items.LIME_DYE }
            ?: throw IllegalStateException("Failed to set the listed value to $newListed.")
        if (listed == newListed) return

        MenuItems.LISTED.click()
    }

    override suspend fun getActionContainer(): ActionContainer {
        openActionsEditMenu()
        return ActionContainer("Actions: /$name")
    }

    suspend fun exists(): Boolean = getTabCompletions("command edit").contains(name)
    suspend fun create() {
        if (this.exists()) throw IllegalStateException("Command already exists")
        CommandUtils.runCommand("command create $name")
    }

    override suspend fun delete() = CommandUtils.runCommand("command delete $name")

    private enum class MenuItems(
        val label: String,
        val type: Item? = null
    ) {
        RENAME_COMMAND("Rename Command", Items.ANVIL),
        TOGGLE_COMMAND_MODE("Toggle Command Mode"),
        REQUIRED_GROUP_PRIORITY("Required Group Priority", Items.FILLED_MAP),
        LISTED("Listed");

        suspend fun click() = if (type != null) MenuUtils.clickItems(label, type) else MenuUtils.clickItems(label)
        fun find(): Slot = if (type != null) MenuUtils.findSlots(label, type).first() else MenuUtils.findSlots(label).first()
    }
}