package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.importer.ActionContainer.MenuItems
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
import net.minecraft.nbt.StringNbtReader
import net.minecraft.screen.slot.Slot
import java.lang.reflect.ParameterizedType
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaField

object PropertySettings {
    val importTimes = mutableMapOf<KClass<*>, Long>()
    val exportTimes = mutableMapOf<KClass<*>, Long>()

    suspend fun import(property: KProperty1<out PropertyHolder, *>, slot: Slot, value: Any?) {
        val slotIndex = slot.id
        val index = slot.stack.loreLines(false).indexOfFirst { it == "Current Value:" }
        val currentValueColor = slot.stack.loreLines(true).getOrNull(index + 1) ?: ""
        val currentValue = currentValueColor.replace(Regex("&[0-9a-fk-or]"), "")

        val startTime = System.currentTimeMillis()
        val prevTime = importTimes.getOrDefault(property.returnType.classifier as KClass<*>, 400L)

        fun finishImport() {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            importTimes[property.returnType.classifier as KClass<*>] = (prevTime + duration) / 2
        }

        if (value == null || !slot.hasStack()) {
            return
        }
        when (property.returnType.classifier) {
            Int::class, Double::class, StatValue::class -> {
                if (currentValue != value.toString()) {
                    MenuUtils.packetClick(slotIndex)
                    InputUtils.textInput(value.toString())
                }
            }

            String::class -> {
                if (property.hasAnnotation<Pagination>()) {
                    if (currentValue == value) return
                    MenuUtils.packetClick(slotIndex)
                    MenuUtils.onOpen("Select Option")
                    MenuUtils.clickItems(value.toString(), paginated = true)
                    finishImport()
                    return
                }

                if (currentValueColor == value) return
                MenuUtils.packetClick(slotIndex)
                InputUtils.textInput(value.toString())
            }

            ItemStack::class -> {
                MenuUtils.packetClick(slotIndex)
                MenuUtils.onOpen("Select an Item")

                val nbtString = (value as ItemStack).nbt ?: error("[Item action] ItemStack has no NBT data")
                val nbt = StringNbtReader.readCompound(nbtString)
                val item = NbtHelper.deserializeItemStack(nbt).getOrNull() ?: error("[Item action] Failed to deserialize ItemStack from NBT")
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
                    ActionContainer.updateTime = false
                    genericContainer.addActions(actions)
                    ActionContainer.updateTime = true
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
                finishImport()
                return
            }

            Sound::class -> {
                if (currentValue == value.toString()) return

                value as Sound

                MenuUtils.packetClick(slotIndex)
                MenuUtils.onOpen("Select Option")
                MenuUtils.packetClick(48)
                InputUtils.textInput(value.key)
                finishImport()
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
                finishImport()
                return
            }

            StatOp::class -> {
                if (currentValue == value.toString()) return

                val operation = value as StatOp

                val value = slot.stack.getLoreLineMatchesOrNull(false, filter = { str -> str == operation.key })
                if (value == null) {
                    MenuUtils.packetClick(slotIndex)
                    MenuUtils.onOpen("Select Option")

                    if (operation.advanced) {
                        val advancedOperationsValue = MenuUtils.findSlots(MenuItems.TOGGLE_ADVANCED_OPERATIONS)
                            .firstOrNull()
                            ?.stack
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
                finishImport()
                return
            }
        }

        if (property.returnType.isSubtypeOf(Keyed::class.starProjectedType.withNullability(true))) {
            val keyed = value as Keyed

            if (keyed is KeyedCycle) {
                InputUtils.setKeyedCycle(slot, keyed.key)
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

            finishImport()
            return
        }
        finishImport()
    }

    private val genericContainer = ActionContainer("Edit Actions")

    suspend fun export(title: String, prop: KProperty1<out PropertyHolder, *>, actionSlot: Slot, propertySlotIndex: Int, value: String, colorValue: String): Any? {

        val startTime = System.currentTimeMillis()
        val prevTime = exportTimes.getOrDefault(prop.returnType.classifier as KClass<*>, 50L)
        var colorValue = colorValue
        var value = value

        if (value == "Not Set") {
            return null
        }

        if (value.endsWith("...")) {
            when (prop.returnType.classifier) {
                Location::class -> {
                    MenuUtils.packetClick(actionSlot.id)
                    MenuUtils.onOpen("Action Settings")
                    MenuUtils.getSlot(propertySlotIndex).stack.getCurrentValue(false)?.let {
                        colorValue = it
                    }
                    MenuUtils.clickItems(MenuItems.BACK)
                    MenuUtils.onOpen(title)
                }
                ItemStack::class -> {}
                else -> {
                    colorValue = InputUtils.getPreviousInput {
                        MenuUtils.packetClick(actionSlot.id)
                        MenuUtils.onOpen("Action Settings")
                        MenuUtils.packetClick(propertySlotIndex)
                    }.also {
                        MenuUtils.onOpen("Action Settings")
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
                    MenuUtils.packetClick(actionSlot.id)
                    MenuUtils.onOpen("Action Settings")
                    MenuUtils.packetClick(propertySlotIndex)
                    returnValue = genericContainer.getActions()
                } else if (listType == Condition::class.java) {
                    if (value == "None") return emptyList<Condition>()
                    MenuUtils.packetClick(actionSlot.id)
                    MenuUtils.onOpen("Action Settings")
                    MenuUtils.packetClick(propertySlotIndex)
                    returnValue = ConditionContainer.exportConditions()
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
                MenuUtils.packetClick(actionSlot.id)
                MenuUtils.onOpen("Settings")

                val stack = MenuUtils.getSlot(propertySlotIndex).stack

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

        fun finishExport() {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            exportTimes[prop.returnType.classifier as KClass<*>] = (prevTime + duration) / 2
        }

        if (argValue != null) {
            finishExport()
            return argValue
        }

        if (prop.returnType.isSubtypeOf(Keyed::class.starProjectedType.withNullability(true))) {
            val companion = prop.returnType.classifier
                .let { it as? KClass<*> }
                ?.companionObjectInstance
                ?: error("No companion object for keyed enum: ${prop.returnType}")

            val getByKeyMethod = companion::class.members.find { it.name == "fromKey" }
                ?: error("No getByKey method for keyed enum: ${prop.returnType}")

            finishExport()
            return getByKeyMethod.call(companion, value)
        }

        finishExport()
        return null
    }
}