package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer
import llc.redstone.systemsapi.importer.ConditionContainer

interface House {
    fun isImporting(): Boolean
    fun setImporting(importing: Boolean)
    suspend fun getCommand(name: String): Command?
    suspend fun createCommand(name: String): Command
    suspend fun getAllCommands(): List<Command>

    suspend fun getEvent(event: Event.Events): ActionContainer
    suspend fun getAllEvents(): List<ActionContainer>

    suspend fun getFunction(name: String): Function?
    suspend fun createFunction(name: String): Function
    suspend fun getAllFunctions(): List<Function>

//    suspend fun getInventoryLayout(name: String): InventoryLayout?
//    suspend fun createInventoryLayout(name: String): InventoryLayout
//    suspend fun getAllInventoryLayouts(): List<InventoryLayout>

    suspend fun getMenu(title: String): Menu?
    suspend fun createMenu(title: String): Menu
    suspend fun getAllMenus(): List<Menu>

    suspend fun getRegion(name: String): Region?
    suspend fun createRegion(name: String): Region
    suspend fun getAllRegions(): List<Region>

//    suspend fun getScoreboardLines(): List<String>
//    suspend fun getScoreboardLine(index: Int): String?
//    suspend fun setScoreboardLines(lines: List<String>)
//    suspend fun setScoreboardLine(line: String, index: Int)
//
//    suspend fun getTeam(name: String): Team?
//    suspend fun createTeam(name: String): Team // throws error if team exists
//    suspend fun getAllTeams(): List<Team>

    suspend fun getOpenActionContainer(): ActionContainer?
    suspend fun getOpenConditionContainer(): ConditionContainer?
}