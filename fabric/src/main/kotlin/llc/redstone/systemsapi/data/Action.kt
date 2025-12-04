@file:Suppress("SERIALIZER_TYPE_INCOMPATIBLE")

package llc.redstone.systemsapi.data

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import llc.redstone.systemsapi.data.enums.Enchantment
import llc.redstone.systemsapi.data.enums.Lobby
import llc.redstone.systemsapi.data.enums.PotionEffect
import llc.redstone.systemsapi.data.enums.Sound
import net.minecraft.nbt.NbtCompound

sealed class Action(
    @Transient val actionName: String = ""
): PropertyHolder {
    @DisplayName("Conditional")
    data class Conditional(
        val conditions: List<Condition>,
        val matchAnyCondition: Boolean,
        val ifActions: List<Action>,
        val elseActions: List<Action>,
    ) : Action("CONDITIONAL")

    @DisplayName("Cancel Event")
    class CancelEvent : Action("CANCEL_EVENT")

    @DisplayName("Change Player's Group")
    data class ChangePlayerGroup(
        @property:Pagination val newGroup: String,
        val includeHigherGroups: Boolean = false,
    ) : Action("CHANGE_PLAYER_GROUP")

    @DisplayName("Kill Player")
    class KillPlayer : Action("KILL")

    @DisplayName("Full Heal")
    class FullHeal : Action("FULL_HEAL")

    @DisplayName("Display Title")
    data class DisplayTitle(
        val title: String,
        val subtitle: String,
        val fadeIn: Int,
        val stay: Int,
        val fadeOut: Int,
    ) : Action("TITLE")

    @DisplayName("Display Action Bar")
    data class DisplayActionBar(val message: String) : Action("ACTION_BAR")

    @DisplayName("Reset Inventory")
    class ResetInventory : Action("RESET_INVENTORY")

    @DisplayName("Change Max Health")
    data class ChangeMaxHealth(
        val amount: Double,
        val op: StatOp,
        val healOnChange: Boolean = true,
    ) : Action("CHANGE_MAX_HEALTH")

    @DisplayName("Parkour Checkpoint")
    class ParkourCheckpoint : Action("PARKOUR_CHECKPOINT")

    @DisplayName("Give Item")
    data class GiveItem(
        val item: ItemStack?,
        val allowMultiple: Boolean,
        val inventorySlot: InventorySlot?,
        val replaceExistingItem: Boolean = false,
    ) : Action("GIVE_ITEM")

    @DisplayName("Remove Item")
    data class RemoveItem(val item: ItemStack?) : Action("REMOVE_ITEM")

    @DisplayName("Send a Chat Message")
    data class SendMessage(val message: String) : Action("SEND_MESSAGE")

    @DisplayName("Apply Potion Effect")
    data class ApplyPotionEffect(
        val effect: PotionEffect,
        val duration: Int,
        val level: Int,
        @SerialName("override_existing_effects")
        val override: Boolean,
        @SerialName("show_potion_icon")
        val showIcon: Boolean = false,
    ) : Action("POTION_EFFECT")

    @DisplayName("Clear All Potion Effects")
    class ClearAllPotionEffects : Action("CLEAR_EFFECTS")

    @DisplayName("Give Experience Levels")
    data class GiveExperienceLevels(val levels: Int) : Action("GIVE_EXP_LEVELS")

    @DisplayName("Send to Lobby")
    data class SendToLobby(val location: Lobby) : Action("SEND_TO_LOBBY")

    @DisplayName("Change Variable")
    sealed class ChangeVariable protected constructor(
        val holder: VariableHolder
    ): Action("CHANGE_VARIABLE")

    @DisplayName("Change Variable")
    data class PlayerVariable(
        val variable: String,
        val op: StatOp,
        val amount: StatValue,
        val unset: Boolean = false
    ) : ChangeVariable(VariableHolder.Player)

    @DisplayName("Change Variable")
    class TeamVariable(
        @property:Pagination val teamName: String,
        val variable: String,
        val op: StatOp,
        val amount: StatValue,
        val unset: Boolean = false
    ) : ChangeVariable(VariableHolder.Team) {
    }

    @DisplayName("Change Variable")
    data class GlobalVariable(
        val variable: String,
        val op: StatOp,
        val amount: StatValue,
        val unset: Boolean = false
    ) : ChangeVariable(VariableHolder.Global)

    @DisplayName("Teleport Player")
    data class TeleportPlayer(
        val location: Location,
        val preventInsideBlocks: Boolean = false,
    ) : Action("TELEPORT_PLAYER")

    @DisplayName("Fail Parkour")
    data class FailParkour(val reason: String) : Action("FAIL_PARKOUR")

    @DisplayName("Play Sound")
    data class PlaySound(
        val sound: Sound,
        val volume: Double,
        val pitch: Double,
        val location: Location,
    ) : Action("PLAY_SOUND")

    @DisplayName("Set Compass Target")
    data class SetCompassTarget(val location: Location) : Action("SET_COMPASS_TARGET")

    @DisplayName("Set Gamemode")
    data class SetGameMode(val gamemode: GameMode) : Action("SET_GAMEMODE")

    @DisplayName("Change Health")
    data class ChangeHealth(
        val amount: Double,
        val op: StatOp,
    ) : Action("CHANGE_HEALTH")

    @DisplayName("Change Hunger Level")
    data class ChangeHunger(
        val amount: Double,
        val op: StatOp,
    ) : Action("CHANGE_HUNGER")

    @DisplayName("Use/Remove Held Item")
    class UseHeldItem : Action("USE_HELD_ITEM")

    @DisplayName("Random Action")
    data class RandomAction(
        val actions: List<Action>,
    ) : Action("RANDOM_ACTION")

    @DisplayName("Trigger Function")
    data class ExecuteFunction(@property:Pagination val name: String, val global: Boolean) : Action("TRIGGER_FUNCTION")

    @DisplayName("Apply Inventory Layout")
    data class ApplyInventoryLayout(@property:Pagination val layout: String) : Action("APPLY_LAYOUT")

    @DisplayName("Exit")
    class Exit : Action("EXIT")
    
    @DisplayName("Enchant Held Item")
    data class EnchantHeldItem(
        val enchantment: Enchantment,
        val level: Int,
    ) : Action("ENCHANT_HELD_ITEM")
    
    @DisplayName("Pause Execution")
    data class PauseExecution(val ticks: Int) : Action("PAUSE")

    @DisplayName("Set Player Team")
    data class SetPlayerTeam(@property:Pagination val team: String) : Action("SET_PLAYER_TEAM")

    @DisplayName("Display Menu")
    data class DisplayMenu(@property:Pagination val menu: String) : Action("DISPLAY_MENU")

    @DisplayName("Close Menu")
    class CloseMenu : Action("CLOSE_MENU")

    @DisplayName("Drop Item")
    data class DropItem(
        val item: ItemStack?,
        val location: Location,
        val dropNaturally: Boolean,
        val disableMerging: Boolean,
        val despawnDurationTicks: Int,
        val pickupDelayTicks: Int,
        val prioritizePlayer: Boolean,
        val inventoryFallback: Boolean,
    ) : Action("DROP_ITEM")

    @DisplayName("Change Velocity")
    data class ChangeVelocity(
        val x: Double,
        val y: Double,
        val z: Double,
    ) : Action("CHANGE_VELOCITY")

    @DisplayName("Launch to Target")
    data class LaunchToTarget(
        val location: Location,
        val strength: Double
    ) : Action("LAUNCH_TO_TARGET")

    @DisplayName("Set Player Weather")
    data class SetPlayerWeather(val weather: Weather) : Action("SET_PLAYER_WEATHER")

    @DisplayName("Set Player Time")
    data class SetPlayerTime(val time: Time) : Action("SET_PLAYER_TIME")

    @DisplayName("Toggle Nametag Display")
    data class ToggleNametagDisplay(val displayNametag: Boolean) :
        Action("TOGGLE_NAMETAG_DISPLAY")

    @DisplayName("Balance Player Team")
    class BalancePlayerTeam : Action("BALANCE_PLAYER_TEAM")


}

