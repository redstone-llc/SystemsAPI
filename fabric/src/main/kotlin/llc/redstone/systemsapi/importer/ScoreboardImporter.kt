package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.LOGGER
import llc.redstone.systemsapi.SystemsAPI.scaledDelay
import llc.redstone.systemsapi.api.Scoreboard
import llc.redstone.systemsapi.api.Scoreboard.LineType
import llc.redstone.systemsapi.api.Scoreboard.LineType.Companion.typesByDisplayName
import llc.redstone.systemsapi.api.Scoreboard.LineType.CustomLine
import llc.redstone.systemsapi.api.Scoreboard.LineType.VariableValue
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.InputUtils.setKeyedCycle
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import net.minecraft.item.Items

object ScoreboardImporter : Scoreboard {

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
            .filter { it.id <= 44 && it.hasStack() }
            .mapNotNull { fromItemStack(it.stack, it.id) }
    }

    override suspend fun setLines(newLines: List<LineType>) {
        if (newLines.sumOf { it.lines } !in 1..10) throw IllegalArgumentException("New lines exceed scoreboard line limit of 1..10")
        openScoreboardMenu()

        // remove old lines
        do {
            val gui = MenuUtils.currentMenu()
            val elements = gui.screenHandler.slots
                .filter { it.id <= 44 && it.hasStack() && !it.stack.item.equals(Items.BEDROCK)}
                .sortedByDescending { it.id }
            for (element in elements) {
                LOGGER.info("clicking slot ${element.id}")
                MenuUtils.packetClick(element.id, 1)
                scaledDelay(0.5) // TODO: check--is this too fast?
            }
        } while (elements.isNotEmpty())

        // create new ones
        newLines.forEach { line ->
            MenuUtils.clickItems(MenuItems.add)
            MenuUtils.onOpen("Add Scoreboard Item")
            MenuUtils.clickItems(line.displayName)
            MenuUtils.onOpen("Scoreboard Editor")

            val gui = MenuUtils.currentMenu()

            when (line) {
                is LineType.CustomLine -> {
                    val itemIndex = (44 downTo 0).firstOrNull { index ->
                        val slot = gui.screenHandler.getSlot(index)
                        slot.hasStack()
                    } ?: throw IllegalStateException("There are no scoreboard lines when there should be")

                    MenuUtils.packetClick(itemIndex)
                    MenuUtils.onOpen("Item Settings")
                    MenuUtils.clickItems("Text")
                    InputUtils.textInput(line.text)
                    MenuUtils.onOpen("Item Settings")
                    MenuUtils.clickItems(MenuItems.back)
                    MenuUtils.onOpen("Scoreboard Editor")
                }
                is LineType.VariableValue -> {
                    val itemIndex = (44 downTo 0).firstOrNull { index ->
                        val slot = gui.screenHandler.getSlot(index)
                        slot.hasStack()
                    } ?: throw IllegalStateException("There are no scoreboard lines when there should be")

                    MenuUtils.packetClick(itemIndex)
                    MenuUtils.onOpen("Item Settings")

                    // Cycle through to the correct scope
                    val slot = MenuUtils.findSlots(MenuItems.holderSelector).first()
                    setKeyedCycle(slot, line.scope.displayName)

                    if (line.scope is Scoreboard.VariableType.Team) {
                        MenuUtils.clickItems(MenuItems.teamSelector)
                        MenuUtils.onOpen("Select Option")
                        MenuUtils.clickItems(line.scope.team)
                        MenuUtils.onOpen("Item Settings")
                    }
                    MenuUtils.clickItems(MenuItems.variableSelector)
                    InputUtils.textInput(line.key)
                    MenuUtils.onOpen("Item Settings")
                    MenuUtils.clickItems(MenuItems.back)
                    MenuUtils.onOpen("Scoreboard Editor")
                }
                else -> {}
            }

        }
    }

    suspend fun fromItemStack(stack: net.minecraft.item.ItemStack, slot: Int): LineType? {
        val name = runCatching { stack.name.string }.getOrNull() ?: return null

        return when (name) {
            "Custom Line" -> {
                MenuUtils.packetClick(slot)
                MenuUtils.onOpen("Item Settings")
                val text = InputUtils.getPreviousInput {
                    MenuUtils.clickItems("Text", Items.PAPER)
                }
                MenuUtils.onOpen("Item Settings")
                MenuUtils.clickItems(MenuItems.back)
                MenuUtils.onOpen("Scoreboard Editor")
                CustomLine(text)
            }
            "Variable Value" -> {
                val scope = when (stack.getProperty("Holder")) {
                    "Player" -> Scoreboard.VariableType.Player
                    "Global" -> Scoreboard.VariableType.Global
                    "Team" -> {
                        Scoreboard.VariableType.Team(stack.getProperty("Team") ?: throw IllegalStateException("Could not find variable team"))
                    }
                    else -> throw IllegalStateException("Could not find variable holder")
                }
                val key = stack.getProperty("Variable") ?: throw IllegalStateException("Could not find variable key")
                VariableValue(scope, key)
            }
            else -> typesByDisplayName[name]
        }
    }

    private object MenuItems {
        val add = ItemSelector(
            name = NameExact("Add Scoreboard Items"),
            item = ItemExact(Items.PAPER)
        )
        val back = ItemSelector(
            name = NameExact("Go Back"),
            item = ItemExact(Items.ARROW)
        )
        val teamSelector = ItemSelector(
            name = NameExact("Team"),
            item = ItemExact(Items.OAK_SIGN)
        )
        val holderSelector = ItemSelector(
            name = NameExact("Holder")
        )
        val variableSelector = ItemSelector(
            name = NameExact("Variable"),
            item = ItemExact(Items.PAPER)
        )
    }

}