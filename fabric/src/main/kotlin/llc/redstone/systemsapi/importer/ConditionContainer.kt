package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.data.Condition
import llc.redstone.systemsapi.data.CustomKey
import llc.redstone.systemsapi.data.DisplayName
import llc.redstone.systemsapi.data.ItemStack
import llc.redstone.systemsapi.data.Keyed
import llc.redstone.systemsapi.data.KeyedCycle
import llc.redstone.systemsapi.data.KeyedLabeled
import llc.redstone.systemsapi.data.Pagination
import llc.redstone.systemsapi.data.StatValue
import llc.redstone.systemsapi.util.ItemUtils
import llc.redstone.systemsapi.util.ItemUtils.giveItem
import llc.redstone.systemsapi.util.ItemUtils.loreLine
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.Target
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Items
import kotlin.math.abs
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

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

            println(properties.joinToString(", ") { it.name })

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

                //All other properties
                when (property.returnType.classifier) {
                    String::class, Int::class, Double::class -> {
                        MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                        val pagination = property.annotations.find { it is Pagination }
                        if (pagination != null) {
                            MenuUtils.onOpen("Select Option")
                            MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, value.toString())))
                            continue
                        }
                        TextUtils.input(value.toString(), 100L)
                    }

                    StatValue::class -> {
                        MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                        TextUtils.input(value.toString(), 100L)
                    }

                    ItemStack::class -> {
                        MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                        MenuUtils.onOpen("Select an Item")

                        val nbt = (value as ItemStack).nbt ?: error("[Item action] ItemStack has no NBT data")
                        val item = ItemUtils.createFromNBT(nbt)
                        val player = MC.player ?: error("[Item action] Could not get the player")
                        val oldStack = player.inventory.getStack(0)
                        item.giveItem(26)
                        MenuUtils.clickPlayerSlot(26)
                        oldStack.giveItem(26)
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
                }

                //Enum condition properties
                if (property.returnType.isSubtypeOf(Keyed::class.starProjectedType)) {
                    val keyed = value as Keyed

                    val entries = value.javaClass.enumConstants

                    if (keyed is KeyedCycle) {
                        val holderIndex = entries.indexOf(keyed) + 1
                        val stack = slot.stack

                        val current = stack.loreLine(true) { str -> str.contains("➠") } ?: continue
                        val currentHolder = entries.find { current.contains(it.key) }
                        val currentIndex = if (currentHolder != null) entries.indexOf(currentHolder) + 1 else 0
                        if (currentHolder != keyed) {
                            val clicks = holderIndex - currentIndex
                            repeat(abs(clicks)) {
                                MenuUtils.clickMenuTargets(Target(MenuSlot(null, null, slotIndex), if (clicks > 0) 0 else 1))
                                delay(50) //Small delay to allow the menu to update
                            }
                        }

                        continue
                    }

                    if (slot.stack.loreLine(false, filter = { str -> str == value.key }) == null) {
                        MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                        MenuUtils.onOpen("Select Option")
                        if (keyed is KeyedLabeled) {
                            MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, keyed.label)))
                        } else {
                            MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, keyed.key)))
                        }

                        if (keyed::class.annotations.find { it is CustomKey } != null) {
                            TextUtils.input(value.toString(), 200L)
                        }
                    }

                    continue
                }
            }
            //Make sure we are in the condition settings menu before we go back to actions to add another one
            if (properties.isNotEmpty()) {
                MenuUtils.onOpen("Settings")
                MenuUtils.clickMenuSlot(MenuItems.BACK)
            }
            MenuUtils.onOpen("Edit Conditions")
        }
    }

    object MenuItems {
        val ADD_CONDITION = MenuSlot(Items.PAPER, "Add Condition")
        val BACK = MenuSlot(Items.ARROW, "Go Back")
        val TOGGLE_ADVANCED_OPERATIONS = MenuSlot(Items.COMMAND_BLOCK, "Toggle Advanced Operations")
    }
}