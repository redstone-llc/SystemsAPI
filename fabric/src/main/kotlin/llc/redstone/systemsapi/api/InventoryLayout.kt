package llc.redstone.systemsapi.api

interface InventoryLayout {
    var name: String

    suspend fun getName(): String = name

    // TODO

    suspend fun delete()
}