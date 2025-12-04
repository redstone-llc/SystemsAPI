package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.MenuUtils.Target
import llc.redstone.systemsapi.api.Function
import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.data.ItemStack
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.ItemUtils
import llc.redstone.systemsapi.util.ItemUtils.loreLine
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Item
import net.minecraft.item.Items

internal class FunctionImporter(override var name: String) : Function {
    private fun isFunctionEditMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Edit: $name")
    }

    private fun isActionsMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Actions: $name")
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
        if (newName.length !in 1..50) error(("[Function $name] Invalid title '$newName'. Must be between 1 and 50 characters long."))
        openFunctionEditMenu()

        MenuUtils.clickMenuSlot(MenuItems.RENAME_FUNCTION)
        TextUtils.input(newName, 100L)

        name = newName
    }

    override suspend fun createIfNotExists(): Boolean {
        if (exists()) return false

        CommandUtils.runCommand("function create $name") // TODO: delay until we receive confirmation that the function is actually created
        return true
    }

    override suspend fun getDescription(): String {
        openFunctionEditMenu()

        val description = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.SET_DESCRIPTION) }
            ?.stack
            ?.loreLine(2, false, LoreFilters.RENAME_LORE_FILTER)
            ?: error("[Function $name] Failed to get description.")

        return description
    }

    override suspend fun setDescription(newDescription: String) {
        if (newDescription.length !in 1..120) error(("[Function $name] Invalid title '$newDescription'. Must be between 1 and 120 characters long."))
        openFunctionEditMenu()

        val description = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.SET_DESCRIPTION) }
            ?.stack
            ?.loreLine(2, false, LoreFilters.RENAME_LORE_FILTER)
            ?: error("[Function $name] Failed to set description to '$newDescription'.")
        if (description == newDescription) return

        MenuUtils.clickMenuSlot(MenuItems.SET_DESCRIPTION)
        TextUtils.input(newDescription, 100L)
    }

    override suspend fun getIcon(): Item {
        openFunctionEditMenu()
        val gui = MC.currentScreen as? GenericContainerScreen ?: error("[Function $name] getIcon: Failed to cast currentScreen as GenericContainerScreen.")
        val slot = MenuUtils.findSlot(gui, MenuItems.EDIT_ICON) ?: error("[Function $name] getIcon: Failed to find EDIT_ICON.")
        return slot.stack.item
    }

    override suspend fun setIcon(newIcon: ItemStack) {
        openFunctionEditMenu()

        MenuUtils.clickMenuSlot(MenuItems.EDIT_ICON)
        MenuUtils.onOpen("Select an Item")

        val itemstack = ItemUtils.createFromNBT(newIcon.nbt ?: return)
        ItemUtils.placeInventory(itemstack, 26)
        MenuUtils.clickPlayerSlot(26)
        delay(50)
    }

    override suspend fun getAutomaticExecution(): Int {
        openFunctionEditMenu()

        val ticks = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.AUTOMATIC_EXECUTION) }
            ?.stack
            ?.loreLine(5, false)
            ?.split(" ")
            ?.getOrNull(1)
            ?.let { value -> if (value == "Disabled") 0 else value.toIntOrNull() }
            ?: error("[Function $name] Failed to get automatic execution.")

        return ticks
    }

    override suspend fun setAutomaticExecution(newAutomaticExecution: Int) {
        if (newAutomaticExecution !in 0..18000) error(("[Function $name] Invalid automatic execution ticks '$newAutomaticExecution'. Must be between 1 and 18000."))
        openFunctionEditMenu()

        val ticks = (MC.currentScreen as? GenericContainerScreen)
            ?.let { gui -> MenuUtils.findSlot(gui, MenuItems.AUTOMATIC_EXECUTION) }
            ?.stack
            ?.loreLine(5, false)
            ?.split(" ")
            ?.getOrNull(1)
            ?.let { value -> if (value == "Disabled") 0 else value.toIntOrNull() }
            ?: error("[Function $name] Failed to set automatic execution to '$newAutomaticExecution'.")
        if (ticks == newAutomaticExecution) return

        MenuUtils.clickMenuSlot(MenuItems.AUTOMATIC_EXECUTION)
        TextUtils.input(newAutomaticExecution.toString(), 100L)
    }

    override suspend fun getActionContainer(): ActionContainer {
        openActionsEditMenu()
        return ActionContainer("Actions: $name")
    }

    override suspend fun exists(): Boolean = getTabCompletions("function edit").contains(name)

    override suspend fun delete() {
        CommandUtils.runCommand("function delete $name")
    }

    object MenuItems {
        val RENAME_FUNCTION = MenuSlot(Items.ANVIL, "Rename Function")
        val SET_DESCRIPTION = MenuSlot(Items.BOOK, "Edit Description")
        val EDIT_ICON = MenuSlot(null, "Edit Icon")
        val AUTOMATIC_EXECUTION = MenuSlot(Items.COMPARATOR, "Automatic Execution")
        val BACK = MenuSlot(Items.ARROW, "Go Back")
    }

    object LoreFilters {
        val RENAME_LORE_FILTER: (String) -> Boolean = { loreLine ->
            !loreLine.contains("Click to rename!")
        }
    }
}