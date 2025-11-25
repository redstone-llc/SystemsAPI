package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Menu
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.component.DataComponentTypes
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
        if (newSize !in 1..6) error("[Menu $title] Invalid size '$newSize'. Must be between 1 and 6.")
        openMenuEditMenu()
        MenuUtils.clickMenuSlot(MenuItems.CHANGE_MENU_SIZE)
        MenuUtils.clickMenuSlot(
            if (newSize == 1) MenuSlot(Items.BEACON, "1 Row")
            else MenuSlot(Items.BEACON, "$newSize Rows")
        )
    }

    override suspend fun getAllMenuElements(): Array<Menu.MenuElement> {
        TODO("Not yet implemented")
    }

    override suspend fun getMenuElement(index: Int): Menu.MenuElement {
        MenuUtils.clickMenuSlot(MenuItems.EDIT_MENU_ELEMENTS)
        return MenuElementImporter(index) // TODO: is this right?
    }

    override suspend fun delete() {
        CommandUtils.runCommand("menu delete $title")
    }

    object MenuItems {
        val CHANGE_TITLE = MenuSlot(Items.ANVIL, "Change Title")
        val CHANGE_MENU_SIZE = MenuSlot(Items.BEACON, "Change Menu Size")
        val EDIT_MENU_ELEMENTS = MenuSlot(Items.ENDER_CHEST, "Edit Menu Elements")
    }

    internal class MenuElementImporter(val slot: Int) : Menu.MenuElement {
        override suspend fun getItem(): ItemStack {
            MenuUtils.packetClick(MC.currentScreen as GenericContainerScreen, slot, 1)
            MenuUtils.packetClick(MC.currentScreen as GenericContainerScreen, 13, 0)
            TODO("somehow wait until mixin receives item then return that item")
        }

        override suspend fun setItem(item: ItemStack) {
            MenuUtils.packetClick(MC.currentScreen as GenericContainerScreen, slot, 1)
            TODO("generate the item and click it")
        }

        override suspend fun getActionContainer(): ActionContainer? {
            val gui = MC.currentScreen as? GenericContainerScreen ?: error("Could not cast currentScreen to GenericContainerScreen.")
            val item = gui.screenHandler.inventory.getStack(slot)
            if (item.name.string == "Empty Slot" && item.get(DataComponentTypes.LORE)?.lines?.get(0)?.string == "Click to set item!") return null // Slot must first have an item before it can have an ActionContainer
            MenuUtils.packetClick(gui, slot, 0)
            MenuUtils.onOpen("Edit Actions")
            return ActionContainer("Edit Actions")
        }
    }
}