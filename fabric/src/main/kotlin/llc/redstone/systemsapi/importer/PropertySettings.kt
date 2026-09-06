package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.importer.ActionContainer.MenuItems
import llc.redstone.systemsapi.progress.ExportPlanner
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getCurrentValue
import llc.redstone.systemsapi.util.ItemStackUtils.getLoreLine
import llc.redstone.systemsapi.util.ItemStackUtils.getLoreLineMatchesOrNull
import llc.redstone.systemsapi.util.ItemStackUtils.giveItem
import llc.redstone.systemsapi.util.ItemStackUtils.loreLines
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.NbtHelper
import llc.redstone.systemsdata.*
import llc.redstone.systemsdata.enums.Sound
import net.minecraft.nbt.TagParser
import net.minecraft.world.inventory.Slot
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaField

object PropertySettings {

    /**
     * Applies [value] to the property's slot in the currently open settings menu.
     *
     * The branches below are mirrored by [llc.redstone.systemsapi.progress.ImportPlanner], which
     * predicts what each one costs. **Adding a branch here means adding one there**, or the new
     * property becomes invisible to the time estimate.
     */
    suspend fun import(property: KProperty1<out PropertyHolder, *>, slot: Slot, value: Any?) {
        val slotIndex = slot.index
        val index = slot.item.loreLines(false).indexOfFirst { it == "Current Value:" }
        val currentValueColor = slot.item.loreLines(true).getOrNull(index + 1) ?: ""
        val currentValue = currentValueColor.replace(Regex("&[0-9a-fk-or]"), "")

        if (value == null || !slot.hasItem()) {
            return
        }
        when (property.returnType.classifier) {
            Double::class -> {
                try {
                    val value: String = BigDecimal(value.toString())
                        .stripTrailingZeros()
                        .toPlainString()
                    if (currentValue != value) {
                        MenuUtils.packetClick(slotIndex)
                        InputUtils.textInput(value)
                    }
                } catch (e: NumberFormatException) {
                    error("[Property action] Failed to parse Double value: $value")
                }
            }
            Int::class, StatValue::class -> {
                if (currentValue != value.toString()) {
                    MenuUtils.packetClick(slotIndex)
                    InputUtils.textInput(value.toString())
                }
            }

            String::class -> {
                if (property.hasAnnotation<Pagination>()) {
                    if (currentValue == value.toString()) return
                    MenuUtils.packetClick(slotIndex)
                    MenuUtils.onOpen("Select Option")
                    MenuUtils.clickItems(value.toString(), paginated = true)
                    return
                }
                if (currentValueColor == value.toString()) return
                if (currentValue == value.toString()) return
                MenuUtils.packetClick(slotIndex)
                InputUtils.textInput(value.toString())
            }

            ItemStack::class -> {
                MenuUtils.packetClick(slotIndex)
                MenuUtils.onOpen("Select an Item")
                value as ItemStack
                val player = MC.player ?: error("[Item action] Could not get the player")

                if (value.nbt != null) {
                    val nbtString = value.nbt ?: error("[Item action] ItemStack has no NBT data")
                    val nbt = TagParser.parseCompoundFully(nbtString)
                    val item = NbtHelper.deserializeItemStack(nbt).getOrNull()
                        ?: error("[Item action] Failed to deserialize ItemStack from NBT")
                    val oldStack = player.inventory.getItem(26)
                    item.giveItem(26)
                    MenuUtils.clickPlayerSlot(26)
                    oldStack.giveItem(26)
                } else if (value.slot != null) {
                    MenuUtils.clickPlayerSlot(value.slot!!)
                } else {
                    error("[Item action] ItemStack must have either NBT data or a slot index")
                }
            }

            Boolean::class -> {
                val currentValue = currentValue == "Enabled"
                val boolValue = value as Boolean
                if (currentValue != boolValue) {
                    MenuUtils.packetClick(slotIndex)
                    MenuUtils.onCurrentScreenUpdate()
                }
            }

            List::class -> {
                val value = value as List<*>
                if (value.isEmpty()) return
                //if the first entry is an action then we assume they all are actions
                if (value.first() is Action) {
                    val actions = value.filterIsInstance<Action>()
                    if (actions.size != value.size) error("List contains non-action entries")
                    MenuUtils.packetClick(slotIndex)
                    // Created fresh, so they start at their constructor defaults.
                    genericContainer.addActions(actions, fresh = true, plan = null)
                    MenuUtils.onOpen("Edit Actions")
                    MenuUtils.clickItems(MenuItems.BACK)
                    MenuUtils.onOpen("Action Settings")
                } else if (value.first() is Condition) {
                    val conditions = value.filterIsInstance<Condition>()
                    if (conditions.size != value.size) error("List contains non-condition entries")
                    MenuUtils.packetClick(slotIndex)
                    ConditionContainer.addConditions(conditions)
                    MenuUtils.onOpen("Edit Conditions")
                    MenuUtils.clickItems(MenuItems.BACK)
                    MenuUtils.onOpen("Action Settings")
                }
            }

            InventorySlot::class -> {
                if (currentValue == value.toString()) return

                val invSlot = value as InventorySlot

                MenuUtils.packetClick(slotIndex)
                MenuUtils.onOpen("Select Inventory Slot")
                MenuUtils.clickItems(invSlot.key, paginated = true)

                if (invSlot::class.annotations.find { it is CustomKey } != null) {
                    InputUtils.textInput(value.toString())
                }
                return
            }

            Sound::class -> {
                if (currentValue == value.toString()) return

                value as Sound

                MenuUtils.packetClick(slotIndex)
                MenuUtils.onOpen("Select Option")
                MenuUtils.packetClick(48)
                InputUtils.textInput(value.key)
                return
            }

            Location::class -> {
                if (currentValue == value.toString()) return

                val location = value as Location

                MenuUtils.packetClick(slotIndex)
                MenuUtils.onOpen("Select Option")

                when (location) {
                    is Location.CurrentLocation, Location.HouseSpawn, Location.InvokersLocation -> MenuUtils.clickItems(location.key, paginated = true)
                    is Location.Custom -> {
                        MenuUtils.clickItems("Custom Coordinates", paginated = true)
                        InputUtils.textInput(location.toString())
                    }
                }
                return
            }

            StatOp::class -> {
                if (currentValue == value.toString()) return

                val operation = value as StatOp

                val value = slot.item.getLoreLineMatchesOrNull(false, filter = { str -> str == operation.key })
                if (value == null) {
                    MenuUtils.packetClick(slotIndex)
                    MenuUtils.onOpen("Select Option")

                    if (operation.advanced) {
                        val advancedOperationsValue = MenuUtils.findSlots(MenuItems.TOGGLE_ADVANCED_OPERATIONS)
                            .firstOrNull()
                            ?.item
                            ?.getLoreLine(4, false)
                            ?.equals("Enabled")
                            ?: throw IllegalStateException("Failed to get the status of advanced operations toggle")
                        if (!advancedOperationsValue) {
                            MenuUtils.clickItems(MenuItems.TOGGLE_ADVANCED_OPERATIONS)
                            MenuUtils.onOpen("Select Option", checkIfOpen = false)
                        }
                    }

                    MenuUtils.clickItems(operation.key, paginated = true)
                }
                return
            }
        }

        if (property.returnType.isSubtypeOf(Keyed::class.starProjectedType.withNullability(true))) {
            val keyed = value as Keyed

            if (currentValue == keyed.key) {
                return
            }

            if (keyed is KeyedLabeled && currentValue == keyed.label) {
                return
            }

            if (keyed is KeyedCycle) {
                InputUtils.setKeyedCycle(slot, keyed.key, cycleKey = keyed::class.simpleName ?: "cycle")
                return
            }

            if (currentValue != keyed.key) {
                MenuUtils.packetClick(slotIndex)
                MenuUtils.onOpen("Select Option")
                if (keyed is KeyedLabeled) {
                    MenuUtils.clickItems(keyed.label, paginated = true)
                } else {
                    MenuUtils.clickItems(keyed.key, paginated = true)
                }

                if (keyed::class.annotations.find { it is CustomKey } != null) {
                    InputUtils.textInput(value.toString())
                }
            }

            return
        }
    }

