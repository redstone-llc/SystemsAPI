package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Command
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.ItemUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.ItemUtils.ItemMatch.ItemWithin
import llc.redstone.systemsapi.util.ItemUtils.ItemSelector
import llc.redstone.systemsapi.util.ItemUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Items

internal class CommandImporter(override var name: String) : Command {
    private fun isCommandEditMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Edit /${this@CommandImporter.name}") // absence of colon is intentional; hypixel weird
    }

    private fun isActionsMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Actions: /${this@CommandImporter.name}")
    }

    private suspend fun openCommandEditMenu() {
        if (isCommandEditMenuOpen()) return

        CommandUtils.runCommand("command edit ${this@CommandImporter.name}")
        MenuUtils.onOpen("Edit /${this@CommandImporter.name}")
        delay(50)
    }

    private suspend fun openActionsEditMenu() {
        if (isActionsMenuOpen()) return

        CommandUtils.runCommand("command actions ${this@CommandImporter.name}")
        MenuUtils.onOpen("Actions: /${this@CommandImporter.name}")
        delay(50)
    }

    override suspend fun setName(newName: String) {
        openCommandEditMenu()

        MenuUtils.clickItems(MenuItems.name)
        InputUtils.textInput(newName, 100L)

        this@CommandImporter.name = newName
    }

    override suspend fun getCommandMode(): Command.CommandMode {
        openCommandEditMenu()

        val mode = MenuUtils.findSlots(MenuItems.mode).first()
            .stack
            ?.getProperty("Current")
            ?.let { if (it == "Self") Command.CommandMode.SELF else Command.CommandMode.TARGETED }
            ?: throw IllegalStateException("Failed to get the command mode")
        return mode
    }

    override suspend fun setCommandMode(newCommandMode: Command.CommandMode) {
        openCommandEditMenu()

        val mode = MenuUtils.findSlots(MenuItems.mode).first()
            .stack
            ?.getProperty("Current")
            ?.let { if (it == "Self") Command.CommandMode.SELF else Command.CommandMode.TARGETED }
            ?: throw IllegalStateException("Failed to set the command mode to ${newCommandMode.name}")
        if (mode == newCommandMode) return

        MenuUtils.clickItems(MenuItems.mode)
    }

    override suspend fun getRequiredGroupPriority(): Int {
        openCommandEditMenu()

        val priority = MenuUtils.findSlots(MenuItems.requiredGroupPriority).first()
            .stack
            ?.getProperty("Current")
            ?.toIntOrNull()
            ?: throw IllegalStateException("Failed to get the required group priority")

        return priority
    }

    override suspend fun setRequiredGroupPriority(newPriority: Int) {
        openCommandEditMenu()

        val priority = MenuUtils.findSlots(MenuItems.requiredGroupPriority).first()
            .stack
            ?.getProperty("Current")
            ?.toIntOrNull()
            ?: throw IllegalStateException("Failed to set the required group priority to $newPriority.")
        if (priority == newPriority) return

        MenuUtils.clickItems(MenuItems.requiredGroupPriority)
        InputUtils.textInput(newPriority.toString(), 100L)
    }

    override suspend fun getListed(): Boolean {
        openCommandEditMenu()

        val listed = MenuUtils.findSlots(MenuItems.listed).first()
            .stack
            ?.item
            ?.let { item -> item == Items.LIME_DYE }
            ?: throw IllegalStateException("Failed to get the listed value")

        return listed
    }

    override suspend fun setListed(newListed: Boolean) {
        openCommandEditMenu()

        val listed = MenuUtils.findSlots(MenuItems.listed).first()
            .stack
            ?.item
            ?.let { item -> item == Items.LIME_DYE }
            ?: throw IllegalStateException("Failed to set the listed value to $newListed.")
        if (listed == newListed) return

        MenuUtils.clickItems(MenuItems.mode)
    }

    override suspend fun getActionContainer(): ActionContainer {
        openActionsEditMenu()
        return ActionContainer("Actions: /${this@CommandImporter.name}")
    }

    suspend fun exists(): Boolean = getTabCompletions("command edit").contains(this@CommandImporter.name)
    suspend fun create() {
        if (this.exists()) throw IllegalStateException("Command already exists")
        CommandUtils.runCommand("command create ${this@CommandImporter.name}")
    }

    override suspend fun delete() = CommandUtils.runCommand("command delete ${this@CommandImporter.name}")


    private object MenuItems {
        val name = ItemSelector(
            name = NameExact("Rename Command"),
            item = ItemExact(Items.ANVIL)
        )
        val mode = ItemSelector(
            name = NameExact("Toggle Command Mode"),
            item = ItemWithin(listOf(Items.LIGHT_GRAY_DYE, Items.LIME_DYE))
        )
        val requiredGroupPriority = ItemSelector(
            name = NameExact("Required Group Priority"),
            item = ItemExact(Items.FILLED_MAP)
        )
        val listed = ItemSelector(
            name = NameExact("Listed"),
            item = ItemWithin(listOf(Items.LIGHT_GRAY_DYE, Items.LIME_DYE))
        )
    }
}