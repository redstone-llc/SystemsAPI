package llc.redstone.systemapi.importer

import llc.redstone.systemapi.SystemAPI.MC
import llc.redstone.systemapi.data.Action
import llc.redstone.systemapi.data.Condition
import llc.redstone.systemapi.data.CustomKey
import llc.redstone.systemapi.data.DisplayName
import llc.redstone.systemapi.data.Keyed
import llc.redstone.systemapi.data.KeyedCycle
import llc.redstone.systemapi.data.StatOp
import llc.redstone.systemapi.data.StatValue
import llc.redstone.systemapi.util.ItemUtils.loreLine
import llc.redstone.systemapi.util.MenuUtils
import llc.redstone.systemapi.util.MenuUtils.MenuSlot
import llc.redstone.systemapi.util.MenuUtils.Target
import llc.redstone.systemapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Items
import kotlin.math.abs
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

//The title of the actions gui, either Actions: <name> or Edit Actions
class ActionInteraction(val title: String) {
    companion object {
        private val slots = mapOf(
            0 to 10,
            1 to 11,
            2 to 12,
            3 to 13,
            4 to 14,
            5 to 15,
            6 to 16,
            7 to 19,
            8 to 20,
        )
    }

    //List of actions to add to the container
    suspend fun addActions(actions: List<Action>) {
        for (action in actions) {
            //Wait for the "Actions: <name>" or "Edit Actions" to open
            //We do this every iteration to make sure we are right back at the Actions page
            MenuUtils.onOpen(title)

            //Add an action
            MenuUtils.clickMenuSlot(MenuItems.ADD_ACTION)
            MenuUtils.onOpen("Add Action")

            //Get the action parameters/properties
            val parameters = action::class.primaryConstructor!!.parameters.toMutableList()
            val actionProperties = action.javaClass.kotlin.memberProperties

            val properties = mutableListOf<KProperty1<Action, *>>()
            for (parm in parameters) {
                properties.add(actionProperties.find { it.name == parm.name } ?: continue)
            }

            //Get the Display Name of the action and add it
            val displayName = (action::class.annotations.find { it is DisplayName } as DisplayName).value
            MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, displayName)))

            //For change variable, because the holder isn't found in the parameters
            if (action is Action.ChangeVariable) {
                properties.add(0, actionProperties.find { it.name == "holder" } ?: continue)
            }


            //Iterate through parameters
            for ((index, property) in properties.withIndex()) {
                //Get the property and its values
                val value = property.get(action)

                //Make sure we are in the right gui before continuing
                MenuUtils.onOpen("Action Settings")
                val gui = MC.currentScreen as? GenericContainerScreen ?: return

                //Place in the gui to click
                val slotIndex = slots[index]!!
                val slot = gui.screenHandler.getSlot(slotIndex)

                //All other properties
                when (property.returnType.classifier) {
                    String::class, Int::class, Double::class -> {
                        MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                        TextUtils.input(value.toString(), 100L)
                    }

                    StatValue::class -> {
                        MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                        TextUtils.input(value.toString(), 100L)
                    }

                    Boolean::class -> {
                        val line = slot.stack.loreLine(false, filter = { str -> str == "Disabled" || str == "Enabled" })
                            ?: continue
                        val currentValue = line == "Enabled"
                        val boolValue = value as Boolean
                        if (currentValue != boolValue) {
                            MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                        }
                    }

                    List::class -> {
                        val value = value as List<*>
                        if (value.isEmpty()) continue
                        //Then we assume they all are actions
                        if (value.first() is Action) {
                            MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                            ActionInteraction("Edit Actions").addActions(value as List<Action>)
                            MenuUtils.onOpen("Edit Actions")
                            MenuUtils.clickMenuSlot(MenuItems.BACK)
                            MenuUtils.onOpen("Action Settings")
                        } else if (value.first() is Condition) {
                            MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                            ConditionInteraction.addConditions(value as List<Condition>)
                            MenuUtils.onOpen("Edit Conditions")
                            MenuUtils.clickMenuSlot(MenuItems.BACK)
                            MenuUtils.onOpen("Action Settings")
                        }
                    }

                    StatOp::class -> {
                        val operation = value as StatOp

                        val value = slot.stack.loreLine(false, filter = { str -> str == operation.key })
                        if (value == null) {
                            MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                            MenuUtils.onOpen("Select Option")

                            if (operation.advanced) {
                                val gui = MC.currentScreen as? GenericContainerScreen ?: continue
                                val operationSlot = MenuUtils.findSlot(gui, MenuItems.TOGGLE_ADVANCED_OPERATIONS)
                                val line = operationSlot?.stack?.loreLine(4, false) ?: continue
                                val currentValue = line == "Disabled"
                                if (currentValue) {
                                    MenuUtils.clickMenuSlot(MenuItems.TOGGLE_ADVANCED_OPERATIONS)
                                }
                            }

                            MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, operation.key)))
                        }
                        continue
                    }
                }

                //Enum action properties
                if (property.returnType.isSubtypeOf(Keyed::class.starProjectedType)) {
                    val keyed = value as Keyed

                    val entries = value.javaClass.enumConstants

                    if (keyed is KeyedCycle) {
                        val holderIndex = entries.indexOf(keyed) + 1
                        val stack = slot.stack

                        val current = stack.loreLine(true) { str -> str.contains("&a") || str.contains("&c") } ?: continue
                        val currentHolder = entries.find { current.contains(it.key) }
                        val currentIndex = if (currentHolder != null ) entries.indexOf(currentHolder) + 1 else 0
                        if (currentHolder != keyed) {
                            val clicks = holderIndex - currentIndex

                            repeat(abs(clicks)) {
                                MenuUtils.clickMenuTargets(Target(MenuSlot(null, null, 10), if (clicks > 0) 0 else 1))
                            }
                        }

                        continue
                    }

                    if (slot.stack.loreLine(false, filter = { str -> str == value.key }) == null) {
                        MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                        MenuUtils.onOpen("Select Option")
                        MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, keyed.key)))

                        if (keyed::class.annotations.find { it is CustomKey } != null) {
                            TextUtils.input(value.toString(), 200L)
                        }
                    }

                    continue
                }
            }
            //Make sure we are in the action settings menu before we go back to actions to add another one
            MenuUtils.onOpen("Action Settings")
            MenuUtils.clickMenuSlot(MenuItems.BACK)
            MenuUtils.onOpen(title)
        }
    }

    object MenuItems {
        val ADD_ACTION = MenuSlot(Items.PAPER, "Add Action")
        val BACK = MenuSlot(Items.ARROW, "Go Back")
        val TOGGLE_ADVANCED_OPERATIONS = MenuSlot(Items.COMMAND_BLOCK, "Toggle Advanced Operations")
    }
}