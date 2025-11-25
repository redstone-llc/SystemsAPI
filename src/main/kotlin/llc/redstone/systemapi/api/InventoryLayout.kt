package llc.redstone.systemapi.api

interface InventoryLayout {
    var name: String

    suspend fun getName(): String = name

    // TODO

    suspend fun delete()
}