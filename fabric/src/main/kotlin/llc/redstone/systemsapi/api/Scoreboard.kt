package llc.redstone.systemsapi.api

interface Scoreboard {
    suspend fun getLines(): List<String>
    suspend fun setLines(newLines: List<String>)
}