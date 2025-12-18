package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.api.Scoreboard
import llc.redstone.systemsapi.importer.ScoreboardImporter.LineType.Companion.fromItemStack
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.ItemUtils.loreLine
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.item.Items

class ScoreboardImporter : Scoreboard {

    suspend fun openScoreboardMenu() {
        if (isScoreboardMenuOpen()) return
        CommandUtils.runCommand("scoreboard")
        MenuUtils.onOpen("Scoreboard Editor")
    }

    fun isScoreboardMenuOpen(): Boolean =
        runCatching { MenuUtils.currentMenu().title.string == "Scoreboard Editor" }.getOrDefault(false)

    override suspend fun getLines(): List<LineType> {
        openScoreboardMenu()

        val gui = MenuUtils.currentMenu()
        return gui.screenHandler.slots
            .filter { it.id <= 17 && it.hasStack() }
            .mapNotNull { fromItemStack(it.stack) }
    }

    override suspend fun setLines(newLines: List<LineType>) {
        if (newLines.sumOf { it.lines } !in 1..10) throw IllegalArgumentException("New lines exceed scoreboard line limit of 1..10")
        openScoreboardMenu()

        newLines.forEach { line ->
            MenuUtils.clickMenuSlot(MenuItems.ADD_ITEM)
            MenuUtils.onOpen("Add Scoreboard Item")
            MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(null, line.displayName))
            MenuUtils.onOpen("Scoreboard Editor")

            val gui = MenuUtils.currentMenu()

            when (line) {
                is LineType.CustomLine -> {
                    val itemIndex = (17 downTo 0).firstOrNull { index ->
                        val slot = gui.screenHandler.getSlot(index)
                        slot.hasStack()
                    } ?: throw IllegalStateException("There are no scoreboard lines when there should be")

                    MenuUtils.packetClick(itemIndex)
                    MenuUtils.onOpen("Item Settings")
                    MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(null, "Text"))
                    TextUtils.input(line.text)
                    MenuUtils.clickMenuSlot(MenuItems.GO_BACK)
                    MenuUtils.onOpen("Scoreboard Editor")
                }
                is LineType.VariableValue -> {
                    val itemIndex = (17 downTo 0).firstOrNull { index ->
                        val slot = gui.screenHandler.getSlot(index)
                        slot.hasStack()
                    } ?: throw IllegalStateException("There are no scoreboard lines when there should be")

                    MenuUtils.packetClick(itemIndex)
                    MenuUtils.onOpen("Item Settings")
                    // TODO: Cycle through to the correct scope
                    if (line.scope is VariableType.Team) {
                        MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(Items.OAK_SIGN, "Team"))
                        TextUtils.input(line.scope.team)
                    }
                    MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(Items.PAPER, "Variable"))
                    TextUtils.input(line.key)

                    TODO("Cycle through to the correct scope")
                }
                else -> {}
            }

        }
    }

    sealed class LineType(val displayName: String, val lines: Int) {
        object BlankLine : LineType("Blank Line", 1)
        data class CustomLine(val text: String) : LineType("Custom Line", 1)
        object HouseName : LineType("House Name", 2)
        object GuestsCount : LineType("Guests Count", 1)
        object CookieCount : LineType("Cookie Count", 1)
        object PlayerGroup : LineType("Player Group", 1)
        object PvpEnabledState : LineType("PvP Enabled State", 1)
        object Gamemode : LineType("Gamemode", 1)
        object HouseMode : LineType("House Mode", 1)
        object ParkourTime : LineType("Parkour Time", 1)
        data class VariableValue(val scope: VariableType, val key: String) : LineType("Variable Value", 1)

        companion object {
            private val typesByDisplayName: Map<String, LineType> by lazy {
                LineType::class.sealedSubclasses
                    .mapNotNull { it.objectInstance }
                    .associateBy { it.displayName }
            }
            suspend fun fromItemStack(stack: net.minecraft.item.ItemStack): LineType? {
                val name = runCatching { stack.name.string }.getOrNull() ?: return null

                return when (name) {
                    "Custom Line" -> {
                        val text = MenuUtils.getPreviousInput { MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(Items.PAPER, "Text")) }
                        CustomLine(text)
                    }
                    "Variable Value" -> {
                        val scope = when (stack.loreLine(1, false).substringAfter("Holder: ")) {
                            "Player" -> VariableType.Player
                            "Global" -> VariableType.Global
                            "Team" -> {
                                VariableType.Team(stack.loreLine(2, false).substringAfter("Team: "))
                            }

                            else -> throw IllegalStateException("Unknown variable type $name")
                        }
                        val key = if (scope is VariableType.Team) stack.loreLine(3, false).substringAfter("Team: ") else stack.loreLine(2, false).substringAfter("Team: ")
                        VariableValue(scope, key)
                    }
                    else -> typesByDisplayName[name]
                }
            }
        }
    }

    sealed class VariableType(val displayName: String) {
        object Player : VariableType("Player")
        object Global : VariableType("Global")
        data class Team(val team: String) : VariableType("Team")
    }

    private object MenuItems {
        val ADD_ITEM = MenuUtils.MenuSlot(Items.PAPER, "Add Scoreboard Item")
        val GO_BACK = MenuUtils.MenuSlot(Items.ARROW, "Go Back")
    }

}