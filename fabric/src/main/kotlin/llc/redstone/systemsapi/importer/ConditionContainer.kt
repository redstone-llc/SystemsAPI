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
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

object ConditionContainer {
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

    //List of conditions to add to the container
    suspend fun addConditions(actions: List<Condition>) {
        for (condition in actions) {
            //Wait for the "Edit Conditions" to open
            //We do this every iteration to make sure we are right back at the Conditions page
            MenuUtils.onOpen("Edit Conditions")

            //Add a condition
            MenuUtils.clickItems(MenuItems.ADD_CONDITION)
            MenuUtils.onOpen("Add Condition")

            //Get the condition parameters/properties
            val parameters = condition::class.primaryConstructor!!.parameters.toMutableList()
            val conditionProperties = condition.javaClass.kotlin.memberProperties

            val properties = mutableListOf<KProperty1<Condition, *>>()
            for (parm in parameters) {
                properties.add(conditionProperties.find { it.name == parm.name } ?: continue)
            }

            //Get the Display Name of the condition and add it
            val displayName = (condition::class.annotations.find { it is DisplayName } as DisplayName).value
            MenuUtils.clickItems(displayName, paginated = true)

            //Inverted
            properties.add(0, conditionProperties.find { it.name == "inverted" } ?: continue)

            //For require variable, because the holder isn't found in the parameters
            if (condition is Condition.VariableRequirement) {
                properties.add(1, conditionProperties.find { it.name == "holder" } ?: continue)
            }

            //Iterate through parameters
            for ((index, property) in properties.withIndex()) {
                //Get the property and its values
                val value = property.get(condition)

                //Make sure we are in the right gui before continuing
                MenuUtils.onOpen("Settings")

                //Place in the gui to click
                val slotIndex = slots[index]!!
                val slot = MenuUtils.getSlot(slotIndex)

                PropertySettings.import(property, slot, value)
            }
            //Make sure we are in the condition settings menu before we go back to actions to add another one
            if (properties.isNotEmpty()) {
                MenuUtils.onOpen("Settings")
                MenuUtils.clickItems(MenuItems.BACK)
            }
            MenuUtils.onOpen("Edit Conditions")
        }
    }

    suspend fun exportConditions(): List<Condition> {
        val conditions = mutableListOf<Condition>()

        MenuUtils.onOpen("Edit Conditions")

        if (MenuUtils.findSlots(MenuItems.NO_CONDITIONS).firstOrNull() != null) return conditions

        for (slotIndex in slots.values) {
            val slot = MenuUtils.getSlot(slotIndex)
            if (!slot.hasStack()) break //No more actions

            val item = slot.stack
            val loreLines = item.loreLines(true).filter {
                it.contains(": ") //Only care about lines with properties
            }


            val name = TextUtils.convertTextToString(item.name, false)
            var conditionClass = Condition::class.sealedSubclasses.firstOrNull { it.findAnnotations(DisplayName::class).any { ann -> ann.value == name } }
                ?: continue

            var constructor = conditionClass.primaryConstructor!!
            var parameters = constructor.parameters.toMutableList()
            var conditionProperties = conditionClass.memberProperties
            var properties = mutableListOf<Pair<KProperty1<Condition, *>, KParameter?>>()

            for (parm in parameters) {
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
                        "Edit Conditions",
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
                        parameters = constructor.parameters.toMutableList()
                        conditionProperties = conditionClass.memberProperties
                        properties = mutableListOf()
                        for (parm in parameters) {
                            properties.add(conditionProperties.find { it.name == parm.name } as KProperty1<Condition, *> to parm)
                        }
                        // I hate recursion, but I think this is the cleanest way to handle it
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

            if (conditionInstance == null) continue

            if (slot.stack.getLoreLineMatchesOrNull(false) {it == "Inverted"} != null) {
                conditionInstance.inverted = true
            }

            conditions.add(conditionInstance)
        }

        if (MenuUtils.findSlots(MenuUtils.GlobalMenuItems.NEXT_PAGE).firstOrNull() != null) {
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.NEXT_PAGE)
            MenuUtils.onOpen("Edit Conditions", checkIfOpen = false)
            conditions.addAll(exportConditions())
        }

        return conditions
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