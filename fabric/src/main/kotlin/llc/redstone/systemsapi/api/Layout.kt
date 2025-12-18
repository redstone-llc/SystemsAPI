package llc.redstone.systemsapi.api

import net.minecraft.item.ItemStack

interface Layout {
    var name: String

    suspend fun getName(): String = name

    suspend fun getHelmet(): ItemStack
    suspend fun setHelmet(stack: ItemStack)

    suspend fun getChestplate(): ItemStack
    suspend fun setChestplate(stack: ItemStack)

    suspend fun getLeggings(): ItemStack
    suspend fun setLeggings(stack: ItemStack)

    suspend fun getBoots(): ItemStack
    suspend fun setBoots(stack: ItemStack)

    suspend fun getHotbar(): Array<ItemStack>
    suspend fun setHotbar(stacks: Array<ItemStack>)

    suspend fun getInventory(): Array<ItemStack>
    suspend fun setInventory(stacks: Array<ItemStack>)

    suspend fun delete()
}