interface Keyed {
    val key: String
}

annotation class CustomKey
annotation class Pagination

interface KeyedCycle: Keyed

interface KeyedLabeled : Keyed {
    val label: String
}

object KeyedSerializer : KSerializer<Keyed> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Keyed", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Keyed {
        error("not implemented!")
    }

    override fun serialize(encoder: Encoder, value: Keyed) {
        encoder.encodeString(value.key)
    }
}

data class ItemStack(
    val nbt: NbtCompound? = null,
    val relativeFileLocation: String,
)

@Serializable
sealed class Location(override val key: String): Keyed {
    @CustomKey
    class Custom(
        val relX: Boolean,
        val relY: Boolean,
        val relZ: Boolean,
        val relPitch: Boolean = false,
        val relYaw: Boolean = false,
        val x: Double?,
        val y: Double?,
        val z: Double?,
        val pitch: Float?,
        val yaw: Float?,
    ) : Location("Custom Coordinates") {
        override fun toString(): String {
            val xString = if (relX) "~$x" else x
            val yString = if (relY) "~$y" else y
            val zString = if (relZ) "~$z" else z
            if (pitch != null && yaw != null) {
                val pitchString = if (relPitch) "~$pitch" else pitch
                val yawString = if (relYaw) "~$yaw" else yaw
                return "$xString $yString $zString $pitchString $yawString"
            } else {
                return "$xString $yString $zString"
            }
        }
    }


    object HouseSpawn : Location("House Spawn Location")


    object InvokersLocation : Location("Invokers Location")


