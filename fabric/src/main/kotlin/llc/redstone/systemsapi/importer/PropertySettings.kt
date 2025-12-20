package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.data.*
import llc.redstone.systemsapi.importer.ActionContainer.MenuItems
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemConverterUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getLoreLine
import llc.redstone.systemsapi.util.ItemStackUtils.getLoreLineMatches
import llc.redstone.systemsapi.util.ItemStackUtils.giveItem
import llc.redstone.systemsapi.util.ItemStackUtils.loreLines
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.MenuUtils.Target
import net.minecraft.nbt.NbtOps
import net.minecraft.screen.slot.Slot
import java.lang.reflect.ParameterizedType
import kotlin.jvm.optionals.getOrNull
import kotlin.math.abs
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaField

object PropertySettings {
    suspend fun import(property: KProperty1<out PropertyHolder, *>, slot: Slot, value: Any?) {
        val slotIndex = slot.id
        val index = slot.stack.loreLines(false).indexOfFirst { it == "Current Value:" }
        val currentValueColor = slot.stack.loreLines(true).getOrNull(index + 1) ?: ""
        val currentValue = currentValueColor.replace(Regex("&[0-9a-fk-or]"), "")

        when (property.returnType.classifier) {
            Int::class, Double::class, StatValue::class -> {
                if (currentValue != value.toString()) {
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                    _root_ide_package_.llc.redstone.systemsapi.util.InputUtils.textInput(value.toString(), 100L)
                }
            }

            String::class -> {
                val pagination = property.annotations.find { it is Pagination }
                if (pagination != null) {
                    if (currentValue == value) return
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                    MenuUtils.onOpen("Select Option")
                    MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, value.toString())))
                    return
                }

