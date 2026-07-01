package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.SystemsAPI.scaledDelay
import llc.redstone.systemsapi.util.ItemStackUtils.loreLines
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.TextUtils
import llc.redstone.systemsdata.Action
import llc.redstone.systemsdata.ActionDefinition
import llc.redstone.systemsdata.Condition
import llc.redstone.systemsdata.VariableHolder
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

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

        var updateTime = true
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
            for ((prop, param) in properties) {
                val classifier = param.type.classifier as? KClass<*> ?: continue
                if (classifier == List::class) {
                    val value = prop.get(action) as? List<*> ?: continue
                    if (value.isEmpty()) continue
                    if (value.first() is Action) {
                        timeRemaining += estimateImportTime(value as List<Action>)
                    } else if (value.first() is Condition) {
                        timeRemaining += ConditionContainer.estimateImportTime(value as List<Condition>)
                    }
                    continue
                }
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
            return collectActions()
        } finally {
            HouseImporter.setImporting(false)
        }
    }

    private suspend fun collectActions(): List<Action> {
        val actions = mutableListOf<Action>()
        MenuUtils.onOpen(title)

        if (MenuUtils.findSlots(MenuItems.NO_ACTIONS).firstOrNull() != null) return actions

        for (slotIndex in slots.values) {
            val slot = MenuUtils.getSlot(slotIndex)
            if (!slot.hasStack()) break

            parseAction(slot)?.let { actions.add(it) }
        }

        MenuUtils.onOpen(title)
        if (MenuUtils.findSlots(MenuUtils.GlobalMenuItems.NEXT_PAGE).firstOrNull() != null) {
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.NEXT_PAGE)
            MenuUtils.onOpen(" $title", checkIfOpen = false)
            actions.addAll(collectActions())
        }

        return actions
    }

    private suspend fun parseAction(slot: Slot): Action? {
        val item = slot.stack
        val loreLines = item.loreLines(true).filter { it.contains(":") }
        val allLines = item.loreLines(true)
        val name = TextUtils.convertTextToString(item.name, false)

        val actionClass = Action::class.sealedSubclasses.firstOrNull {
            it.findAnnotations(ActionDefinition::class).any { ann -> ann.displayName == name }
        } ?: return null

        return buildAction(actionClass, loreLines, allLines, slot, 0)
    }

    private suspend fun buildAction(
        actionClass: KClass<out Action>,
        loreLines: List<String>,
        allLines: List<String>,
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
            var colorValue = loreLines.getOrNull(index + indexOffset)
                ?.split(": ")?.drop(1)?.joinToString(": ")
                ?.replaceFirst("&f", "") ?: continue
            var value = colorValue.replace(Regex("&[0-9a-fk-or]"), "")

            if (value.isEmpty()) {
                //Dont question this :)
                //Used to catch when there is no actions or conditionals
                val index = allLines.indexOf(loreLines.getOrNull(index + indexOffset))
                colorValue = allLines.getOrNull(index + 1)
                    ?.split(" - ")?.drop(1)?.joinToString(" - ")
                    ?.replaceFirst("&f", "") ?: continue
                value = colorValue.replace(Regex("&[0-9a-fk-or]"), "")
            }

            val returnValue =
                PropertySettings.export(title, prop, slot, slots[index + indexOffset]!!, value, colorValue)

            // Handle VariableHolder by switching to the appropriate subclass
            if (returnValue is VariableHolder) {
                val newClass = when (returnValue) {
                    VariableHolder.Player -> Action.PlayerVariable::class
                    VariableHolder.Global -> Action.GlobalVariable::class
                    VariableHolder.Team -> Action.TeamVariable::class
                }
                return buildAction(newClass, loreLines, allLines, slot, 1)
            }

            args[param] = returnValue
        }


        return if (args.size != constructor.parameters.size) {
            actionClass.constructors.firstOrNull { it.parameters.size == constructor.parameters.size }?.callBy(args)
                ?: constructor.callBy(args)
        } else {
            constructor.isAccessible = true
            constructor.callBy(args)
        }
    }

    suspend fun setActions(newActions: List<Action>) {
        HouseImporter.setImporting(true)
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

                if (MenuUtils.findSlots(MenuItems.NO_ACTIONS).firstOrNull() != null) break

                MenuUtils.packetClick(10, 1)
                MenuUtils.onCurrentScreenUpdate()
            }
        }

        //Add new actions
        addActions(newActions)
    }

    suspend fun updateActions(newActions: List<Action>) {
        HouseImporter.setImporting(true)
        try {
            val current = collectActions().toMutableList()

            if (newActions.isEmpty()) {
                while (current.isNotEmpty()) {
                    deleteActionAt(current.lastIndex)
                    current.removeAt(current.lastIndex)
                }
                return
            }

            if (current.isEmpty()) {
                addActions(newActions)
                return
            }

            for (targetIndex in newActions.indices) {
                val desired = newActions[targetIndex]
                val currentAtTarget = current.getOrNull(targetIndex)

                if (actionsEqual(currentAtTarget, desired)) continue

                if (desired is Action.CustomAction) {
                    replaceActionAt(targetIndex, desired, current)
                    continue
                }

                val exactMatch = current.indexOfFirst { actionsEqual(it, desired) }
                if (exactMatch >= 0) {
                    moveAction(exactMatch, targetIndex)
                    current.moveElement(exactMatch, targetIndex)
                    continue
                }

                val sameTypeMatch = current.withIndex().indexOfFirst { (index, item) ->
                    index != targetIndex && item::class == desired::class
                }
                if (sameTypeMatch >= 0) {
                    moveAction(sameTypeMatch, targetIndex)
                    current.moveElement(sameTypeMatch, targetIndex)
                    if (!actionsEqual(current[targetIndex], desired)) {
                        editActionAt(targetIndex, desired)
                    }
                    current[targetIndex] = desired
                    continue
                }

                if (currentAtTarget != null && currentAtTarget::class == desired::class) {
                    editActionAt(targetIndex, desired)
                    current[targetIndex] = desired
                    continue
                }

                replaceActionAt(targetIndex, desired, current)
            }

            while (current.size > newActions.size) {
                deleteActionAt(current.lastIndex)
                current.removeAt(current.lastIndex)
            }
        } finally {
            HouseImporter.setImporting(false)
        }
    }

    private fun MutableList<Action>.moveElement(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val item = removeAt(fromIndex)
        val insertIndex = if (toIndex > fromIndex) toIndex - 1 else toIndex
        add(insertIndex, item)
    }

    var actionNavigationTime = 400L

    private fun getActionProperties(action: Action): List<KProperty1<Action, *>> {
        val parameters = action::class.primaryConstructor!!.parameters
        val actionProperties = action.javaClass.kotlin.memberProperties
        val properties = mutableListOf<KProperty1<Action, *>>()
        for (parm in parameters) {
            actionProperties.find { it.name == parm.name }?.let { properties.add(it) }
        }
        if (action is Action.ChangeVariable) {
            actionProperties.find { it.name == "holder" }?.let { properties.add(0, it) }
        }
        return properties
    }

    private suspend fun configureActionProperties(action: Action, properties: List<KProperty1<Action, *>>) {
        for ((index, property) in properties.withIndex()) {
            MenuUtils.onOpen("Action Settings")
            val slot = MenuUtils.getSlot(slots[index]!!)
            PropertySettings.import(property, slot, property.get(action))
        }
    }

    private suspend fun navigateToActionIndex(index: Int) {
        val page = index / slots.size
        MenuUtils.onOpen(title)
        while (MenuUtils.findSlots(MenuUtils.GlobalMenuItems.PREVIOUS_PAGE).firstOrNull() != null) {
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.PREVIOUS_PAGE)
            MenuUtils.onOpen(" $title", checkIfOpen = false)
        }
        repeat(page) {
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.NEXT_PAGE)
            MenuUtils.onOpen(" $title", checkIfOpen = false)
        }
    }

    private suspend fun deleteActionAt(index: Int) {
        navigateToActionIndex(index)
        MenuUtils.packetClick(slots[index % slots.size]!!, 1)
        MenuUtils.onCurrentScreenUpdate()
    }

    private suspend fun moveActionForward(index: Int) {
        navigateToActionIndex(index)
        MenuUtils.shiftPacketClick(slots[index % slots.size]!!, 0)
        MenuUtils.onCurrentScreenUpdate()
    }

    private suspend fun moveActionBackward(index: Int) {
        navigateToActionIndex(index)
        MenuUtils.shiftPacketClick(slots[index % slots.size]!!, 1)
        MenuUtils.onCurrentScreenUpdate()
    }

    private suspend fun moveAction(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        var currentIndex = fromIndex
        while (currentIndex > toIndex) {
            moveActionForward(currentIndex)
            currentIndex--
        }
        while (currentIndex < toIndex) {
            moveActionBackward(currentIndex)
            currentIndex++
        }
    }

    private suspend fun replaceActionAt(index: Int, action: Action, current: MutableList<Action>) {
        if (index < current.size) {
            deleteActionAt(index)
            current.removeAt(index)
        }
        MenuUtils.onOpen(title)
        createAction(action)
        current.add(action)
        val lastIndex = current.lastIndex
        if (lastIndex != index) {
            moveAction(lastIndex, index)
            current.moveElement(lastIndex, index)
        }
    }

    private suspend fun editActionAt(index: Int, action: Action) {
        navigateToActionIndex(index)
        MenuUtils.packetClick(slots[index % slots.size]!!)
        MenuUtils.onOpen("Action Settings")
        val properties = getActionProperties(action)
        configureActionProperties(action, properties)
        if (properties.isNotEmpty()) {
            MenuUtils.onOpen("Action Settings")
            MenuUtils.clickItems(MenuItems.BACK)
        }
        MenuUtils.onOpen(title)
    }

    private suspend fun createAction(action: Action) {
        if (action is Action.CustomAction) {
            action.function(action.parameters)
            return
        }

        val startA = System.currentTimeMillis()
        MenuUtils.clickItems(MenuItems.ADD_ACTION)
        MenuUtils.onOpen("Add Action")

        val displayName =
            (action::class.annotations.find { it is ActionDefinition } as ActionDefinition).displayName
        MenuUtils.clickItems(displayName, paginated = true)

        val properties = getActionProperties(action)
        val endA = System.currentTimeMillis()

        configureActionProperties(action, properties)

        val startB = System.currentTimeMillis()
        if (properties.isNotEmpty()) {
            MenuUtils.onOpen("Action Settings")
            MenuUtils.clickItems(MenuItems.BACK)
        }
        MenuUtils.onOpen(title)
        val endB = System.currentTimeMillis()

        actionNavigationTime = (endA - startA) + (endB - startB)
    }

    private fun actionsEqual(a: Action?, b: Action?): Boolean {
        if (a === b) return true
        if (a == null || b == null) return false
        if (a is Action.CustomAction || b is Action.CustomAction) return false
        return a == b
    }

    //List of actions to add to the container
    suspend fun addActions(actions: List<Action>) {
        if (actions.isEmpty()) return

        HouseImporter.setImporting(true)

        var time = estimateImportTime(actions)
        var startTime = (HouseImporter.getTimeRemaining()?.times(1000) ?: time).toLong()
        if (updateTime) {
            HouseImporter.setTimeRemaining(time)
        }
        println("Estimated import time: ${time}ms ${HouseImporter.getTimeRemaining()}s")

        for ((index, action) in actions.withIndex()) {
            val estimatedTime = estimateImportTime(actions.subList(index, actions.size))
            if (updateTime) {
                HouseImporter.setTimeRemaining(estimatedTime)
            } else {
                HouseImporter.setTimeRemaining(startTime - (time - estimatedTime))
            }
            println("Updated estimated time remaining: ${estimatedTime}ms ${HouseImporter.getTimeRemaining()}s")

            MenuUtils.onOpen(title)
            createAction(action)
        }

        HouseImporter.setImporting(false)
    }

    suspend fun copyToHousingClipboard() {
        MenuUtils.onOpen(title)
        MenuUtils.packetClick(51, 1)
        scaledDelay(1.0)
        if (MenuUtils.findSlots(MenuItems.PASTE_ACTIONS).firstOrNull() == null) {
            error("Failed to copy actions to clipboard")
        }
    }

    suspend fun pasteFromHousingClipboard() {
        MenuUtils.onOpen(title)
        if (MenuUtils.findSlots(MenuItems.PASTE_ACTIONS).firstOrNull() == null) {
            error("Clipboard is empty or does not contain valid actions")
        }
        MenuUtils.packetClick(51, 0)
        scaledDelay(1.0)
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
        val PASTE_ACTIONS = ItemSelector(
            name = NameExact("Paste Actions"),
            item = ItemExact(Items.BOOK)
        )
    }
}