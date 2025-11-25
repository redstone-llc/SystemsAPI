package llc.redstone.systemapi.api

import llc.redstone.systemapi.data.Action
import llc.redstone.systemapi.data.ItemStack
import net.minecraft.item.Item

interface Function {
    var name: String
    suspend fun getName(): String = name
    suspend fun setName(newName: String)

    suspend fun createIfNotExists()

    suspend fun getDescription(): String
    suspend fun setDescription(newDescription: String)

    suspend fun getIcon(): Item
    suspend fun setIcon(newIcon: ItemStack)

    suspend fun getAutomaticExecution(): Int
    suspend fun setAutomaticExecution(newAutomaticExecution: Int)

    suspend fun getActions(): List<Action>
    suspend fun addActions(newActions: List<Action>)

    suspend fun delete()
}