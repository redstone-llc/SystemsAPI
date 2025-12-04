package llc.redstone.systemsapi.importer

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Menu
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.ItemUtils.giveItem
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
        if (exists()) return false

        CommandUtils.runCommand("menu create $title") // TODO: delay until we receive confirmation that the function is actually created
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
        MenuUtils.clickMenuSlot(MenuItems.EDIT_MENU_ELEMENTS)
        val gui = MC.currentScreen as? GenericContainerScreen ?: error("[Menu $title] getAllMenuElements: Could not cast currentScreen as GenericContainerScreen.")
        val numSlots = 9 * gui.screenHandler.rows
        return Array(numSlots) { index -> MenuElementImporter(index) }
    }

    override suspend fun getMenuElement(index: Int): Menu.MenuElement {
        MenuUtils.clickMenuSlot(MenuItems.EDIT_MENU_ELEMENTS)
        val gui = MC.currentScreen as? GenericContainerScreen ?: error("[Menu $title] getMenuElement: Could not cast currentScreen as GenericContainerScreen.")
        val numSlots = 9 * gui.screenHandler.rows
        if (index !in 0..<numSlots) error("[Menu $title] getMenuElement: Invalid index '$index'.")
        return MenuElementImporter(index)
    }

    override suspend fun exists(): Boolean = getTabCompletions("menu edit").contains(title)

    override suspend fun delete() = CommandUtils.runCommand("menu delete $title")

    object MenuItems {
        val CHANGE_TITLE = MenuSlot(Items.ANVIL, "Change Title")
        val CHANGE_MENU_SIZE = MenuSlot(Items.BEACON, "Change Menu Size")
        val EDIT_MENU_ELEMENTS = MenuSlot(Items.ENDER_CHEST, "Edit Menu Elements")
    }

    internal class MenuElementImporter(val slot: Int) : Menu.MenuElement {
        companion object {
            private var pending: CompletableDeferred<ItemStack>? = null

            fun onItemReceived(stack: ItemStack) {
                pending?.let { current ->
                    pending = null
                    current.complete(stack)
                }
            }
        }

        override suspend fun getItem(): ItemStack {
            val gui = MC.currentScreen as? GenericContainerScreen ?: error("[getItem] Could not cast currentScreen as GenericContainerScreen.")
            MenuUtils.packetClick(gui, slot, 1)

            val deferred = CompletableDeferred<ItemStack>()
            pending?.cancel()
            pending = deferred

            try {
                MenuUtils.packetClick(gui, 13, 0)
                return withTimeout(1_000) { deferred.await() }
            } catch (e: TimeoutCancellationException) {
                if (pending === deferred) pending = null
                error("[getItem] Timed out waiting for item.")
            } finally {
                if (pending === deferred) pending = null
            }
        }

        override suspend fun setItem(item: ItemStack) {
            val gui = MC.currentScreen as? GenericContainerScreen ?: error("[setItem] Could not cast currentScreen as GenericContainerScreen.")
            val player = MC.player ?: error("[setItem] Could not get the player")
            MenuUtils.packetClick(gui, slot, 1)
            val oldStack = player.inventory.getStack(0)
            item.giveItem(0)
            MenuUtils.interactionClick(gui, 0)
            oldStack.giveItem(0)
        }

        override suspend fun getActionContainer(): ActionContainer? {
            val gui = MC.currentScreen as? GenericContainerScreen ?: error("[MenuElement] getActionContainer: Could not cast currentScreen as GenericContainerScreen.")
            val item = gui.screenHandler.inventory.getStack(slot)
            if (item.name.string == "Empty Slot" && item.get(DataComponentTypes.LORE)?.lines?.get(0)?.string == "Click to set item!") return null // Slot must first have an item before it can have an ActionContainer
            MenuUtils.packetClick(gui, slot, 0)
            MenuUtils.onOpen("Edit Actions")
            return ActionContainer("Edit Actions")
        }
    }
}