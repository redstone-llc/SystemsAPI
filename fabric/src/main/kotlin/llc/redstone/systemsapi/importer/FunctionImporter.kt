package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.api.Function
import llc.redstone.systemsapi.data.ItemStack
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemConverterUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.ItemStackUtils.giveItem
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.MenuUtils.Target
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot

internal class FunctionImporter(override var name: String) : Function {
    private fun isFunctionEditMenuOpen(): Boolean = try {
        MenuUtils.currentMenu().title.string.contains("Edit: $name")
    } catch (_: Exception) {
        false
    }

    private fun isActionsMenuOpen(): Boolean = try {
        MenuUtils.currentMenu().title.string.contains("Actions: $name")
    } catch (_: Exception) {
        false
    }

    private suspend fun openFunctionEditMenu(): Boolean {
        if (!isFunctionEditMenuOpen()) {
            CommandUtils.runCommand("functions")
            MenuUtils.onOpen("Functions")

            if (!MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, name), 1))) return false

            MenuUtils.onOpen("Edit: $name")
            delay(50)
        }
        return true
    }

    private suspend fun openActionsEditMenu(): Boolean {
        if (!isActionsMenuOpen()) {
            CommandUtils.runCommand("function edit $name")
            MenuUtils.onOpen("Actions: $name")
            delay(50)
        }
        return true
    }

    override suspend fun setName(newName: String) {
        if (newName.length !in 1..50) throw IllegalArgumentException("Title length must be in range 1..50")
        openFunctionEditMenu()

        MenuItems.RENAME_FUNCTION.click()
        InputUtils.textInput(newName, 100L)

        name = newName
    }

    override suspend fun getDescription(): String {
        openFunctionEditMenu()

        return InputUtils.getPreviousInput { MenuItems.EDIT_DESCRIPTION.click() }
    }

    override suspend fun setDescription(newDescription: String) {
        if (newDescription.length !in 1..120) throw IllegalArgumentException("Description length must be in range 1..120")
        openFunctionEditMenu()

        MenuItems.EDIT_DESCRIPTION.click()
        InputUtils.textInput(newDescription, 100L)
    }

    override suspend fun getIcon(): Item {
        openFunctionEditMenu()
        val slot = MenuItems.EDIT_ICON.find()
        return slot.stack.item
    }

    override suspend fun setIcon(newIcon: ItemStack) {
        openFunctionEditMenu()

        MenuItems.EDIT_ICON.click()
        MenuUtils.onOpen("Select an Item")

        val itemStack = ItemConverterUtils.createFromNBT(newIcon.nbt ?: return)
        itemStack.giveItem(26)
        MenuUtils.clickPlayerSlot(26)
        delay(50)
    }

    override suspend fun getAutomaticExecution(): Int {
        openFunctionEditMenu()

        val ticks = MenuItems.AUTOMATIC_EXECUTION.find()
            .stack
            ?.getProperty("Current")
            ?.let { if (it == "Disabled") 0 else it.toIntOrNull() }
            ?: throw IllegalStateException("Failed to find automatic execution ticks")

        return ticks
    }

    override suspend fun setAutomaticExecution(newAutomaticExecution: Int) {
        if (newAutomaticExecution !in 0..18000) throw IllegalArgumentException("Automatic execution ticks must be in range 1..18000")
        openFunctionEditMenu()

        val ticks = MenuItems.AUTOMATIC_EXECUTION.find()
            .stack
            ?.getProperty("Current")
            ?.let { if (it == "Disabled") 0 else it.toIntOrNull() }
            ?: throw IllegalStateException("Failed to set automatic execution")
        if (ticks == newAutomaticExecution) return

        MenuItems.AUTOMATIC_EXECUTION.click()
        InputUtils.textInput(newAutomaticExecution.toString(), 100L)
    }

    override suspend fun getActionContainer(): ActionContainer {
        openActionsEditMenu()
        return ActionContainer("Actions: $name")
    }

    suspend fun exists(): Boolean = getTabCompletions("function edit").contains(name)
    fun create() = CommandUtils.runCommand("function create $name")
    override suspend fun delete() = CommandUtils.runCommand("function delete $name")

    private enum class MenuItems(
        val label: String,
        val type: Item? = null
    ) {
        RENAME_FUNCTION("Rename Function", Items.ANVIL),
        EDIT_DESCRIPTION("Edit Description", Items.BOOK),
        EDIT_ICON("Edit Icon"),
        AUTOMATIC_EXECUTION("Automatic Execution", Items.COMPARATOR),
        BACK("Go Back", Items.ARROW);

        fun click() = if (type != null) MenuUtils.clickItems(label, type) else MenuUtils.clickItems(label)
        fun find(): Slot = if (type != null) MenuUtils.findSlots(label, type).first() else MenuUtils.findSlots(label).first()
    }
}