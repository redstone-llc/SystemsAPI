package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.util.ItemStackUtils.getLoreLineMatchesOrNull
import llc.redstone.systemsapi.util.ItemStackUtils.loreLines
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.TextUtils
import llc.redstone.systemsdata.Action
import llc.redstone.systemsdata.Condition
import llc.redstone.systemsdata.DisplayName
import llc.redstone.systemsdata.VariableHolder
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

object ConditionContainer {
    private const val TITLE = "Edit Conditions"

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

    fun estimateImportTime(conditions: List<Condition>): Long {
        var timeRemaining = 0L
        for (action in conditions) {
            val conditionClass = action::class
            val constructor = conditionClass.primaryConstructor ?: continue
            val properties = constructor.parameters.mapNotNull { param ->
                val prop = conditionClass.memberProperties.find { it.name == param.name } as? KProperty1<Action, *>
                prop?.let { it to param }
            }
            for ((_, param) in properties) {
                val classifier = param.type.classifier as? KClass<*> ?: continue
                val returnValue = PropertySettings.importTimes.getOrDefault(classifier, 400L)
                timeRemaining += returnValue
            }
        }
        return timeRemaining
    }

    suspend fun exportConditions(): List<Condition> {
        MenuUtils.onOpen(TITLE)
        return collectConditions()
    }

    private suspend fun collectConditions(): List<Condition> {
        val conditions = mutableListOf<Condition>()
        MenuUtils.onOpen(TITLE)

        if (MenuUtils.findSlots(MenuItems.NO_CONDITIONS).firstOrNull() != null) return conditions

        for (slotIndex in slots.values) {
            val slot = MenuUtils.getSlot(slotIndex)
            if (!slot.hasStack()) break

            parseCondition(slot)?.let { conditions.add(it) }
        }

        MenuUtils.onOpen(TITLE)
        if (MenuUtils.findSlots(MenuUtils.GlobalMenuItems.NEXT_PAGE).firstOrNull() != null) {
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.NEXT_PAGE)
            MenuUtils.onOpen(" $TITLE", checkIfOpen = false)
            conditions.addAll(collectConditions())
        }

