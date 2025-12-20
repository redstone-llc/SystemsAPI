package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.*
import llc.redstone.systemsapi.api.Function
import llc.redstone.systemsapi.util.CommandUtils.getTabCompletions
import llc.redstone.systemsapi.util.MenuUtils

internal object HouseImporter : House {
    private var importing = false
    override fun isImporting(): Boolean {
        return importing
    }

    override fun setImporting(importing: Boolean) {
        this.importing = importing
    }


    override suspend fun getCommand(name: String): Command? {
        val commandImporter = CommandImporter(name)
        return if (commandImporter.exists()) commandImporter else null
    }

    override suspend fun createCommand(name: String): Command {
        val commandImporter = CommandImporter(name)
        commandImporter.create()
        return commandImporter
    }

    override suspend fun getAllCommands(): List<Command> {
        return getTabCompletions("command edit")
            .mapNotNull { getCommand(it) }
    }

    override suspend fun getEvent(event: Event.Events): ActionContainer {
        return EventImporter.getActionContainerForEvent(event)
    }

    override suspend fun getAllEvents(): List<ActionContainer> {
        return Event.Events.entries
            .map { getEvent(it) }
    }

    override suspend fun getFunction(name: String): Function? {
        val functionImporter = FunctionImporter(name)
        return if (functionImporter.exists()) functionImporter else null
    }

    override suspend fun createFunction(name: String): Function {
        val functionImporter = FunctionImporter(name)
        functionImporter.create()
        return functionImporter
    }

    override suspend fun getAllFunctions(): List<Function> {
        return getTabCompletions("function edit")
            .mapNotNull { getFunction(it) }
    }

    override suspend fun getInventoryLayout(name: String): Layout? {
        val layout = LayoutImporter(name)
        return if (layout.exists()) layout else null
    }

    override suspend fun createInventoryLayout(name: String): Layout {
        val layout = LayoutImporter(name)
        layout.create()
        return layout
    }

    override suspend fun getAllInventoryLayouts(): List<Layout> {
        return getTabCompletions("layout edit")
            .mapNotNull { getInventoryLayout(it) }
    }

    override suspend fun getMenu(title: String): Menu? {
        val menuImporter = MenuImporter(title)
        return if (menuImporter.exists()) menuImporter else null
    }

    override suspend fun createMenu(title: String): Menu {
        val menuImporter = MenuImporter(title)
        menuImporter.create()
        return menuImporter
    }

    override suspend fun getAllMenus(): List<Menu> {
        return getTabCompletions("menu edit")
            .mapNotNull { getMenu(it) }
    }

    override suspend fun getRegion(name: String): Region? {
        val regionImporter = RegionImporter(name)
        return if (regionImporter.exists()) regionImporter else null
    }

    override suspend fun createRegion(name: String): Region {
        val regionImporter = RegionImporter(name)
        regionImporter.create()
        try {
            MenuUtils.onOpen("Edit: $name")
        } catch (e: Exception) {
            throw IllegalStateException("Could not create region, make sure you have a region selection made")
        }
        return regionImporter
    }

    override suspend fun getAllRegions(): List<Region> {
        return getTabCompletions("region edit")
            .mapNotNull { getRegion(it) }
    }

    override suspend fun getScoreboardLines(): List<Scoreboard.LineType> {
        return ScoreboardImporter.getLines()
    }

    override suspend fun setScoreboardLines(lines: List<Scoreboard.LineType>) {
        ScoreboardImporter.setLines(lines)
    }

    override suspend fun getTeam(name: String): Team? {
        val teamImporter = TeamImporter(name)
        return if (teamImporter.exists()) teamImporter else null
    }

    override suspend fun createTeam(name: String): Team {
        val teamImporter = TeamImporter(name)
        teamImporter.create()
        return teamImporter
    }

    override suspend fun getAllTeams(): List<Team> {
        TODO("Teams don't have tab completion...")
    }

    override suspend fun getOpenActionContainer(): ActionContainer? {
        return try {
            ActionContainer()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getOpenConditionContainer(): ConditionContainer? {
        if (MC.currentScreen?.title?.string?.contains("Edit Conditions") != true) return null
        return ConditionContainer
    }

    override suspend fun getHouseSettings(): HouseSettings {
        return HouseSettingsImporter
    }

    override suspend fun getGamerule(gamerule: Gamerule.Gamerules): Boolean {
        return GameruleImporter.getGamerule(gamerule)
    }

    override suspend fun setGamerule(gamerule: Gamerule.Gamerules, newValue: Boolean) {
        GameruleImporter.setGamerule(gamerule, newValue)
    }


}