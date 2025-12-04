package llc.redstone.systemsapi.api

interface House {
    fun getFunctionOrNull(name: String): Function?
    fun getAllFunctions(): List<Function>
    fun getMenuOrNull(title: String): Menu?
    fun getCommandOrNull(name: String): Command?
}