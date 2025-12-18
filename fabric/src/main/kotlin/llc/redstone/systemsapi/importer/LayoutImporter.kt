package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Layout
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.ItemUtils.giveItem
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

class LayoutImporter(override var name: String) : Layout {
    private suspend fun openLayoutMenu() {
        if (!this.exists()) throw IllegalStateException("Layout does not exist")

        val screen = MC.currentScreen as? GenericContainerScreen

        when (screen?.title?.string) {
            "Change Armor" -> MenuUtils.clickMenuSlot(MenuItems.GO_BACK)
            else -> CommandUtils.runCommand("layout edit $name")
        }

        MenuUtils.onOpen("Layout Editor")
    }

    private suspend fun saveLayout() = MenuUtils.clickMenuSlot(MenuItems.SAVE_LAYOUT)

    private suspend fun openArmorSelection(slotLabel: String) {
        MenuUtils.clickMenuSlot(MenuItems.CHANGE_ARMOR)
        MenuUtils.onOpen("Change Armor")

        MenuUtils.clickMenuSlot(MenuSlot(null, slotLabel))
        MenuUtils.onOpen("Select an Item")
    }

    private suspend fun getArmorPiece(label: String): ItemStack {
        openLayoutMenu()
        openArmorSelection(label)

        //TODO look and see if the item name can also be found
        val stack = MenuUtils.findSlot(MenuSlot(slot = 13))?.stack
            ?: throw IllegalStateException("Could not find armor piece in layout '$name' for slot '$label'")
        return MenuUtils.getItemFromMenu(null, stack) {
            MenuUtils.packetClick(13, 0)
        }
    }

    private suspend fun setArmorPiece(label: String, newStack: ItemStack) {
        val player = MC.player ?: throw IllegalStateException("Could not access the player")
        openLayoutMenu()
        openArmorSelection(label)

        val oldStack = player.inventory.getStack(26)
        newStack.giveItem(26)
        MenuUtils.clickPlayerSlot(26)
        oldStack.giveItem(26)
    }

    private fun GenericContainerScreen.getStacks(range: IntRange): Array<ItemStack> =
        range.map { screenHandler.inventory.getStack(it) }.toTypedArray()

    private fun GenericContainerScreen.setStacks(stacks: Array<ItemStack>, startIndex: Int = 0) {
        stacks.forEachIndexed { index, stack ->
            val currentIndex = startIndex + index
            if (stack == screenHandler.inventory.getStack(currentIndex)) return@forEachIndexed
            screenHandler.inventory.setStack(currentIndex, stack)
        }
    }


    override suspend fun getHelmet(): ItemStack = getArmorPiece("Helmet")
    override suspend fun setHelmet(stack: ItemStack) = setArmorPiece("Helmet", stack)

    override suspend fun getChestplate(): ItemStack = getArmorPiece("Chestplate")
    override suspend fun setChestplate(stack: ItemStack) = setArmorPiece("Chestplate", stack)

    override suspend fun getLeggings(): ItemStack = getArmorPiece("Leggings")
    override suspend fun setLeggings(stack: ItemStack) = setArmorPiece("Leggings", stack)

    override suspend fun getBoots(): ItemStack = getArmorPiece("Boots")
    override suspend fun setBoots(stack: ItemStack) = setArmorPiece("Boots", stack)

    override suspend fun getHotbar(): Array<ItemStack> {
        openLayoutMenu()
        return MenuUtils.currentMenu().getStacks(45..53)
    }

    override suspend fun setHotbar(stacks: Array<ItemStack>) {
        if (stacks.size !in 1..8) throw IllegalArgumentException("Hotbar itemstack array length must be in 1..8")

        openLayoutMenu()
        MenuUtils.currentMenu().setStacks(stacks, 36)
        saveLayout()
    }

    override suspend fun getInventory(): Array<ItemStack> {
        openLayoutMenu()
        return MenuUtils.currentMenu().getStacks(0..26)
    }

    override suspend fun setInventory(stacks: Array<ItemStack>) {
        if (stacks.size !in 1..27) throw IllegalArgumentException("Inventory itemstack array length must be in 1..27")

        openLayoutMenu()
        MenuUtils.currentMenu().setStacks(stacks)
        saveLayout()
    }

    suspend fun exists(): Boolean = CommandUtils.getTabCompletions("layout edit").contains(name)
    fun create() = CommandUtils.runCommand("layout create $name")
    override suspend fun delete() = CommandUtils.runCommand("layout delete $name")

    private object MenuItems {
        val CHANGE_ARMOR = MenuSlot(Items.CHAINMAIL_CHESTPLATE, "Change Armor")
        val SAVE_LAYOUT = MenuSlot(Items.CHEST, "Save Layout")
        val APPLY_LAYOUT = MenuSlot(Items.ENDER_CHEST, "Apply Layout")
        val IMPORT_LAYOUT = MenuSlot(Items.BREWING_STAND, "Import Layout")
        val GO_BACK = MenuSlot(Items.ARROW, "Go Back")
    }
}