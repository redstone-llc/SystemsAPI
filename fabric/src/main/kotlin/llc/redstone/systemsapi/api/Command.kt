package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer

interface Command {
    var name: String

    suspend fun getName(): String = name
    suspend fun setName(newName: String): Command

    suspend fun getCommandMode(): CommandMode
    suspend fun setCommandMode(newCommandMode: CommandMode): Command

    suspend fun getRequiredGroupPriority(): Int
    suspend fun setRequiredGroupPriority(newPriority: Int): Command

    suspend fun getListed(): Boolean
    suspend fun setListed(newListed: Boolean): Command

    suspend fun getActionContainer(): ActionContainer

    suspend fun delete()

    enum class CommandMode {
        SELF,
        TARGETED
    }
}