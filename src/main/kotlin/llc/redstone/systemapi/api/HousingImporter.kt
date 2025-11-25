package llc.redstone.systemapi.api

interface HousingImporter {
    fun getFunctionOrNull(name: String): Function?
    fun getAllFunctions(): List<Function>
    fun getCommandOrNull(name: String): Command?
}