                if (currentValueColor == value) return
                MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                _root_ide_package_.llc.redstone.systemsapi.util.InputUtils.textInput(value.toString(), 100L)
            }

            ItemStack::class -> {
                MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                MenuUtils.onOpen("Select an Item")

                val nbt = (value as ItemStack).nbt ?: error("[Item action] ItemStack has no NBT data")
                val item = ItemConverterUtils.createFromNBT(nbt)
                val player = MC.player ?: error("[Item action] Could not get the player")
                val oldStack = player.inventory.getStack(26)
                item.giveItem(26)
                MenuUtils.clickPlayerSlot(26)
                oldStack.giveItem(26)
            }

            Boolean::class -> {
                val currentValue = currentValue == "Enabled"
                val boolValue = value as Boolean
                if (currentValue != boolValue) {
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                }
            }

            List::class -> {
                val value = value as List<*>
                if (value.isEmpty()) return
                //if the first entry is an action then we assume they all are actions
                if (value.first() is Action) {
                    val actions = value.filterIsInstance<Action>()
                    if (actions.size != value.size) error("List contains non-action entries")
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                    ActionContainer("Edit Actions").addActions(actions)
                    MenuUtils.onOpen("Edit Actions")
                    MenuUtils.clickMenuSlot(MenuItems.BACK)
                    MenuUtils.onOpen("Action Settings")
                } else if (value.first() is Condition) {
                    val conditions = value.filterIsInstance<Condition>()
                    if (conditions.size != value.size) error("List contains non-condition entries")
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                    ConditionContainer.addConditions(conditions)
                    MenuUtils.onOpen("Edit Conditions")
                    MenuUtils.clickMenuSlot(MenuItems.BACK)
                    MenuUtils.onOpen("Action Settings")
                }
            }

            InventorySlot::class -> {
                if (currentValue == value.toString()) return

                val operation = value as InventorySlot

                MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                MenuUtils.onOpen("Select Inventory Slot")
                MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, operation.key)))

                if (operation::class.annotations.find { it is CustomKey } != null) {
                    _root_ide_package_.llc.redstone.systemsapi.util.InputUtils.textInput(value.toString(), 200L)
                }
                return
            }

            StatOp::class -> {
                if (currentValue == value.toString()) return

                val operation = value as StatOp

                val value = slot.stack.getLoreLineMatches(false, filter = { str -> str == operation.key })
                if (value == null) {
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                    MenuUtils.onOpen("Select Option")

                    if (operation.advanced) {
                        val advancedOperationsValue = MenuUtils.findSlot(MenuItems.TOGGLE_ADVANCED_OPERATIONS)
                            ?.stack
                            ?.getLoreLine(4, false)
                            ?.equals("Enabled")
                            ?: throw IllegalStateException("Failed to get the status of advanced operations toggle")
                        if (advancedOperationsValue) MenuUtils.clickMenuSlot(MenuItems.TOGGLE_ADVANCED_OPERATIONS)
                    }

                    MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, operation.key)))
                }
                return
            }
        }

        if (property.returnType.isSubtypeOf(Keyed::class.starProjectedType)) {
            val keyed = value as Keyed

            val entries = value.javaClass.enumConstants

            if (keyed is KeyedCycle) {
                val holderIndex = entries.indexOf(keyed) + 1
                val stack = slot.stack

                val current = stack.getLoreLineMatches(true) { str -> str.contains("➠") } ?: return
                val currentHolder = entries.find { current.contains(it.key) }
                val currentIndex = if (currentHolder != null) entries.indexOf(currentHolder) + 1 else 0
                if (currentHolder != keyed) {
                    val clicks = holderIndex - currentIndex
                    repeat(abs(clicks)) {
                        MenuUtils.clickMenuTargets(Target(MenuSlot(null, null, slotIndex), if (clicks > 0) 0 else 1))
                        delay(50) //Small delay to allow the menu to update
                    }
                }

                return
            }

            if (currentValue != keyed.key) {
                MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                MenuUtils.onOpen("Select Option")
                if (keyed is KeyedLabeled) {
                    MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, keyed.label)))
                } else {
                    MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, keyed.key)))
                }

                if (keyed::class.annotations.find { it is CustomKey } != null) {
                    InputUtils.textInput(value.toString(), 200L)
                }
            }

            return
        }
    }

    private val genericContainer = ActionContainer("Edit Actions")

    suspend fun export(title: String, prop: KProperty1<out PropertyHolder, *>, actionSlot: Slot, propertySlotIndex: Int, value: String, colorValue: String): Any? {

        val argValue = when (prop.returnType.classifier) {
            String::class -> colorValue
            Int::class -> value.toInt()
            Long::class -> value.toLong()
            Double::class -> value.toDouble()
            Boolean::class -> value.equals("enabled", ignoreCase = true)
            //Stat Values
            StatValue::class -> {
                when {
                    value == "Not Set" -> null
                    value.matches(Regex("-?\\d+")) -> StatValue.I32(value.toInt())
                    value.matches(Regex("-?\\d+")) -> StatValue.Lng(value.toLong())
                    value.matches(Regex("-?\\d+(\\.\\d+)?")) -> StatValue.Dbl(value.toDouble())
                    else -> StatValue.Str(colorValue)
                }
            }

            List::class -> {
                var returnValue = emptyList<Any>()
                val field = prop.javaField?.genericType as? ParameterizedType ?: error("Could not get parameterized type for List property ${prop.name}")
                val listType = field.actualTypeArguments[0]
                if (listType == Action::class.java) {
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, actionSlot.id))
                    MenuUtils.onOpen("Action Settings")
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, propertySlotIndex))
                    returnValue = genericContainer.getActions()
                } else if (listType == Condition::class.java) {
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, actionSlot.id))
                    MenuUtils.onOpen("Action Settings")
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, propertySlotIndex))
                    returnValue = ConditionContainer.exportConditions()
                }
                MenuUtils.clickMenuSlot(MenuItems.BACK)
                MenuUtils.onOpen("Action Settings")
                MenuUtils.clickMenuSlot(MenuItems.BACK)
                MenuUtils.onOpen(title)

                returnValue
            }

            InventorySlot::class -> {
                InventorySlot.fromKey(value)
            }

            ItemStack::class -> {
                MenuUtils.clickMenuSlot(MenuSlot(null, null, actionSlot.id))
                MenuUtils.onOpen("Settings")

                val stack = MenuUtils.findSlot(MenuSlot(slot=propertySlotIndex))?.stack

                MenuUtils.clickMenuSlot(MenuSlot(null, null, propertySlotIndex))
                MenuUtils.onOpen("Select an Item")

                val item = InputUtils.getItemFromMenu(value, stack) {
                    MenuUtils.interactionClick(13, 0)
                }
                val nbt = net.minecraft.item.ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, item).result().getOrNull()
                    ?.asCompound()?.getOrNull() ?: error("Could not get NBT from item $item")

                MenuUtils.clickMenuSlot(MenuItems.BACK)
                MenuUtils.onOpen("Settings")
                MenuUtils.clickMenuSlot(MenuItems.BACK)
                MenuUtils.onOpen(title)

                ItemStack(
                    nbt = nbt,
                    relativeFileLocation = "",
                )
            }

            Location::class -> {
                when (value) {
                    "Not Set" -> {
                        null
                    }
                    "Invokers Location" -> {
                        Location.CurrentLocation
                    }
                    "House Spawn Location" -> {
                        Location.HouseSpawn
                    }
                    else -> {
                        val parts = value.split(", ")
                        val xPart = parts[0]
                        val yPart = parts[1]
                        val zPart = parts[2]
                        val pitchPart = parts.getOrNull(3)?.split(": ")?.getOrNull(1)
                        val yawPart = parts.getOrNull(4)?.split(": ")?.getOrNull(1)

                        val relX = xPart.startsWith("~")
                        val relY = yPart.startsWith("~")
                        val relZ = zPart.startsWith("~")
                        val relPitch = pitchPart?.startsWith("~") ?: false
                        val relYaw = yawPart?.startsWith("~") ?: false

                        val x = xPart.removePrefix("~").toDoubleOrNull() ?: 0.0
                        val y = yPart.removePrefix("~").toDoubleOrNull() ?: 0.0
                        val z = zPart.removePrefix("~").toDoubleOrNull() ?: 0.0
                        val pitch = pitchPart?.removePrefix("~")?.toFloatOrNull()
                        val yaw = yawPart?.removePrefix("~")?.toFloatOrNull()

                        Location.Custom(
                            relX = relX,
                            relY = relY,
                            relZ = relZ,
                            relPitch = relPitch,
                            relYaw = relYaw,
                            x = x,
                            y = y,
                            z = z,
                            pitch = pitch,
                            yaw = yaw,
                        )
                    }
                }
            }
            else -> null
        }

        if (argValue != null) {
            return argValue
        }

        if (prop.returnType.isSubtypeOf(Keyed::class.starProjectedType)) {
            val companion = prop.returnType.classifier
                .let { it as? kotlin.reflect.KClass<*> }
                ?.companionObjectInstance
                ?: error("No companion object for keyed enum: ${prop.returnType}")

            val getByKeyMethod = companion::class.members.find { it.name == "fromKey" }
                ?: error("No getByKey method for keyed enum: ${prop.returnType}")

            return getByKeyMethod.call(companion, value)
        }

        return null
    }
}