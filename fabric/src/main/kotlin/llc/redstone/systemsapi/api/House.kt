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

    suspend fun getInventoryLayout(name: String): Layout?
    suspend fun createInventoryLayout(name: String): Layout
    suspend fun getAllInventoryLayouts(): List<Layout>

    suspend fun getMenu(title: String): Menu?
    suspend fun createMenu(title: String): Menu
    suspend fun getAllMenus(): List<Menu>

//    suspend fun getNpc(name: String): Npc?
//    suspend fun getAllNpcs(): List<Npc>

    suspend fun getRegion(name: String): Region?
    suspend fun createRegion(name: String): Region
    suspend fun getAllRegions(): List<Region>

    suspend fun getScoreboardLines(): List<Scoreboard.LineType>
    suspend fun setScoreboardLines(lines: List<Scoreboard.LineType>)

    suspend fun getTeam(name: String): Team?
    suspend fun createTeam(name: String): Team
    suspend fun getAllTeams(): List<Team>

    suspend fun getOpenActionContainer(): ActionContainer?
    suspend fun getOpenConditionContainer(): ConditionContainer?

    suspend fun getGamerule(gamerule: Gamerule.Gamerules): Boolean
    suspend fun setGamerule(gamerule: Gamerule.Gamerules, newValue: Boolean)
}