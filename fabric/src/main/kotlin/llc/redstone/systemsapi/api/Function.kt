package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer
import net.minecraft.item.Item

interface Function {
    var name: String
    suspend fun getName(): String = name
    suspend fun setName(newName: String): Function

    suspend fun getDescription(): String
    suspend fun setDescription(newDescription: String): Function

    suspend fun getIcon(): Item
    suspend fun setIcon(newIcon: Item): Function

    suspend fun getAutomaticExecution(): Int
    suspend fun setAutomaticExecution(newAutomaticExecution: Int): Function

    suspend fun getActionContainer(): ActionContainer

    suspend fun delete()
}