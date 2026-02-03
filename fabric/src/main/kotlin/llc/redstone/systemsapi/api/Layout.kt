package llc.redstone.systemsapi.api

import net.minecraft.item.ItemStack

interface Layout {
    var name: String

    suspend fun getName(): String = name

    suspend fun getHelmet(): ItemStack?
    suspend fun setHelmet(stack: ItemStack): Layout

    suspend fun getChestplate(): ItemStack?
    suspend fun setChestplate(stack: ItemStack): Layout

    suspend fun getLeggings(): ItemStack?
    suspend fun setLeggings(stack: ItemStack): Layout

    suspend fun getBoots(): ItemStack?
    suspend fun setBoots(stack: ItemStack): Layout

    suspend fun getHotbar(): Array<ItemStack>
    suspend fun setHotbar(stacks: Array<ItemStack>): Layout

    suspend fun getInventory(): Array<ItemStack>
    suspend fun setInventory(stacks: Array<ItemStack>): Layout

    suspend fun delete()
}