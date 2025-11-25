package llc.redstone.systemapi.api

import llc.redstone.systemapi.data.Action
import llc.redstone.systemapi.data.ItemStack

interface CustomMenu {
    var name: String

    suspend fun getName(): String = name
    suspend fun setName(newName: String)

    suspend fun getMenuSize(): Int
    suspend fun setMenuSize(newSize: Int)

    suspend fun getMenuElements(): List<Pair<net.minecraft.item.ItemStack, List<Action>>?>
    suspend fun setMenuElements(newMenuElements: List<Pair<ItemStack, List<Action>>?>)

    suspend fun delete()
}