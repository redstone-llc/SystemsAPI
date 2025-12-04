package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.data.Action

interface Npc {
    var name: String
    suspend fun getName(): String = name
    suspend fun setName(newName: String)

    suspend fun getLookAtPlayers(): Boolean
    suspend fun setLookAtPlayers(newLookAtPlayers: Boolean)

    suspend fun getHideNameTag(): Boolean
    suspend fun setHideNameTag(newHideNameTag: Boolean)

    suspend fun getLeftClickActions(): List<Action>
    suspend fun setLeftClickActions(newActions: List<Action>, optimized: Boolean = false)

    suspend fun getRightClickActions(): List<Action>
    suspend fun setRightClickActions(newActions: List<Action>, optimized: Boolean = false)

    suspend fun delete()
}