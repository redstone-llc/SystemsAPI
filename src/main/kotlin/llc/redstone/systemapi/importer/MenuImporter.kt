package llc.redstone.systemapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemapi.SystemAPI.MC
import llc.redstone.systemapi.api.Menu
import llc.redstone.systemapi.util.CommandUtils
import llc.redstone.systemapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemapi.util.MenuUtils
import llc.redstone.systemapi.util.MenuUtils.MenuSlot
import llc.redstone.systemapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

internal class MenuImporter(override var title: String) : Menu {
    private fun isMenuEditMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Edit Menu: $title")
    }

    private suspend fun openMenuEditMenu() {
        if (isMenuEditMenuOpen()) return
        CommandUtils.runCommand("menu edit $title")
        MenuUtils.onOpen("Edit Menu: $title")
        delay(50)
    }

    override suspend fun createIfNotExists(): Boolean {
        val menus = getTabCompletions("menu edit")
        if (menus.contains(title)) return false

        CommandUtils.runCommand("menu create $title")
        return true
    }

    override suspend fun setTitle(newTitle: String) {
        if (newTitle.length !in 1..32) error(("[Menu $title] Invalid title '$newTitle'. must be between 1 and 32 characters long."))
        openMenuEditMenu()
        MenuUtils.clickMenuSlot(MenuItems.CHANGE_TITLE)
        TextUtils.input(newTitle, 100L)
        title = newTitle
    }

    override suspend fun getMenuSize(): Int {
        openMenuEditMenu()
        MenuUtils.clickMenuSlot(MenuItems.CHANGE_MENU_SIZE)
        val stack = (MC.currentScreen as GenericContainerScreen).screenHandler.inventory.first { stack -> stack.hasGlint() || stack.hasEnchantments() }
        return Regex("""\d+""").find(stack.name.string)?.value?.toIntOrNull() ?: error("[Menu $title] Couldn't find menu size.")
    }

    override suspend fun changeMenuSize(newSize: Int) {
        if (newSize !in 1..6) error("[Menu \$title] Invalid size '$newSize'. Must be between 1 and 6.")
        openMenuEditMenu()
        MenuUtils.clickMenuSlot(MenuItems.CHANGE_MENU_SIZE)
        MenuUtils.clickMenuSlot(
            if (newSize == 1) MenuSlot(Items.BEACON, "1 Row")
            else MenuSlot(Items.BEACON, "$newSize Rows")
        )
    }

    override suspend fun getMenuElements(): Array<ItemStack> {
        TODO("Not yet implemented")
    }

    override suspend fun setMenuElements(newMenuElements: Array<ItemStack>) {
        TODO("Not yet implemented")
    }

    override suspend fun delete() {
        CommandUtils.runCommand("menu delete $title")
    }

    object MenuItems {
        val CHANGE_TITLE = MenuSlot(Items.ANVIL, "Change Title")
        val CHANGE_MENU_SIZE = MenuSlot(Items.BEACON, "Change Menu Size")
        val EDIT_MENU_ELEMENTS = MenuSlot(Items.ENDER_CHEST, "Edit Menu Elements")
    }
}