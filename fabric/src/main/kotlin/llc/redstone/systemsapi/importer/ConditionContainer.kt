package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.data.Condition
import llc.redstone.systemsapi.data.DisplayName
import llc.redstone.systemsapi.data.VariableHolder
import llc.redstone.systemsapi.util.ItemUtils.loreLines
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.MenuUtils.Target
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Items
import java.util.function.Consumer
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

//The title of the actions gui, either Actions: <name> or Edit Actions
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
    )

    //List of actions to add to the container
    suspend fun addConditions(actions: List<Condition>) {
        for (condition in actions) {
            //Wait for the "Edit Conditions" to open
            //We do this every iteration to make sure we are right back at the Conditions page
            MenuUtils.onOpen("Edit Conditions")

            //Add a condition
            MenuUtils.clickMenuSlot(MenuItems.ADD_CONDITION)
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
            MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, displayName)))

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
                val gui = MC.currentScreen as? GenericContainerScreen ?: return

                //Place in the gui to click
                val slotIndex = slots[index]!!
                val slot = gui.screenHandler.getSlot(slotIndex)

                PropertySettings.import(property, slot, value)
            }
            //Make sure we are in the condition settings menu before we go back to actions to add another one
            if (properties.isNotEmpty()) {
                MenuUtils.onOpen("Settings")
                MenuUtils.clickMenuSlot(MenuItems.BACK)
            }
            MenuUtils.onOpen("Edit Conditions")
        }
    }

    suspend fun exportConditions(): List<Condition> {
        val conditions = mutableListOf<Condition>()

        MenuUtils.onOpen("Edit Conditions")
        val gui = MC.currentScreen as? GenericContainerScreen
            ?: throw ClassCastException("Expected GenericContainerScreen but found ${MC.currentScreen?.javaClass?.name}")

        if (MenuUtils.findSlot(MenuItems.NO_CONDITIONS, true) != null) return conditions

        for (slotIndex in slots.values) {
            val slot = gui.screenHandler.getSlot(slotIndex)
            if (!slot.hasStack()) break //No more actions

            val item = slot.stack
            val loreLines = item.loreLines(true).filter {
                it.contains(": ") //Only care about lines with properties
            }

            val name = TextUtils.convertTextToString(item.name, false)
            var conditionClass = Condition::class.sealedSubclasses.firstOrNull() { it.findAnnotations(DisplayName::class).any { ann -> ann.value == name } }
                ?: continue

            var constructor = conditionClass.primaryConstructor!!
            var parameters = constructor.parameters.toMutableList()
            var conditionProperties = conditionClass.memberProperties
            var properties = mutableListOf<Pair<KProperty1<Condition, *>, KParameter?>>()
            val toRun = mutableListOf<Consumer<Condition>>()

            for (parm in parameters) {
                properties.add(conditionProperties.find { it.name == parm.name } as KProperty1<Condition, *> to parm)
            }

            suspend fun args(indexAddition: Int = 1): MutableMap<KParameter, Any?> {
                val args = mutableMapOf<KParameter, Any?>()
                properties.forEachIndexed { index, (prop, param) ->
                    val colorValue = loreLines.getOrNull(index + indexAddition - 1)?.split(": ")?.drop(1)?.joinToString(": ") ?: return@forEachIndexed
                    val value = colorValue.replace(Regex("&[0-9a-fk-or]"), "")

                    val returnValue = PropertySettings.export("Edit Conditions", prop, slot, slots[index + indexAddition]!!, value, colorValue)
                    if (param == null) run {
                        if (prop is KMutableProperty1<Condition, *>) {
                            toRun.add (Consumer { condition ->
                                prop.setter.call(condition, returnValue)
                            })
                        }
                        return@forEachIndexed
                    }
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

            toRun.forEach { it.accept(conditionInstance) }
            conditions.add(conditionInstance)
        }

        return conditions
    }

    object MenuItems {
        val ADD_CONDITION = MenuSlot(Items.PAPER, "Add Condition")
        val BACK = MenuSlot(Items.ARROW, "Go Back")
        val TOGGLE_ADVANCED_OPERATIONS = MenuSlot(Items.COMMAND_BLOCK, "Toggle Advanced Operations")
        val NO_CONDITIONS = MenuSlot(Items.BEDROCK, "No Conditions!")
    }
}