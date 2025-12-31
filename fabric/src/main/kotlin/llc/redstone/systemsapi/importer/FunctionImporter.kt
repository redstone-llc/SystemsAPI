package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.api.Function
import llc.redstone.systemsapi.data.ItemStack
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.ItemStackUtils.giveItem
import llc.redstone.systemsapi.util.ItemUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.item.Item
import net.minecraft.item.Items

internal class FunctionImporter(override var name: String) : Function {
    private fun isFunctionEditMenuOpen(): Boolean = try {
        MenuUtils.currentMenu().title.string.contains("Edit: ${this@FunctionImporter.name}")
    } catch (_: Exception) {
        false
    }

    private fun isActionsMenuOpen(): Boolean = try {
        MenuUtils.currentMenu().title.string.contains("Actions: ${this@FunctionImporter.name}")
    } catch (_: Exception) {
        false
    }

    private suspend fun openFunctionEditMenu(): Boolean {
        if (!isFunctionEditMenuOpen()) {
            CommandUtils.runCommand("functions")
            MenuUtils.onOpen("Functions")

            MenuUtils.clickItems(this@FunctionImporter.name, paginated = true)
            MenuUtils.onOpen("Edit: ${this@FunctionImporter.name}")
            delay(50)
        }
        return true
    }

    private suspend fun openActionsEditMenu(): Boolean {
        if (!isActionsMenuOpen()) {
            CommandUtils.runCommand("function edit ${this@FunctionImporter.name}")
            MenuUtils.onOpen("Actions: ${this@FunctionImporter.name}")
            delay(50)
        }
        return true
    }

    override suspend fun setName(newName: String) {
        if (newName.length !in 1..50) throw IllegalArgumentException("Title length must be in range 1..50")
        openFunctionEditMenu()

        MenuUtils.clickItems(MenuItems.name)
        MenuUtils.clickItems(MenuItems.name)
        InputUtils.textInput(newName)

        this@FunctionImporter.name = newName
    }

    override suspend fun getDescription(): String {
        openFunctionEditMenu()

        return InputUtils.getPreviousInput { MenuUtils.clickItems(MenuItems.description) }
    }

    override suspend fun setDescription(newDescription: String) {
        if (newDescription.length !in 1..120) throw IllegalArgumentException("Description length must be in range 1..120")
        openFunctionEditMenu()

        MenuUtils.clickItems(MenuItems.description)
        InputUtils.textInput(newDescription)
    }

    override suspend fun getIcon(): Item {
        openFunctionEditMenu()
        val slot = MenuUtils.findSlots(MenuItems.icon).first()
        return slot.stack.item
    }

    override suspend fun setIcon(newIcon: ItemStack) {
        openFunctionEditMenu()

        MenuUtils.clickItems(MenuItems.icon)
        MenuUtils.onOpen("Select an Item")

        val itemStack = ItemUtils.createFromNBT(newIcon.nbt ?: return)
        itemStack.giveItem(26)
        MenuUtils.clickPlayerSlot(26)
        delay(50)
    }

    override suspend fun getAutomaticExecution(): Int {
        openFunctionEditMenu()

        val ticks = MenuUtils.findSlots(MenuItems.automaticExecution).first()
            .stack
            ?.getProperty("Current")
            ?.let { if (it == "Disabled") 0 else it.toIntOrNull() }
            ?: throw IllegalStateException("Failed to find automatic execution ticks")

        return ticks
    }

    override suspend fun setAutomaticExecution(newAutomaticExecution: Int) {
        if (newAutomaticExecution !in 0..18000) throw IllegalArgumentException("Automatic execution ticks must be in range 1..18000")
        openFunctionEditMenu()

        val ticks = MenuUtils.findSlots(MenuItems.automaticExecution).first()
            .stack
            ?.getProperty("Current")
            ?.let { if (it == "Disabled") 0 else it.toIntOrNull() }
            ?: throw IllegalStateException("Failed to set automatic execution")
        if (ticks == newAutomaticExecution) return

        MenuUtils.clickItems(MenuItems.automaticExecution)
        InputUtils.textInput(newAutomaticExecution.toString())
    }

    override suspend fun getActionContainer(): ActionContainer {
        openActionsEditMenu()
        return ActionContainer("Actions: ${this@FunctionImporter.name}")
    }

    suspend fun exists(): Boolean = getTabCompletions("function edit").contains(this@FunctionImporter.name)
    fun create() = CommandUtils.runCommand("function create ${this@FunctionImporter.name}")
    override suspend fun delete() = CommandUtils.runCommand("function delete ${this@FunctionImporter.name}")


    private object MenuItems {
        val name = ItemSelector(
            name = NameExact("Rename Function"),
            item = ItemExact(Items.ANVIL)
        )
        val description = ItemSelector(
            name = NameExact("Edit Description"),
            item = ItemExact(Items.BOOK)
        )
        val icon = ItemSelector(
            name = NameExact("Edit Icon")
        )
        val automaticExecution = ItemSelector(
            name = NameExact("Automatic Execution"),
            item = ItemExact(Items.COMPARATOR)
        )
    }
}