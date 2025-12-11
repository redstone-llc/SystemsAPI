package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.importer.ActionContainer

interface Command {
    var name: String

    suspend fun getName(): String = name
    suspend fun setName(newName: String)

    suspend fun getCommandMode(): CommandMode
    suspend fun setCommandMode(newCommandMode: CommandMode)

    suspend fun getRequiredGroupPriority(): Int
    suspend fun setRequiredGroupPriority(newPriority: Int)

    suspend fun getListed(): Boolean
    suspend fun setListed(newListed: Boolean)

    suspend fun getActionContainer(): ActionContainer

    suspend fun delete()

    enum class CommandMode {
        SELF,
        TARGETED
    }
}