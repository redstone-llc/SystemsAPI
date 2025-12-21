package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot

interface Scoreboard {
    suspend fun getLines(): List<LineType>
    suspend fun setLines(newLines: List<LineType>)


    sealed class VariableType(val displayName: String) {
        object Player : VariableType("Player")
        object Global : VariableType("Global")
        data class Team(val team: String) : VariableType("Team")
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
        data class VariableValue(val scope: Scoreboard.VariableType, val key: String) : LineType("Variable Value", 1)

        companion object {
            private val typesByDisplayName: Map<String, LineType> by lazy {
                LineType::class.sealedSubclasses
                    .mapNotNull { it.objectInstance }
                    .associateBy { it.displayName }
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
                        MenuItems.GO_BACK.click()
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

            private enum class MenuItems(
                val label: String,
                val type: Item? = null
            ) {
                GO_BACK("Go Back", Items.ARROW);

                suspend fun click() = if (type != null) MenuUtils.clickItems(label, type) else MenuUtils.clickItems(label)
                fun find(): Slot = if (type != null) MenuUtils.findSlots(label, type).first() else MenuUtils.findSlots(label).first()
            }

        }
    }
}