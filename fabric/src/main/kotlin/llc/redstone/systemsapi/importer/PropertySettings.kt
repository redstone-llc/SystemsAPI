package llc.redstone.systemsapi.importer

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.data.Condition
import llc.redstone.systemsapi.data.CustomKey
import llc.redstone.systemsapi.data.InventorySlot
import llc.redstone.systemsapi.data.ItemStack
import llc.redstone.systemsapi.data.Keyed
import llc.redstone.systemsapi.data.KeyedCycle
import llc.redstone.systemsapi.data.KeyedLabeled
import llc.redstone.systemsapi.data.Location
import llc.redstone.systemsapi.data.Pagination
import llc.redstone.systemsapi.data.PropertyHolder
import llc.redstone.systemsapi.data.StatOp
import llc.redstone.systemsapi.data.StatValue
import llc.redstone.systemsapi.importer.ActionContainer.MenuItems
import llc.redstone.systemsapi.importer.MenuImporter.MenuElementImporter.Companion.pending
import llc.redstone.systemsapi.util.ItemUtils
import llc.redstone.systemsapi.util.ItemUtils.giveItem
import llc.redstone.systemsapi.util.ItemUtils.loreLine
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.MenuUtils.Target
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.nbt.NbtOps
import net.minecraft.screen.slot.Slot
import kotlin.jvm.optionals.getOrNull
import kotlin.math.abs
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

object PropertySettings {
    suspend fun import(property: KProperty1<out PropertyHolder, *>, slot: Slot, value: Any?) {
        val slotIndex = slot.id
        when (property.returnType.classifier) {
            String::class, Int::class, Double::class, StatValue::class -> {
                MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                val pagination = property.annotations.find { it is Pagination }
                if (pagination != null) {
                    MenuUtils.onOpen("Select Option")
                    MenuUtils.clickMenuTargetPaginated(Target(MenuSlot(null, value.toString())))
                    return
                }
                TextUtils.input(value.toString(), 100L)
            }

            InventorySlot::class -> {
                MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                MenuUtils.onOpen("Select Inventory Slot")
                MenuUtils.clickMenuSlot(MenuItems.MANUAL_INPUT)
                TextUtils.input(value.toString(), 100L)
            }

            ItemStack::class -> {
                MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                MenuUtils.onOpen("Select an Item")

                val nbt = (value as ItemStack).nbt ?: error("[Item action] ItemStack has no NBT data")
                val item = ItemUtils.createFromNBT(nbt)
                val player = MC.player ?: error("[Item action] Could not get the player")
                val oldStack = player.inventory.getStack(26)
                item.giveItem(26)
                MenuUtils.clickPlayerSlot(26)
                oldStack.giveItem(26)
            }

            Boolean::class -> {
                val line = slot.stack.loreLine(false, filter = { str -> str == "Disabled" || str == "Enabled" })
                    ?: return
                val currentValue = line == "Enabled"
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

            StatOp::class -> {
                val operation = value as StatOp

                val value = slot.stack.loreLine(false, filter = { str -> str == operation.key })
                if (value == null) {
                    MenuUtils.clickMenuSlot(MenuSlot(null, null, slotIndex))
                    MenuUtils.onOpen("Select Option")

                    if (operation.advanced) {
                        val gui = MC.currentScreen as? GenericContainerScreen ?: return
                        val operationSlot = MenuUtils.findSlot(gui, MenuItems.TOGGLE_ADVANCED_OPERATIONS)
                        val line = operationSlot?.stack?.loreLine(4, false) ?: return
                        val currentValue = line == "Disabled"
                        if (currentValue) {
                            MenuUtils.clickMenuSlot(MenuItems.TOGGLE_ADVANCED_OPERATIONS)
                        }
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

                val current = stack.loreLine(true) { str -> str.contains("➠") } ?: return
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

            return
        }
    }

    suspend fun export(title: String, prop: KProperty1<out PropertyHolder, *>, actionSlot: Slot, propertySlotIndex: Int, value: String, colorValue: String): Any? {
        val gui = MC.currentScreen as? GenericContainerScreen ?: error("[export] Could not cast currentScreen as GenericContainerScreen.")

        var argValue = when (prop.returnType.classifier) {
            String::class -> colorValue
            Int::class -> value.toInt()
            Long::class -> value.toLong()
            Double::class -> value.toDouble()
            Boolean::class -> value.equals("enabled", ignoreCase = true)
            //Stat Values
            StatValue::class -> {
                when {
                    value.matches(Regex("-?\\d+")) -> StatValue.I32(value.toInt())
                    value.matches(Regex("-?\\d+")) -> StatValue.Lng(value.toLong())
                    value.matches(Regex("-?\\d+(\\.\\d+)?")) -> StatValue.Dbl(value.toDouble())
                    else -> StatValue.Str(colorValue)
                }
            }

            ItemStack::class -> {
                MenuUtils.clickMenuSlot(MenuSlot(null, null, actionSlot.id))
                MenuUtils.onOpen("Action Settings")
                MenuUtils.clickMenuSlot(MenuSlot(null, null, propertySlotIndex))
                MenuUtils.onOpen("Select an Item")

                val deferred = CompletableDeferred<net.minecraft.item.ItemStack>()
                pending?.cancel()
                pending = deferred

                val item = try {
                    MenuUtils.packetClick(gui, 13, 0)
                    withTimeout(1_000) { deferred.await() }
                } catch (e: TimeoutCancellationException) {
                    if (pending === deferred) pending = null
                    error("[getItem] Timed out waiting for item.")
                } finally {
                    if (pending === deferred) pending = null
                }
                val nbt = net.minecraft.item.ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, item).result().getOrNull()
                    ?.asCompound()?.getOrNull() ?: error("[Item action] Could not get NBT from item $item")

                MenuUtils.clickMenuSlot(MenuItems.BACK)
                MenuUtils.onOpen(title)

                ItemStack(
                    nbt = nbt,
                    relativeFileLocation = "",
                )
            }

            Location::class -> {
                if (value == "Invokers Location") {
                    Location.CurrentLocation
                } else if (value == "House Spawn Location") {
                    Location.HouseSpawn
                } else {
                    val parts = value.split(",")
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
                    val pitch = pitchPart?.removePrefix("~")?.toFloatOrNull() ?: 0f
                    val yaw = yawPart?.removePrefix("~")?.toFloatOrNull() ?: 0f

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