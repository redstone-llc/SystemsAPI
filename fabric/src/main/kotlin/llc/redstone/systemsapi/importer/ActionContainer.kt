package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.data.ActionDefinition
import llc.redstone.systemsapi.data.VariableHolder
import llc.redstone.systemsapi.util.ItemStackUtils.loreLines
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.item.Items
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

//The title of the actions gui, either Actions: <name> or Edit Actions
class ActionContainer(
    val title: String = MC.currentScreen?.title?.string ?: throw IllegalStateException("No screen is currently open")
) {
    companion object {
        private val slots = mutableMapOf(
            0 to 10,
            1 to 11,
            2 to 12,
            3 to 13,
            4 to 14,
            5 to 15,
            6 to 16,
            7 to 19,
            8 to 20,
            9 to 21,
            10 to 22,
            11 to 23,
            12 to 24,
            13 to 25,
            14 to 28,
            15 to 29,
            16 to 30,
            17 to 31,
            18 to 32,
            19 to 33,
            20 to 34,
        )
    }

    suspend fun getActions(): List<Action> {
        HouseImporter.setImporting(true)
        try {
            val actions = mutableListOf<Action>()

            MenuUtils.onOpen(title)


            if (MenuUtils.findSlots(MenuItems.NO_ACTIONS).firstOrNull() != null) return actions

            for (slotIndex in slots.values) {
                val slot = MenuUtils.getSlot(slotIndex)
                if (!slot.hasStack()) break //No more actions

                val item = slot.stack
                val loreLines = item.loreLines(true).filter {
                    it.contains(":") //Only care about lines with properties
                }

                val name = TextUtils.convertTextToString(item.name, false)
                var actionClass = Action::class.sealedSubclasses.firstOrNull() {
                    it.findAnnotations(ActionDefinition::class).any { ann -> ann.displayName == name }
                } ?: continue

                var constructor = actionClass.primaryConstructor!!
                var parameters = constructor.parameters.toMutableList()
                var actionProperties = actionClass.memberProperties
                var properties = mutableListOf<Pair<KProperty1<Action, *>, KParameter?>>()

                for (parm in parameters) {
                    properties.add(actionProperties.find { it.name == parm.name } as KProperty1<Action, *> to parm)
                }

                suspend fun args(indexAddition: Int = 0): MutableMap<KParameter, Any?> {
                    val args = mutableMapOf<KParameter, Any?>()
                    properties.forEachIndexed { index, (prop, param) ->
                        if (param == null) return@forEachIndexed
                        val colorValue =
                            (loreLines.getOrNull(index + indexAddition)?.split(": ")?.drop(1)?.joinToString(": ")
                                ?: return@forEachIndexed).replaceFirst("&f", "")
                        val value = colorValue.replace(Regex("&[0-9a-fk-or]"), "")

                        val returnValue = PropertySettings.export(
                            title,
                            prop,
                            slot,
                            slots[index + indexAddition]!!,
                            value,
                            colorValue
                        )
                        if (returnValue is VariableHolder) {
                            actionClass = when (returnValue) {
                                VariableHolder.Player -> Action.PlayerVariable::class
                                VariableHolder.Global -> Action.GlobalVariable::class
                                VariableHolder.Team -> Action.TeamVariable::class
                            }
                            constructor = actionClass.primaryConstructor!!
                            parameters = constructor.parameters.toMutableList()
                            actionProperties = actionClass.memberProperties
                            properties = mutableListOf()
                            for (parm in parameters) {
                                properties.add(actionProperties.find { it.name == parm.name } as KProperty1<Action, *> to parm)
                            }
                            // I hate recursion, but I think this is the cleanest way to handle it
                            return args(1)
                        }

                        args[param] = returnValue
                    }
                    return args
                }

                val args = args()

                if (args.size != constructor.parameters.size) {
                    actionClass.constructors.forEach { newCon ->
                        if (constructor.parameters.size == newCon.parameters.size) {
                            actions.add(newCon.callBy(args))
                        }
                    }
                } else {
                    actions.add(constructor.callBy(args))
                }
            }

            MenuUtils.onOpen(title)
            if (MenuUtils.findSlots(MenuUtils.GlobalMenuItems.NEXT_PAGE).firstOrNull() != null) {
                MenuUtils.clickItems(MenuUtils.GlobalMenuItems.NEXT_PAGE)
                MenuUtils.onOpen(" $title")
                actions.addAll(getActions())
            }

            HouseImporter.setImporting(false)

            return actions
        } catch (e: Exception) {
            e.printStackTrace()
            HouseImporter.setImporting(false)
            throw e
        }
    }

    suspend fun setActions(newActions: List<Action>) {
        //Clear existing actions
        MenuUtils.onOpen(title)

        if (MenuUtils.findSlots(MenuItems.NO_ACTIONS).firstOrNull() == null) {
            //There are existing actions, remove them
            while (true) {
                val actionSlots = mutableListOf<Int>()
                for (slotIndex in slots.values) {
                    val slot = MenuUtils.getSlot(slotIndex)
                    if (!slot.hasStack()) break //No more actions
                    actionSlots.add(slotIndex)
                }

                if (actionSlots.isEmpty()) break

                for (slotIndex in actionSlots) {
                    val slot = MenuUtils.getSlot(slotIndex)
                    MenuUtils.interactionClick(slot.id, 1) //Shift + right click to remove action
                    MenuUtils.onOpen(title)
                }
                delay(50)
            }
        }

        //Add new actions
        addActions(newActions)
    }

    suspend fun updateActions(newActions: List<Action>) {
        TODO("Not yet implemented")
    }

    //List of actions to add to the container
    suspend fun addActions(actions: List<Action>) {
        HouseImporter.setImporting(true)

        for (action in actions) {
            //Wait for the "Actions: <name>" or "Edit Actions" to open
            //We do this every iteration to make sure we are right back at the Actions page
            MenuUtils.onOpen(title)

            //Add an action
            MenuUtils.clickItems(MenuItems.ADD_ACTION)
            MenuUtils.onOpen("Add Action")

            //Get the action parameters/properties
            val parameters = action::class.primaryConstructor!!.parameters.toMutableList()
            val actionProperties = action.javaClass.kotlin.memberProperties

            val properties = mutableListOf<KProperty1<Action, *>>()
            for (parm in parameters) {
                properties.add(actionProperties.find { it.name == parm.name } ?: continue)
            }

            //Get the Display Name of the action and add it
            val displayName =
                (action::class.annotations.find { it is ActionDefinition } as ActionDefinition).displayName
            MenuUtils.clickItems(displayName, paginated = true)

            //For change variable, because the holder isn't found in the parameters
            if (action is Action.ChangeVariable) {
                properties.add(0, actionProperties.find { it.name == "holder" } ?: continue)
            }

            //Iterate through parameters
            for ((index, property) in properties.withIndex()) {
                //Get the property and its values
                val value = property.get(action)

                //Make sure we are in the right gui before continuing
                MenuUtils.onOpen("Action Settings", checkIfOpen = false)

                //Place in the gui to click
                val slotIndex = slots[index]!!
                val slot = MenuUtils.getSlot(slotIndex)

                PropertySettings.import(property, slot, value)
            }
            //Make sure we are in the action settings menu before we go back to actions to add another one
            if (properties.isNotEmpty()) {
                MenuUtils.onOpen("Action Settings")
                MenuUtils.clickItems(MenuItems.BACK)
            }
            MenuUtils.onOpen(title)
        }

        HouseImporter.setImporting(false)
    }

    object MenuItems {
        val ADD_ACTION = ItemSelector(
            name = NameExact("Add Action"),
            item = ItemExact(Items.PAPER)
        )
        val BACK = ItemSelector(
            name = NameExact("Go Back"),
            item = ItemExact(Items.ARROW)
        )
        val TOGGLE_ADVANCED_OPERATIONS = ItemSelector(
            name = NameExact("Toggle Advanced Operations"),
            item = ItemExact(Items.COMMAND_BLOCK)
        )
        val NO_ACTIONS = ItemSelector(
            name = NameExact("No Actions!"),
            item = ItemExact(Items.BEDROCK)
        )
    }
}