package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ScoreboardImporter

interface Scoreboard {
    suspend fun getLines(): List<ScoreboardImporter.LineType>
    suspend fun setLines(newLines: List<ScoreboardImporter.LineType>)
}