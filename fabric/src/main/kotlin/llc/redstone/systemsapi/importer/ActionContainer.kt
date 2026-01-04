package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.SystemsAPI.scaledDelay
import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.data.ActionDefinition
import llc.redstone.systemsapi.data.VariableHolder
import llc.redstone.systemsapi.util.ItemStackUtils.loreLines
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import kotlin.reflect.KClass
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

    fun estimateImportTime(actions: List<Action>): Long {
        var timeRemaining = 0L
        for (action in actions) {
            val actionClass = action::class
            val constructor = actionClass.primaryConstructor ?: continue
            val properties = constructor.parameters.mapNotNull { param ->
                val prop = actionClass.memberProperties.find { it.name == param.name } as? KProperty1<Action, *>
                prop?.let { it to param }
            }
            for ((_, param) in properties) {
                val classifier = param.type.classifier as? KClass<*> ?: continue
                val returnValue = PropertySettings.importTimes.getOrDefault(classifier, 400L)
                timeRemaining += returnValue
            }
            timeRemaining += actionNavigationTime
        }
        return timeRemaining
    }

    suspend fun getActions(): List<Action> {
        HouseImporter.setImporting(true)
        try {
            val actions = mutableListOf<Action>()
            MenuUtils.onOpen(title)

            if (MenuUtils.findSlots(MenuItems.NO_ACTIONS).firstOrNull() != null) return actions

            for (slotIndex in slots.values) {
                val slot = MenuUtils.getSlot(slotIndex)
                if (!slot.hasStack()) break

                parseAction(slot)?.let { actions.add(it) }
            }

            // Handle pagination
            MenuUtils.onOpen(title)
            if (MenuUtils.findSlots(MenuUtils.GlobalMenuItems.NEXT_PAGE).firstOrNull() != null) {
                MenuUtils.clickItems(MenuUtils.GlobalMenuItems.NEXT_PAGE)
                MenuUtils.onOpen(" $title")
                actions.addAll(getActions())
            }

            return actions
        } finally {
            HouseImporter.setImporting(false)
        }
    }

    private suspend fun parseAction(slot: Slot): Action? {
        val item = slot.stack
        val loreLines = item.loreLines(true).filter { it.contains(":") }
        val name = TextUtils.convertTextToString(item.name, false)

        val actionClass = Action::class.sealedSubclasses.firstOrNull {
            it.findAnnotations(ActionDefinition::class).any { ann -> ann.displayName == name }
        } ?: return null

        return buildAction(actionClass, loreLines, slot, 0)
    }

    private suspend fun buildAction(
        actionClass: KClass<out Action>,
        loreLines: List<String>,
        slot: Slot,
        indexOffset: Int
    ): Action? {
        val constructor = actionClass.primaryConstructor ?: return null
        val properties = constructor.parameters.mapNotNull { param ->
            val prop = actionClass.memberProperties.find { it.name == param.name } as? KProperty1<Action, *>
            prop?.let { it to param }
        }

        val args = mutableMapOf<KParameter, Any?>()

        for ((index, pair) in properties.withIndex()) {
            val (prop, param) = pair
            val colorValue = loreLines.getOrNull(index + indexOffset)
                ?.split(": ")?.drop(1)?.joinToString(": ")
                ?.replaceFirst("&f", "") ?: continue
            val value = colorValue.replace(Regex("&[0-9a-fk-or]"), "")

            val returnValue = PropertySettings.export(title, prop, slot, slots[index + indexOffset]!!, value, colorValue)

            // Handle VariableHolder by switching to the appropriate subclass
            if (returnValue is VariableHolder) {
                val newClass = when (returnValue) {
                    VariableHolder.Player -> Action.PlayerVariable::class
                    VariableHolder.Global -> Action.GlobalVariable::class
                    VariableHolder.Team -> Action.TeamVariable::class
                }
                return buildAction(newClass, loreLines, slot, 1)
            }

            args[param] = returnValue
        }

        return if (args.size != constructor.parameters.size) {
            actionClass.constructors.firstOrNull { it.parameters.size == constructor.parameters.size }?.callBy(args)
        } else {
            constructor.callBy(args)
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
                    MenuUtils.interactionClick(slot.id, 1)
                    MenuUtils.onOpen(title)
                }
                scaledDelay()
            }
        }

        //Add new actions
        addActions(newActions)
    }

    suspend fun updateActions(newActions: List<Action>) {
        TODO("Not yet implemented")
    }

    var actionNavigationTime = 400L

    //List of actions to add to the container
    suspend fun addActions(actions: List<Action>) {
        if (actions.isEmpty()) return

        HouseImporter.setImporting(true)

        HouseImporter.setTimeRemaining(estimateImportTime(actions))

        for ((index, action) in actions.withIndex()) {
            HouseImporter.setTimeRemaining(estimateImportTime(actions.subList(index, actions.size)))

            val startA = System.currentTimeMillis()
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

            val endA = System.currentTimeMillis()

            //Iterate through parameters
            for ((index, property) in properties.withIndex()) {
                //Get the property and its values
                val value = property.get(action)

                //Make sure we are in the right gui before continuing
                MenuUtils.onOpen("Action Settings")

                //Place in the gui to click
                val slotIndex = slots[index]!!
                val slot = MenuUtils.getSlot(slotIndex)

                PropertySettings.import(property, slot, value)
            }
            //Make sure we are in the action settings menu before we go back to actions to add another one
            var startB = System.currentTimeMillis()
            if (properties.isNotEmpty()) {
                MenuUtils.onOpen("Action Settings")
                MenuUtils.clickItems(MenuItems.BACK)
            }
            MenuUtils.onOpen(title)

            var endB = System.currentTimeMillis()

            actionNavigationTime = ((endA - startA) + (endB - startB))
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