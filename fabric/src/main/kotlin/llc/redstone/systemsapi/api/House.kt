package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer
import llc.redstone.systemsapi.importer.ConditionContainer

/**
 * Represents a house in Housing.
 *
 * A House provides accessors and mutators to all major systems in a house.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface House {
    /**
     * Returns whether an import operation is currently running for this house.
     *
     * @return true if an import is in progress, false otherwise
     */
    fun isImporting(): Boolean

    /**
     * Sets the importing state for this house.
     *
     * @param importing true to mark an import as running, false to mark it stopped
     */
    fun setImporting(importing: Boolean)

    /**
     * > **WARNING:** Not ready for use!
     *
     * Returns the remaining time (in seconds) for the current import operation, or null
     * if no import is active or if the remaining time is not available.
     *
     * @return remaining import time in seconds, or null
     */
    fun getTimeRemaining(): Float?

    /**
     * Cancels the current import operation if one is running.
     */
    fun cancelImport()

    /**
     * Retrieves a command by name.
     *
     * @param name command name to look up
     * @return the [Command] if found, or null otherwise
     */
    suspend fun getCommand(name: String): Command?

    /**
     * Creates a new command with the given name.
     *
     * @param name desired command name
     * @return the created [Command] instance
     */
    suspend fun createCommand(name: String): Command

    /**
     * Returns all commands defined in this house.
     *
     * @return list of all [Command] objects
     */
    suspend fun getAllCommands(): List<Command>

    /**
     * Retrieves the action container for a specific event.
     *
     * @param event the event to retrieve actions for
     * @return the [ActionContainer] for the event
     */
    suspend fun getEvent(event: Event.Events): ActionContainer

    /**
     * Returns all event action containers present in this house.
     *
     * @return list of [ActionContainer] instances for all events
     */
    suspend fun getAllEvents(): List<ActionContainer>

    /**
     * Retrieves a function by name.
     *
     * @param name function name to look up
     * @return the [Function] if found, or null otherwise
     */
    suspend fun getFunction(name: String): Function?

    /**
     * Creates a new function with the given name.
     *
     * @param name desired function name
     * @return the created [Function] instance
     */
    suspend fun createFunction(name: String): Function

    /**
     * Returns all functions defined in this house.
     *
     * @return list of all [Function] objects
     */
    suspend fun getAllFunctions(): List<Function>

    /**
     * Retrieves an inventory layout by name.
     *
     * @param name layout name to look up
     * @return the [Layout] if found, or null otherwise
     */
    suspend fun getInventoryLayout(name: String): Layout?

    /**
     * Creates a new inventory layout with the given name.
     *
     * @param name desired layout name
     * @return the created [Layout] instance
     */
    suspend fun createInventoryLayout(name: String): Layout

    /**
     * Returns all inventory layouts defined in this house.
     *
     * @return list of all [Layout] objects
     */
    suspend fun getAllInventoryLayouts(): List<Layout>

    /**
     * Retrieves a menu by title.
     *
     * @param title menu title to look up
     * @return the [Menu] if found, or null otherwise
     */
    suspend fun getMenu(title: String): Menu?

    /**
     * Creates a new menu with the given title.
     *
     * @param title desired menu title
     * @return the created [Menu] instance
     */
    suspend fun createMenu(title: String): Menu

    /**
     * Returns all menus defined in this house.
     *
     * @return list of all [Menu] objects
     */
    suspend fun getAllMenus(): List<Menu>

    /**
     * Retrieves an NPC by name.
     *
     * @param name NPC name to look up
     * @return the [Npc] if found, or null otherwise
     */
    suspend fun getNpc(name: String): Npc?

    /**
     * Returns all NPCs defined in this house.
     *
     * @return list of all [Npc] objects
     */
    suspend fun getAllNpcs(): List<Npc>

    /**
     * Retrieves a permission group by name.
     *
     * @param name group name to look up
     * @return the [Group] if found, or null otherwise
     */
    suspend fun getGroup(name: String): Group?

    /**
     * Creates a new permission group with the given name.
     *
     * @param name desired group name
     * @return the created [Group] instance
     */
    suspend fun createGroup(name: String): Group

    /**
     * Returns all groups defined in this house.
     *
     * @return list of all [Group] objects
     */
    suspend fun getAllGroups(): List<Group>

    /**
     * Retrieves a region by name.
     *
     * @param name region name to look up
     * @return the [Region] if found, or null otherwise
     */
    suspend fun getRegion(name: String): Region?

    /**
     * Creates a new region with the given name.
     *
     * @param name desired region name
     * @return the created [Region] instance
     */
    suspend fun createRegion(name: String): Region

    /**
     * Returns all regions defined in this house.
     *
     * @return list of all [Region] objects
     */
    suspend fun getAllRegions(): List<Region>

    /**
     * Returns the configured scoreboard lines for this house.
     *
     * @return list of [Scoreboard.LineType] entries representing the scoreboard
     */
    suspend fun getScoreboardLines(): List<Scoreboard.LineType>

    /**
     * Sets the scoreboard lines for this house.
     *
     * @param lines list of [Scoreboard.LineType] to apply
     */
    suspend fun setScoreboardLines(lines: List<Scoreboard.LineType>)

    /**
     * Retrieves a team by name.
     *
     * @param name team name to look up
     * @return the [Team] if found, or null otherwise
     */
    suspend fun getTeam(name: String): Team?

    /**
     * Creates a new team with the given name.
     *
     * @param name desired team name
     * @return the created [Team] instance
     */
    suspend fun createTeam(name: String): Team

    /**
     * Returns all teams defined in this house.
     *
     * @return list of all [Team] objects
     */
    suspend fun getAllTeams(): List<Team>

    /**
     * Returns the currently open action container in the editor, if any.
     *
     * @return open [ActionContainer] or null
     */
    suspend fun getOpenActionContainer(): ActionContainer?

    /**
     * Returns the currently open condition container in the editor, if any.
     *
     * @return open [ConditionContainer] or null
     */
    suspend fun getOpenConditionContainer(): ConditionContainer?

    /**
     * Returns the house-level settings object.
     *
     * @return [HouseSettings] for this house
     */
    suspend fun getHouseSettings(): HouseSettings

    /**
     * Reads the value of a gamerule.
     *
     * @param gamerule the gamerule to query
     * @return true if the gamerule is enabled, false otherwise
     */
    suspend fun getGamerule(gamerule: Gamerule.Gamerules): Boolean

    /**
     * Writes a new value for a gamerule.
     *
     * @param gamerule the gamerule to update
     * @param newValue the boolean value to set
     */
    suspend fun setGamerule(gamerule: Gamerule.Gamerules, newValue: Boolean)
}