@file:Suppress("SERIALIZER_TYPE_INCOMPATIBLE")

package llc.redstone.systemsapi.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import llc.redstone.systemsapi.api.Event.Events
import llc.redstone.systemsapi.data.ScopeType.*
import llc.redstone.systemsapi.data.enums.Enchantment
import llc.redstone.systemsapi.data.enums.Lobby
import llc.redstone.systemsapi.data.enums.PotionEffect
import llc.redstone.systemsapi.data.enums.Sound
import net.minecraft.nbt.NbtCompound

sealed class Action(
    @Transient private val actionName: String = ""
): PropertyHolder {
    @ActionDefinition(
        displayName = "Conditional",
        defaultLimit = 25,
        scopes = [
            Scope(FUNCTION),
            Scope(MENU),
            Scope(EVENT, 40, [
                Events.PLAYER_JOIN, Events.PLAYER_QUIT, Events.PLAYER_DEATH, Events.PLAYER_KILL,
                Events.PLAYER_RESPAWN, Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH,
                Events.PLAYER_ENTER_PORTAL, Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR,
                Events.COMPLETE_PARKOUR, Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM,
                Events.PLAYER_TOGGLE_SNEAK, Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class Conditional(
        val conditions: List<Condition>,
        val matchAnyCondition: Boolean,
        val ifActions: List<Action>,
        val elseActions: List<Action>,
    ) : Action("CONDITIONAL")

    @ActionDefinition(
        displayName = "Cancel Event",
        defaultLimit = 1,
        scopes = [
            Scope(EVENT, events = [
                Events.PLAYER_DEATH, Events.FISH_CATCH, Events.PLAYER_DAMAGE, Events.PLAYER_DROP_ITEM,
                Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    class CancelEvent : Action("CANCEL_EVENT")

    @ActionDefinition(
        displayName = "Change Player's Group",
        defaultLimit = 1,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL, Events.PLAYER_DAMAGE,
                Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR, Events.PLAYER_DROP_ITEM,
                Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK, Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class ChangePlayerGroup(
        @property:Pagination val newGroup: String,
        val includeHigherGroups: Boolean = false,
    ) : Action("CHANGE_PLAYER_GROUP")

    @ActionDefinition(
        displayName = "Kill Player",
        defaultLimit = 1,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM)
        ]
    )
    class KillPlayer : Action("KILL")

    @ActionDefinition(
        displayName = "Full Heal",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    class FullHeal : Action("FULL_HEAL")

    @ActionDefinition(
        displayName = "Display Title",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class DisplayTitle(
        val title: String,
        val subtitle: String,
        val fadeIn: Int,
        val stay: Int,
        val fadeOut: Int,
    ) : Action("TITLE")

    @ActionDefinition(
        displayName = "Display Action Bar",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class DisplayActionBar(val message: String) : Action("ACTION_BAR")

    @ActionDefinition(
        displayName = "Reset Inventory",
        defaultLimit = 1,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    class ResetInventory : Action("RESET_INVENTORY")

    @ActionDefinition(
        displayName = "Change Max Health",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class ChangeMaxHealth(
        val amount: Double,
        val op: StatOp,
        val healOnChange: Boolean = true,
    ) : Action("CHANGE_MAX_HEALTH")

    @ActionDefinition(
        displayName = "Parkour Checkpoint",
        defaultLimit = 1,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    class ParkourCheckpoint : Action("PARKOUR_CHECKPOINT")

    @ActionDefinition(
        displayName = "Give Item",
        defaultLimit = 40,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class GiveItem(
        val item: ItemStack?,
        val allowMultiple: Boolean,
        val inventorySlot: InventorySlot?,
        val replaceExistingItem: Boolean = false,
    ) : Action("GIVE_ITEM")

    @ActionDefinition(
        displayName = "Remove Item",
        defaultLimit = 40,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class RemoveItem(val item: ItemStack?) : Action("REMOVE_ITEM")

    @ActionDefinition(
        displayName = "Send a Chat Message",
        defaultLimit = 20,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class SendMessage(val message: String) : Action("SEND_MESSAGE")

    @ActionDefinition(
        displayName = "Apply Potion Effect",
        defaultLimit = 22,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class ApplyPotionEffect(
        val effect: PotionEffect,
        val duration: Int,
        val level: Int,
        @SerialName("override_existing_effects")
        val override: Boolean,
        @SerialName("show_potion_icon")
        val showIcon: Boolean = false,
    ) : Action("POTION_EFFECT")

    @ActionDefinition(
        displayName = "Clear All Potion Effects",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    class ClearAllPotionEffects : Action("CLEAR_EFFECTS")

    @ActionDefinition(
        displayName = "Give Experience Levels",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class GiveExperienceLevels(val levels: Int) : Action("GIVE_EXP_LEVELS")

    @ActionDefinition(
        displayName = "Send to Lobby",
        defaultLimit = 1,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM)
        ]
    )
    data class SendToLobby(val location: Lobby) : Action("SEND_TO_LOBBY")

    @ActionDefinition(
        displayName = "Change Variable",
        defaultLimit = 25,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_QUIT, Events.PLAYER_DEATH, Events.PLAYER_KILL,
                Events.PLAYER_RESPAWN, Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH,
                Events.PLAYER_ENTER_PORTAL, Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR,
                Events.COMPLETE_PARKOUR, Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM,
                Events.PLAYER_TOGGLE_SNEAK, Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    sealed class ChangeVariable protected constructor(
        val holder: VariableHolder
    ): Action("CHANGE_VARIABLE")

    @ActionDefinition(
        displayName = "Change Variable",
        defaultLimit = 25,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_QUIT, Events.PLAYER_DEATH, Events.PLAYER_KILL,
                Events.PLAYER_RESPAWN, Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH,
                Events.PLAYER_ENTER_PORTAL, Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR,
                Events.COMPLETE_PARKOUR, Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM,
                Events.PLAYER_TOGGLE_SNEAK, Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class PlayerVariable(
        val variable: String,
        val op: StatOp,
        val amount: StatValue,
        val unset: Boolean = false
    ) : ChangeVariable(VariableHolder.Player)

    @ActionDefinition(
        displayName = "Change Variable",
        defaultLimit = 25,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_QUIT, Events.PLAYER_DEATH, Events.PLAYER_KILL,
                Events.PLAYER_RESPAWN, Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH,
                Events.PLAYER_ENTER_PORTAL, Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR,
                Events.COMPLETE_PARKOUR, Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM,
                Events.PLAYER_TOGGLE_SNEAK, Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    class TeamVariable(
        @property:Pagination val teamName: String,
        val variable: String,
        val op: StatOp,
        val amount: StatValue,
        val unset: Boolean = false
    ) : ChangeVariable(VariableHolder.Team) {
    }

    @ActionDefinition(
        displayName = "Change Variable",
        defaultLimit = 25,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_QUIT, Events.PLAYER_DEATH, Events.PLAYER_KILL,
                Events.PLAYER_RESPAWN, Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH,
                Events.PLAYER_ENTER_PORTAL, Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR,
                Events.COMPLETE_PARKOUR, Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM,
                Events.PLAYER_TOGGLE_SNEAK, Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class GlobalVariable(
        val variable: String,
        val op: StatOp,
        val amount: StatValue,
        val unset: Boolean = false
    ) : ChangeVariable(VariableHolder.Global)

    @ActionDefinition(
        displayName = "Teleport Player",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class TeleportPlayer(
        val location: Location?,
        val preventInsideBlocks: Boolean = false,
    ) : Action("TELEPORT_PLAYER")

    @ActionDefinition(
        displayName = "Fail Parkour",
        defaultLimit = 1,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class FailParkour(val reason: String) : Action("FAIL_PARKOUR")

    @ActionDefinition(
        displayName = "Play Sound",
        defaultLimit = 25,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class PlaySound(
        val sound: Sound,
        val volume: Double,
        val pitch: Double,
        val location: Location,
    ) : Action("PLAY_SOUND")

    @ActionDefinition(
        displayName = "Set Compass Target",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class SetCompassTarget(val location: Location?) : Action("SET_COMPASS_TARGET")

    @ActionDefinition(
        displayName = "Set Gamemode",
        defaultLimit = 1,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class SetGameMode(val gamemode: GameMode) : Action("SET_GAMEMODE")

    @ActionDefinition(
        displayName = "Change Health",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class ChangeHealth(
        val amount: Double,
        val op: StatOp,
    ) : Action("CHANGE_HEALTH")

    @ActionDefinition(
        displayName = "Change Hunger Level",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class ChangeHunger(
        val amount: Double,
        val op: StatOp,
    ) : Action("CHANGE_HUNGER")

    @ActionDefinition(
        displayName = "Use/Remove Held Item",
        defaultLimit = 1,
        scopes = [
            Scope(ITEM)
        ]
    )
    class UseHeldItem : Action("USE_HELD_ITEM")

    @ActionDefinition(
        displayName = "Random Action",
        defaultLimit = 25,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class RandomAction(
        val actions: List<Action>,
    ) : Action("RANDOM_ACTION")

    @ActionDefinition(
        displayName = "Trigger Function",
        defaultLimit = 10,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class ExecuteFunction(@property:Pagination val name: String, val global: Boolean) : Action("TRIGGER_FUNCTION")

    @ActionDefinition(
        displayName = "Apply Inventory Layout",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class ApplyInventoryLayout(@property:Pagination val layout: String) : Action("APPLY_LAYOUT")

    @ActionDefinition(
        displayName = "Exit",
        defaultLimit = 1,
        scopes = [
            Scope(CONDITIONAL)
        ]
    )
    class Exit : Action("EXIT")
    
    @ActionDefinition(
        displayName = "Enchant Held Item",
        defaultLimit = 25,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class EnchantHeldItem(
        val enchantment: Enchantment,
        val level: Int,
    ) : Action("ENCHANT_HELD_ITEM")
    
    @ActionDefinition(
        displayName = "Pause Execution",
        defaultLimit = 30,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class PauseExecution(val ticks: Int) : Action("PAUSE")

    @ActionDefinition(
        displayName = "Set Player Team",
        defaultLimit = 1,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class SetPlayerTeam(@property:Pagination val team: String) : Action("SET_PLAYER_TEAM")

    @ActionDefinition(
        displayName = "Display Menu",
        defaultLimit = 10,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class DisplayMenu(@property:Pagination val menu: String) : Action("DISPLAY_MENU")

    @ActionDefinition(
        displayName = "Close Menu",
        defaultLimit = 1,
        scopes = [
            Scope(MENU)
        ]
    )
    class CloseMenu : Action("CLOSE_MENU")

    @ActionDefinition(
        displayName = "Drop Item",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
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

    @ActionDefinition(
        displayName = "Change Velocity",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class ChangeVelocity(
        val x: Double,
        val y: Double,
        val z: Double,
    ) : Action("CHANGE_VELOCITY")

    @ActionDefinition(
        displayName = "Launch to Target",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class LaunchToTarget(
        val location: Location,
        val strength: Double
    ) : Action("LAUNCH_TO_TARGET")

    @ActionDefinition(
        displayName = "Set Player Weather",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class SetPlayerWeather(val weather: Weather) : Action("SET_PLAYER_WEATHER")

    @ActionDefinition(
        displayName = "Set Player Time",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class SetPlayerTime(val time: Time) : Action("SET_PLAYER_TIME")

    @ActionDefinition(
        displayName = "Toggle Nametag Display",
        defaultLimit = 5,
        scopes = [
            Scope(FUNCTION),
            Scope(REGION),
            Scope(COMMAND),
            Scope(MENU),
            Scope(ITEM),
            Scope(EVENT, events = [
                Events.PLAYER_JOIN, Events.PLAYER_DEATH, Events.PLAYER_KILL, Events.PLAYER_RESPAWN,
                Events.GROUP_CHANGE, Events.PVP_STATE_CHANGE, Events.FISH_CATCH, Events.PLAYER_ENTER_PORTAL,
                Events.PLAYER_DAMAGE, Events.PLAYER_BLOCK_BREAK, Events.START_PARKOUR, Events.COMPLETE_PARKOUR,
                Events.PLAYER_DROP_ITEM, Events.PLAYER_PICK_UP_ITEM, Events.PLAYER_CHANGE_HELD_ITEM, Events.PLAYER_TOGGLE_SNEAK,
                Events.PLAYER_TOGGLE_FLIGHT
            ])
        ]
    )
    data class ToggleNametagDisplay(val displayNametag: Boolean) : Action("TOGGLE_NAMETAG_DISPLAY")
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
        TODO("not implemented!")
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

    override fun toString(): String {
        println(super.toString())
        return super.toString()
    }

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

open class InventorySlot(override val key: String, val slot: Int): Keyed {
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