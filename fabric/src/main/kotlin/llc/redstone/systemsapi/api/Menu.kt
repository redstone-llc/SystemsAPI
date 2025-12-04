package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer
import net.minecraft.item.ItemStack

interface Menu {
    var title: String

    suspend fun createIfNotExists(): Boolean

    suspend fun getTitle(): String = title
    suspend fun setTitle(newTitle: String)

    suspend fun getMenuSize(): Int
    suspend fun changeMenuSize(newSize: Int)

    suspend fun getMenuElement(index: Int): MenuElement
    suspend fun getAllMenuElements(): Array<MenuElement>

    suspend fun exists(): Boolean
    suspend fun delete()

    interface MenuElement {
        suspend fun getItem(): ItemStack
        suspend fun setItem(item: ItemStack)

        suspend fun getActionContainer(): ActionContainer?
    }
}