        return conditions
    }

    private suspend fun parseCondition(slot: Slot): Condition? {
        val item = slot.stack
        val loreLines = item.loreLines(true).filter { it.contains(": ") }
        val name = TextUtils.convertTextToString(item.name, false)

        var conditionClass = Condition::class.sealedSubclasses.firstOrNull {
            it.findAnnotations(DisplayName::class).any { ann -> ann.value == name }
        } ?: return null

        var constructor = conditionClass.primaryConstructor!!
        var conditionProperties = conditionClass.memberProperties
        var properties = mutableListOf<Pair<KProperty1<Condition, *>, KParameter?>>()

        for (parm in constructor.parameters) {
            properties.add(conditionProperties.find { it.name == parm.name } as KProperty1<Condition, *> to parm)
        }

        suspend fun args(indexAddition: Int = 1): MutableMap<KParameter, Any?> {
            val args = mutableMapOf<KParameter, Any?>()
            properties.forEachIndexed { index, (prop, param) ->
                if (param == null) return@forEachIndexed
                val colorValue =
                    (loreLines.getOrNull(index + indexAddition - 1)?.split(": ")?.drop(1)?.joinToString(": ")
                        ?: return@forEachIndexed).replaceFirst("&f", "")
                val value = colorValue.replace(Regex("&[0-9a-fk-or]"), "")

                val returnValue = PropertySettings.export(
                    TITLE,
                    prop,
                    slot,
                    slots[index + indexAddition]!!,
                    value,
                    colorValue
                )

                if (returnValue is VariableHolder) {
                    conditionClass = when (returnValue) {
                        VariableHolder.Player -> Condition.PlayerVariableRequirement::class
                        VariableHolder.Global -> Condition.GlobalVariableRequirement::class
                        VariableHolder.Team -> Condition.TeamVariableRequirement::class
                    }
                    constructor = conditionClass.primaryConstructor!!
                    conditionProperties = conditionClass.memberProperties
                    properties = mutableListOf()
                    for (parm in constructor.parameters) {
                        properties.add(
                            conditionProperties.find { it.name == parm.name } as KProperty1<Condition, *> to parm
                        )
                    }
                    return args(2)
                }

                args[param] = returnValue
            }
            return args
        }

        val args = args()

        var conditionInstance: Condition? = null
        if (args.size != constructor.parameters.size) {
            conditionClass.constructors.forEach { newCon ->
                if (constructor.parameters.size == newCon.parameters.size) {
                    conditionInstance = newCon.callBy(args)
                }
            }
        } else {
            conditionInstance = constructor.callBy(args)
        }

        if (conditionInstance == null) return null

        if (slot.stack.getLoreLineMatchesOrNull(false) { it == "Inverted" } != null) {
            conditionInstance.inverted = true
        }

        return conditionInstance
    }

    suspend fun updateConditions(newConditions: List<Condition>) {
        val current = collectConditions().toMutableList()

        if (newConditions.isEmpty()) {
            while (current.isNotEmpty()) {
                deleteConditionAt(current.lastIndex)
                current.removeAt(current.lastIndex)
            }
            return
        }

        if (current.isEmpty()) {
            addConditions(newConditions)
            return
        }

        for (targetIndex in newConditions.indices) {
            val desired = newConditions[targetIndex]
            val currentAtTarget = current.getOrNull(targetIndex)

            if (conditionsEqual(currentAtTarget, desired)) continue

            val exactMatch = current.indexOfFirst { conditionsEqual(it, desired) }
            if (exactMatch >= 0) {
                moveCondition(exactMatch, targetIndex)
                current.moveElement(exactMatch, targetIndex)
                continue
            }

            val sameTypeMatch = current.withIndex().indexOfFirst { (index, item) ->
                index != targetIndex && item::class == desired::class
            }
            if (sameTypeMatch >= 0) {
                moveCondition(sameTypeMatch, targetIndex)
                current.moveElement(sameTypeMatch, targetIndex)
                if (!conditionsEqual(current[targetIndex], desired)) {
                    editConditionAt(targetIndex, desired)
                }
                current[targetIndex] = desired
                continue
            }

            if (currentAtTarget != null && currentAtTarget::class == desired::class) {
                editConditionAt(targetIndex, desired)
                current[targetIndex] = desired
                continue
            }

            replaceConditionAt(targetIndex, desired, current)
        }

        while (current.size > newConditions.size) {
            deleteConditionAt(current.lastIndex)
            current.removeAt(current.lastIndex)
        }
    }

    suspend fun addConditions(conditions: List<Condition>) {
        for (condition in conditions) {
            MenuUtils.onOpen(TITLE)
            createCondition(condition)
        }
    }

    private fun getConditionProperties(condition: Condition): List<KProperty1<Condition, *>> {
        val parameters = condition::class.primaryConstructor!!.parameters
        val conditionProperties = condition.javaClass.kotlin.memberProperties
        val properties = mutableListOf<KProperty1<Condition, *>>()
        for (parm in parameters) {
            conditionProperties.find { it.name == parm.name }?.let { properties.add(it) }
        }
        val inverted = conditionProperties.find { it.name == "inverted" } ?: return emptyList()
        properties.add(0, inverted)
        if (condition is Condition.VariableRequirement) {
            conditionProperties.find { it.name == "holder" }?.let { properties.add(1, it) }
        }
        return properties
    }

    private suspend fun configureConditionProperties(
        condition: Condition,
        properties: List<KProperty1<Condition, *>>
    ) {
        for ((index, property) in properties.withIndex()) {
            MenuUtils.onOpen("Settings")
            val slot = MenuUtils.getSlot(slots[index]!!)
            PropertySettings.import(property, slot, property.get(condition))
        }
    }

    private suspend fun navigateToConditionIndex(index: Int) {
        val page = index / slots.size
        MenuUtils.onOpen(TITLE)
        while (MenuUtils.findSlots(MenuUtils.GlobalMenuItems.PREVIOUS_PAGE).firstOrNull() != null) {
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.PREVIOUS_PAGE)
            MenuUtils.onOpen(" $TITLE", checkIfOpen = false)
        }
        repeat(page) {
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.NEXT_PAGE)
            MenuUtils.onOpen(" $TITLE", checkIfOpen = false)
        }
    }

    private suspend fun deleteConditionAt(index: Int) {
        navigateToConditionIndex(index)
        MenuUtils.packetClick(slots[index % slots.size]!!, 1)
        MenuUtils.onCurrentScreenUpdate()
    }

    private suspend fun moveConditionForward(index: Int) {
        navigateToConditionIndex(index)
        MenuUtils.shiftPacketClick(slots[index % slots.size]!!, 0)
        MenuUtils.onCurrentScreenUpdate()
    }

    private suspend fun moveConditionBackward(index: Int) {
        navigateToConditionIndex(index)
        MenuUtils.shiftPacketClick(slots[index % slots.size]!!, 1)
        MenuUtils.onCurrentScreenUpdate()
    }

    private suspend fun moveCondition(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        var currentIndex = fromIndex
        while (currentIndex > toIndex) {
            moveConditionForward(currentIndex)
            currentIndex--
        }
        while (currentIndex < toIndex) {
            moveConditionBackward(currentIndex)
            currentIndex++
        }
    }

    private suspend fun replaceConditionAt(index: Int, condition: Condition, current: MutableList<Condition>) {
        if (index < current.size) {
            deleteConditionAt(index)
            current.removeAt(index)
        }
        MenuUtils.onOpen(TITLE)
        createCondition(condition)
        current.add(condition)
        val lastIndex = current.lastIndex
        if (lastIndex != index) {
            moveCondition(lastIndex, index)
            current.moveElement(lastIndex, index)
        }
    }

    private suspend fun editConditionAt(index: Int, condition: Condition) {
        navigateToConditionIndex(index)
        MenuUtils.packetClick(slots[index % slots.size]!!)
        MenuUtils.onOpen("Settings")
        val properties = getConditionProperties(condition)
        configureConditionProperties(condition, properties)
        if (properties.isNotEmpty()) {
            MenuUtils.onOpen("Settings")
            MenuUtils.clickItems(MenuItems.BACK)
        }
        MenuUtils.onOpen(TITLE)
    }

    private suspend fun createCondition(condition: Condition) {
        MenuUtils.clickItems(MenuItems.ADD_CONDITION)
        MenuUtils.onOpen("Add Condition")

        val displayName = (condition::class.annotations.find { it is DisplayName } as DisplayName).value
        MenuUtils.clickItems(displayName, paginated = true)

        val properties = getConditionProperties(condition)
        configureConditionProperties(condition, properties)

        if (properties.isNotEmpty()) {
            MenuUtils.onOpen("Settings")
            MenuUtils.clickItems(MenuItems.BACK)
        }
        MenuUtils.onOpen(TITLE)
    }

    private fun conditionsEqual(a: Condition?, b: Condition?): Boolean {
        if (a === b) return true
        if (a == null || b == null) return false
        return a == b
    }

    private fun MutableList<Condition>.moveElement(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val item = removeAt(fromIndex)
        val insertIndex = if (toIndex > fromIndex) toIndex - 1 else toIndex
        add(insertIndex, item)
    }

    object MenuItems {
        val ADD_CONDITION = ItemSelector(
            name = NameExact("Add Condition"),
            item = ItemExact(Items.PAPER)
        )
        val BACK = ItemSelector(
            name = NameExact("Go Back"),
            item = ItemExact(Items.ARROW)
        )
        val NO_CONDITIONS = ItemSelector(
            name = NameExact("No Conditions!"),
            item = ItemExact(Items.BEDROCK)
        )
    }
}
