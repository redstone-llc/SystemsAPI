package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Menu
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemStackUtils.giveItem
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot

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

    override suspend fun setTitle(newTitle: String) {
        if (newTitle.length !in 1..32) throw IllegalArgumentException("Title length must be in range 1..32")
        openMenuEditMenu()
        MenuItems.CHANGE_TITLE.click()
        InputUtils.textInput(newTitle, 100L)
        title = newTitle
    }

    override suspend fun getMenuSize(): Int {
        openMenuEditMenu()
        MenuItems.CHANGE_MENU_SIZE.click()
        MenuUtils.onOpen("Change Menu Size")
        val stack = (MC.currentScreen as GenericContainerScreen).screenHandler.inventory.first { stack -> stack.hasGlint() || stack.hasEnchantments() }
        return Regex("""\d+""").find(stack.name.string)?.value?.toIntOrNull() ?: throw IllegalStateException("[Menu $title] Couldn't find menu size.")
    }

    override suspend fun changeMenuSize(newSize: Int) {
        if (newSize !in 1..6) throw IllegalArgumentException("New size must be in 1..6")
        openMenuEditMenu()
        MenuItems.CHANGE_MENU_SIZE.click()
        MenuUtils.onOpen("Change Menu Size")
        MenuUtils.clickMenuSlot(
            if (newSize == 1) MenuSlot(Items.BEACON, "1 Row")
            else MenuSlot(Items.BEACON, "$newSize Rows")
        )
    }

    override suspend fun getAllMenuElements(): Array<Menu.MenuElement> {
        openMenuEditMenu()
        MenuItems.EDIT_MENU_ELEMENTS.click()
        MenuUtils.onOpen("Edit Elements: $title")
        val gui = MenuUtils.currentMenu()
        val numSlots = 9 * gui.screenHandler.rows
        return Array(numSlots) { index -> MenuElementImporter(index, title) }
    }

    override suspend fun getMenuElement(index: Int): Menu.MenuElement {
        openMenuEditMenu()
        MenuItems.EDIT_MENU_ELEMENTS.click()
        MenuUtils.onOpen("Edit Elements: $title")
        val gui = MenuUtils.currentMenu()
        val numSlots = 9 * gui.screenHandler.rows
        if (index !in 0..<numSlots) throw IllegalArgumentException("Index must be in 0..${numSlots-1}")
        return MenuElementImporter(index, title)
    }

    suspend fun exists(): Boolean = CommandUtils.getTabCompletions("menus edit").contains(title)
    fun create() = CommandUtils.runCommand("menus create $title")
    override suspend fun delete() = CommandUtils.runCommand("menus delete $title")
    

    private enum class MenuItems(
        val label: String,
        val type: Item? = null
    ) {
        CHANGE_TITLE("Change Title", Items.ANVIL),
        CHANGE_MENU_SIZE("Change Menu Size", Items.BEACON),
        EDIT_MENU_ELEMENTS("Edit Menu Elements", Items.ENDER_CHEST);

        suspend fun click() = if (type != null) MenuUtils.clickItems(label, type) else MenuUtils.clickItems(label)
        fun find(): Slot = if (type != null) MenuUtils.findSlots(label, type).first() else MenuUtils.findSlots(label).first()
    }

    internal class MenuElementImporter(val slot: Int, val title: String) : Menu.MenuElement {
        override suspend fun getItem(): ItemStack {
            MenuUtils.packetClick(slot, 1)
            MenuUtils.onOpen("Select an Item")

            val stack = MenuItems.CURRENT_ITEM.find().stack
            return InputUtils.getItemFromMenu(null, stack) {
                MenuItems.CURRENT_ITEM.click()
            }
        }

        override suspend fun setItem(item: ItemStack) {
            val player = MC.player ?: throw IllegalStateException("Could not access the player")
            MenuUtils.onOpen("Edit Elements: $title")

            MenuUtils.packetClick(slot, 1)
            MenuUtils.onOpen("Select an Item")

            val oldStack = player.inventory.getStack(26)
            item.giveItem(26)
            MenuUtils.clickPlayerSlot(26)
            oldStack.giveItem(26)
        }

        override suspend fun getActionContainer(): ActionContainer? {
            val gui = MenuUtils.currentMenu()
            val item = gui.screenHandler.inventory.getStack(slot)
            if (item.name.string == "Empty Slot" && item.get(DataComponentTypes.LORE)?.lines?.get(0)?.string == "Click to set item!") return null // Slot must first have an item before it can have an ActionContainer

            MenuUtils.onOpen("Edit Elements: $title")

            MenuUtils.packetClick(slot)
            MenuUtils.onOpen("Edit Actions")

            return ActionContainer("Edit Actions")
        }

        private enum class MenuItems(
            val label: String,
            val type: Item? = null
        ) {
            CURRENT_ITEM("Current Item");

            suspend fun click() = if (type != null) MenuUtils.clickItems(label, type) else MenuUtils.clickItems(label)
            fun find(): Slot = if (type != null) MenuUtils.findSlots(label, type).first() else MenuUtils.findSlots(label).first()
        }
    }
}