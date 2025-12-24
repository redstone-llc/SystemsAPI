package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Layout
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemStackUtils.giveItem
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameContains
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

class LayoutImporter(override var name: String) : Layout {
    private suspend fun openLayoutMenu() {
        when (runCatching { MenuUtils.currentMenu().title.string == "Change Armor" }.getOrDefault(false)) {
            true -> MenuUtils.clickItems(MenuItems.back)
            false -> CommandUtils.runCommand("layout edit $name")
        }
        MenuUtils.onOpen("Layout Editor")
    }

    // TODO: Wait to receive save confirmation through chat
    private suspend fun saveLayout() = MenuUtils.clickItems(MenuItems.save)

    private suspend fun openArmorSelection(slotLabel: String) {
        openLayoutMenu()

        MenuUtils.clickItems(MenuItems.armor)
        MenuUtils.onOpen("Change Armor")

        MenuUtils.clickItems(slotLabel)
        MenuUtils.onOpen("Select an Item")
    }

    private suspend fun getArmorPiece(label: String): ItemStack? {
        openLayoutMenu()
        openArmorSelection(label)

        // TODO: look and see if the item name can also be found
        val stack = MenuUtils.findSlots(MenuItems.currentItem).first().stack
        return InputUtils.getItemFromMenu(null, stack) {
            MenuUtils.clickItems(MenuItems.currentItem)
        }
    }

    private suspend fun setArmorPiece(label: String, newStack: ItemStack) {
        val player = MC.player ?: throw IllegalStateException("Could not access the player")
        openLayoutMenu()
        openArmorSelection(label)

        val oldStack = player.inventory.getStack(26)
        newStack.giveItem(26)
        MenuUtils.clickPlayerSlot(26)
        MenuUtils.onOpen("Change Armor")
        oldStack.giveItem(26)
        MenuUtils.clickItems(MenuItems.back)
        MenuUtils.onOpen("Layout Editor")
    }

    private fun getStacks(range: IntRange): Array<ItemStack> =
        range.map { MenuUtils.currentMenu().screenHandler.inventory.getStack(it) }.toTypedArray()

    private suspend fun setStacks(range: IntRange, stacks: Array<ItemStack>) {
        val oldStack = MC.player?.inventory?.getStack(26) ?: throw IllegalStateException("Could not get old itemstack")

        Items.AIR.defaultStack.giveItem(26)
        val oldStacks = MenuUtils.currentMenu().screenHandler.slots.filter { it.id in range }
        for ((index, oldStack) in oldStacks.withIndex()) {
            if (oldStack.hasStack()) {
                MenuUtils.interactionClick(oldStack.id)
                delay(200)
                MenuUtils.interactionClick(71)
                delay(100)
            }

            val newStack = stacks.getOrNull(index)
            if (newStack != null && !newStack.isEmpty) {
                newStack.giveItem(26)
                delay(200)
                MenuUtils.interactionClick(71)
                delay(100)
                MenuUtils.interactionClick(range.first + index)
            } else Items.AIR.defaultStack.giveItem(26)
        }
        oldStack.giveItem(26)
    }


    override suspend fun getHelmet(): ItemStack? = getArmorPiece("Helmet")
    override suspend fun setHelmet(stack: ItemStack) = setArmorPiece("Helmet", stack)

    override suspend fun getChestplate(): ItemStack? = getArmorPiece("Chestplate")
    override suspend fun setChestplate(stack: ItemStack) = setArmorPiece("Chestplate", stack)

    override suspend fun getLeggings(): ItemStack? = getArmorPiece("Leggings")
    override suspend fun setLeggings(stack: ItemStack) = setArmorPiece("Leggings", stack)

    override suspend fun getBoots(): ItemStack? = getArmorPiece("Boots")
    override suspend fun setBoots(stack: ItemStack) = setArmorPiece("Boots", stack)

    override suspend fun getHotbar(): Array<ItemStack> {
        openLayoutMenu()
        return getStacks(36..43)
    }

    override suspend fun setHotbar(stacks: Array<ItemStack>) {
        if (stacks.size !in 1..8) throw IllegalArgumentException("Hotbar itemstack array length must be in 1..8")

        openLayoutMenu()
        delay(200)
        setStacks(36..43, stacks)
        saveLayout()
    }

    override suspend fun getInventory(): Array<ItemStack> {
        openLayoutMenu()
        return getStacks(0..26)
    }

    override suspend fun setInventory(stacks: Array<ItemStack>) {
        if (stacks.size !in 1..27) throw IllegalArgumentException("Inventory itemstack array length must be in 1..27")

        openLayoutMenu()
        delay(200)
        setStacks(0..26, stacks)
        saveLayout()
    }

    suspend fun exists(): Boolean = CommandUtils.getTabCompletions("layout edit").contains(name)
    fun create() = CommandUtils.runCommand("layout create $name")
    override suspend fun delete() = CommandUtils.runCommand("layout delete $name")


    private object MenuItems {
        val armor = ItemSelector(
            name = NameExact("Change Armor"),
            item = ItemExact(Items.CHAINMAIL_CHESTPLATE)
        )
        val save = ItemSelector(
            name = NameExact("Save Layout"),
            item = ItemExact(Items.CHEST)
        )
        val apply = ItemSelector(
            name = NameContains("Apply Layout"),
            item = ItemExact(Items.ENDER_CHEST)
        )
        val import = ItemSelector(
            name = NameExact("Import Layout"),
            item = ItemExact(Items.BREWING_STAND)
        )
        val back = ItemSelector(
            name = NameExact("Go Back"),
            item = ItemExact(Items.ARROW)
        )
        val currentItem = ItemSelector(
            name = NameContains("Current Item")
        )
    }
}