    object CurrentLocation : Location("Current Location")

    companion object {
        fun fromKey(key: String): Location? = when (key) {
            HouseSpawn.key -> HouseSpawn
            InvokersLocation.key -> InvokersLocation
            CurrentLocation.key -> CurrentLocation
            else -> null
        }
    }
}

enum class GameMode(override val key: String) : KeyedCycle {
    Adventure("Adventure"),
    Survival("Survival"),
    Creative("Creative");

    companion object {
        fun fromKey(key: String): GameMode? = entries.find { it.key.equals(key, true) }
    }
}

enum class StatOp(override val key: String, val advanced: Boolean = false): Keyed {
    Set("Set"),
    UnSet("Unset"),
    Inc("Increment"),
    Dec("Decrement"),
    Mul("Multiply"),
    Div("Divide"),
    BitAnd("Bitwise AND", true),
    BitOr("Bitwise OR", true),
    BitXor("Bitwise XOR", true),
    LS("Left Shift", true),
    ARS("Arithmetic Right Shift", true),
    LRS("Logical Right Shift", true),
    ;

    companion object {
        fun fromKey(key: String): StatOp? = entries.find { it.key.equals(key, true) }
    }
}


sealed class StatValue {
    data class Lng(val value: Long) : StatValue() {
        override fun toString() = value.toString() + "L"
    }
    data class I32(val value: Int) : StatValue() {
        override fun toString() = value.toString()
    }
    data class Dbl(val value: Double) : StatValue() {
        override fun toString() = value.toString()
    }
    data class Str(val value: String) : StatValue() {
        override fun toString() = value
    }
}

sealed class InventorySlot(override val key: String, val slot: Int): Keyed {
    @CustomKey
    data class ManualInput(val inputSlot: Int) : InventorySlot("Manual Input", inputSlot)
    class HandSlot() : InventorySlot("Hand Slot", -2)
    class FirstAvailableSlot() : InventorySlot("First Available Slot", -1)
    class HotbarSlot(slot: Int) : InventorySlot("Hotbar Slot $slot", slot - 1)
    class PlayerInventorySlot(slot: Int) : InventorySlot("Inventory Slot $slot", slot + 8)
    class HelmetSlot() : InventorySlot("Helmet", 39)
    class ChestplateSlot() : InventorySlot("Chestplate", 38)
    class LeggingsSlot() : InventorySlot("Leggings", 37)
    class BootsSlot() : InventorySlot("Boots", 36)
    override fun toString(): String {
        return "$slot"
    }

    companion object {
        fun fromKey(key: String): InventorySlot? {
            if (key.equals("Hand Slot", true)) return HandSlot()
            if (key.equals("First Available Slot", true)) return FirstAvailableSlot()
            if (key.equals("Helmet", true)) return HelmetSlot()
            if (key.equals("Chestplate", true)) return ChestplateSlot()
            if (key.equals("Leggings", true)) return LeggingsSlot()
            if (key.equals("Boots", true)) return BootsSlot()

            val hotbarMatch = Regex("""Hotbar Slot (\d+)""").find(key)
            if (hotbarMatch != null) {
                val slot = hotbarMatch.groupValues[1].toInt()
                return HotbarSlot(slot)
            }

            val inventoryMatch = Regex("""Inventory Slot (\d+)""").find(key)
            if (inventoryMatch != null) {
                val slot = inventoryMatch.groupValues[1].toInt()
                return PlayerInventorySlot(slot)
            }

            val slot = key.toIntOrNull() ?: return null
            return ManualInput(slot)
        }
    }
}

enum class Weather(override val key: String) : KeyedCycle {
    SUNNY("Sunny"),
    RAINING("Raining");

    companion object {
        fun fromKey(key: String): Weather? = entries.find { it.key.equals(key, true) }
    }
}

sealed class Time(override val key: String): Keyed {
    @CustomKey
    class Custom(
        val time: Long
    ) : Time("Custom Time") {
        override fun toString(): String {
            return time.toString()
        }
    }

    object ResetToWorldTime : Time("Reset to World Time")
    object Sunrise : Time("Sunrise")
    object Noon : Time("Noon")
    object Sunset : Time("Sunset")
    object Midnight : Time("Midnight")

    companion object {
        fun fromKey(key: String): Time {
            if (key.contains("Reset to World Time", true)) return ResetToWorldTime
            if (key.contains("Sunrise", true)) return Sunrise
            if (key.contains("Noon", true)) return Noon
            if (key.contains("Sunset", true)) return Sunset
            if (key.contains("Midnight", true)) return Midnight
            return Custom(key.replace(",", "").toLong())
        }
    }
}

enum class VariableHolder(override val key: String) : KeyedCycle {
    Player("Player"),
    Global("Global"),
    Team("Team");

    companion object {
        fun fromKey(key: String): VariableHolder? = entries.find { it.key.equals(key, true) }
    }
}