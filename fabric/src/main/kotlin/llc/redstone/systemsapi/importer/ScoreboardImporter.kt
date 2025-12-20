package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.LOGGER
import llc.redstone.systemsapi.api.Scoreboard
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.InputUtils.setKeyedCycle
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.item.Items

object ScoreboardImporter : Scoreboard {

    suspend fun openScoreboardMenu() {
        if (isScoreboardMenuOpen()) return
        CommandUtils.runCommand("scoreboard")
        MenuUtils.onOpen("Scoreboard Editor")
    }

    fun isScoreboardMenuOpen(): Boolean =
        runCatching { MenuUtils.currentMenu().title.string == "Scoreboard Editor" }.getOrDefault(false)

    override suspend fun getLines(): List<Scoreboard.LineType> {
        openScoreboardMenu()

        val gui = MenuUtils.currentMenu()
        return gui.screenHandler.slots
            .filter { it.id <= 44 && it.hasStack() }
            .mapNotNull { Scoreboard.LineType.fromItemStack(it.stack, it.id) }
    }

    override suspend fun setLines(newLines: List<Scoreboard.LineType>) {
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
                delay(10)
            }
        } while (elements.isNotEmpty())

        // create new ones
        newLines.forEach { line ->
            MenuUtils.clickMenuSlot(MenuItems.ADD_ITEM)
            MenuUtils.onOpen("Add Scoreboard Item")
            MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(null, line.displayName))
            MenuUtils.onOpen("Scoreboard Editor")

            val gui = MenuUtils.currentMenu()

            when (line) {
                is Scoreboard.LineType.CustomLine -> {
                    val itemIndex = (44 downTo 0).firstOrNull { index ->
                        val slot = gui.screenHandler.getSlot(index)
                        slot.hasStack()
                    } ?: throw IllegalStateException("There are no scoreboard lines when there should be")

                    MenuUtils.packetClick(itemIndex)
                    MenuUtils.onOpen("Item Settings")
                    MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(null, "Text"))
                    InputUtils.textInput(line.text)
                    MenuUtils.onOpen("Item Settings")
                    MenuUtils.clickMenuSlot(MenuItems.GO_BACK)
                    MenuUtils.onOpen("Scoreboard Editor")
                }
                is Scoreboard.LineType.VariableValue -> {
                    val itemIndex = (44 downTo 0).firstOrNull { index ->
                        val slot = gui.screenHandler.getSlot(index)
                        slot.hasStack()
                    } ?: throw IllegalStateException("There are no scoreboard lines when there should be")

                    MenuUtils.packetClick(itemIndex)
                    MenuUtils.onOpen("Item Settings")

                    // Cycle through to the correct scope
                    val slot = MenuUtils.findSlot(MenuUtils.MenuSlot(null, "Holder")) ?: throw IllegalStateException("Could not find Holder slot")
                    setKeyedCycle(slot, line.scope.displayName)

                    if (line.scope is Scoreboard.VariableType.Team) {
                        MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(Items.OAK_SIGN, "Team"))
                        MenuUtils.onOpen("Select Option")
                        MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(null, line.scope.team))
                        MenuUtils.onOpen("Item Settings")
                    }
                    MenuUtils.clickMenuSlot(MenuUtils.MenuSlot(Items.PAPER, "Variable"))
                    InputUtils.textInput(line.key)
                    MenuUtils.onOpen("Item Settings")
                    MenuUtils.clickMenuSlot(MenuItems.GO_BACK)
                    MenuUtils.onOpen("Scoreboard Editor")
                }
                else -> {}
            }

        }
    }

    private object MenuItems {
        val ADD_ITEM = MenuUtils.MenuSlot(Items.PAPER, "Add Scoreboard Item")
        val GO_BACK = MenuUtils.MenuSlot(Items.ARROW, "Go Back")
    }

}