    private val genericContainer = ActionContainer("Edit Actions")

    suspend fun export(title: String, prop: KProperty1<out PropertyHolder, *>, actionSlot: Slot, propertySlotIndex: Int, value: String, colorValue: String, exportItems: Boolean): Any? {
        var colorValue = colorValue
        var value = value

        if (value == "Not Set") {
            return null
        }

        if (value.endsWith("...") || value == "0.0") {
            when (prop.returnType.classifier) {
                Location::class -> {
                    MenuUtils.packetClick(actionSlot.index)
                    MenuUtils.onOpen("Settings")
                    MenuUtils.getSlot(propertySlotIndex).item.getCurrentValue(false)?.let {
                        colorValue = it
                    }
                    MenuUtils.clickItems(MenuItems.BACK)
                    MenuUtils.onOpen(title)
                }
                ItemStack::class -> {}
                else -> {
                    colorValue = InputUtils.getPreviousInput {
                        MenuUtils.packetClick(actionSlot.index)
                        MenuUtils.onOpen("Settings")
                        MenuUtils.packetClick(propertySlotIndex)
                    }.also {
                        MenuUtils.onOpen("Settings")
                        MenuUtils.clickItems(MenuItems.BACK)
                        MenuUtils.onOpen(title)
                    }
                }
            }
            value = colorValue.replace(Regex("&[0-9a-fk-or]"), "")
        }

        value = when (prop.returnType.classifier) {
            Int::class, Long::class, Double::class  -> value.replace(",", "")
            else -> value
        }

        val argValue = when (prop.returnType.classifier) {
            String::class -> colorValue
            Int::class -> value.toInt()
            Long::class -> value.toLong()
            Double::class -> value.toDouble()
            Boolean::class -> value.equals("enabled", ignoreCase = true)
            //Stat Values
            StatValue::class -> {
                val value = value.replace(",", "")
                when {
                    value == "Not Set" -> null
                    else -> StatValue.fromString(value, colorValue)
                }
            }

            List::class -> {
                var returnValue = emptyList<Any>()
                val field = prop.javaField?.genericType as? ParameterizedType ?: error("Could not get parameterized type for List property ${prop.name}")
                val listType = field.actualTypeArguments[0]
                if (listType == Action::class.java) {
                    if (value == "None") return emptyList<Action>()
                    MenuUtils.packetClick(actionSlot.index)
                    MenuUtils.onOpen("Action Settings")
                    MenuUtils.packetClick(propertySlotIndex)
                    // The parent's lore already priced these by name; claim that pre-charge so this
                    // container's real cost replaces it instead of stacking on top.
                    ExportPlanner.beginNestedDescent()
                    returnValue = try {
                        genericContainer.getActions()
                    } finally {
                        ExportPlanner.endNestedDescent()
                    }
                } else if (listType == Condition::class.java) {
                    if (value == "None") return emptyList<Condition>()
                    MenuUtils.packetClick(actionSlot.index)
                    MenuUtils.onOpen("Action Settings")
                    MenuUtils.packetClick(propertySlotIndex)
                    ExportPlanner.beginNestedDescent()
                    returnValue = try {
                        ConditionContainer.exportConditions(exportItems)
                    } finally {
                        ExportPlanner.endNestedDescent()
                    }
                }
                MenuUtils.clickItems(MenuItems.BACK)
                MenuUtils.onOpen("Action Settings")
                MenuUtils.clickItems(MenuItems.BACK)
                MenuUtils.onOpen(title)

                returnValue
            }

            InventorySlot::class -> {
                InventorySlot.fromKey(value)
            }

            ItemStack::class -> {
                if (!exportItems) {
                    return null
                }
                MenuUtils.packetClick(actionSlot.index)
                MenuUtils.onOpen("Settings")

                val stack = MenuUtils.getSlot(propertySlotIndex).item

                MenuUtils.packetClick(propertySlotIndex)
                MenuUtils.onOpen("Select an Item")

                val item = InputUtils.getItemFromMenu(null, stack) {
                    MenuUtils.interactionClick(13, 0)
                }

                MenuUtils.clickItems(MenuItems.BACK)
                MenuUtils.onOpen("Settings")
                MenuUtils.clickItems(MenuItems.BACK)
                MenuUtils.onOpen(title)

                ItemStack(
                    nbt = NbtHelper.serializeItemStack(item).getOrNull().toString(),
                    relativeFileLocation = "",
                )
            }

            Location::class -> {
                when (value) {
                    "Invokers Location" -> {
                        Location.InvokersLocation
                    }
                    "House Spawn Location" -> {
                        Location.HouseSpawn
                    }
                    else -> {
                        val parts = value.split(", ")
                        if (parts.size < 3) error("Invalid location format: $value")
                        val xPart = parts[0]
                        val yPart = parts[1]
                        val zPart = parts[2]
                        val pitch = parts.getOrNull(3)?.split(": ")?.getOrNull(1)
                        val yaw = parts.getOrNull(4)?.split(": ")?.getOrNull(1)

                        fun parsePart(part: String?): Location.Custom.Coordinate? {
                            if (part == null) return null
                            return Location.Custom.Coordinate(
                                value = part.removePrefix("~").removePrefix("^"),
                                type = when {
                                    part.startsWith("~") -> Location.Custom.Type.RELATIVE
                                    part.startsWith("^") -> Location.Custom.Type.CARET
                                    else -> Location.Custom.Type.NORMAL
                                }
                            )
                        }

                        Location.Custom(
                            x = parsePart(xPart) ?: error("Invalid X coordinate: $xPart"),
                            y = parsePart(yPart) ?: error("Invalid Y coordinate: $yPart"),
                            z = parsePart(zPart) ?: error("Invalid Z coordinate: $zPart"),
                            pitch = parsePart(pitch),
                            yaw = parsePart(yaw)
                        )
                    }
                }
            }
            else -> null
        }

        if (argValue != null) {
            return argValue
        }

        if (prop.returnType.isSubtypeOf(Keyed::class.starProjectedType.withNullability(true))) {
            val companion = prop.returnType.classifier
                .let { it as? KClass<*> }
                ?.companionObjectInstance
                ?: error("No companion object for keyed enum: ${prop.returnType}")

            val getByKeyMethod = companion::class.members.find { it.name == "fromKey" }
                ?: error("No getByKey method for keyed enum: ${prop.returnType}")

            return getByKeyMethod.call(companion, value)
        }

        return null
    }
}