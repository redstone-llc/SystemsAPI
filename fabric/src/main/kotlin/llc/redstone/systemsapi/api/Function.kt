package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.data.ItemStack
import llc.redstone.systemsapi.importer.ActionContainer
import net.minecraft.item.Item

interface Function {
    var name: String
    suspend fun getName(): String = name
    suspend fun setName(newName: String)

    suspend fun getDescription(): String
    suspend fun setDescription(newDescription: String)

    suspend fun getIcon(): Item
    suspend fun setIcon(newIcon: ItemStack)

    suspend fun getAutomaticExecution(): Int
    suspend fun setAutomaticExecution(newAutomaticExecution: Int)

    suspend fun getActionContainer(): ActionContainer

    suspend fun delete()
}