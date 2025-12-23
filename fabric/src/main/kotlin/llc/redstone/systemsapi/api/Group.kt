package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.api.Scoreboard.LineType.BlankLine.displayName
import kotlin.reflect.KClass

interface Group {
    var name: String
    suspend fun getName(): String = name
    suspend fun setName(newName: String)

    suspend fun getTag(): String?
    suspend fun setTag(newTag: String)

    suspend fun getTagVisibleInChat(): Boolean
    suspend fun setTagVisibleInChat(newVisibleInChat: Boolean)

    suspend fun getColor(): GroupColor
    suspend fun setColor(newColor: GroupColor)

    suspend fun getPriority(): Int
    suspend fun setPriority(newPriority: Int)

    suspend fun getPermissions(): MutableMap<GroupPermission, PermissionValue>
    suspend fun setPermissions(newPermissions: MutableMap<GroupPermission, PermissionValue>)

    suspend fun clearGroupPlayers()

    suspend fun delete()


    enum class GroupColor(val displayName: String) {
        DARK_BLUE("Dark Blue"),
        DARK_GREEN("Dark Green"),
        DARK_AQUA("Dark Aqua"),
        DARK_RED("Dark Red"),
        DARK_PURPLE("Dark Purple"),
        GOLD("Gold"),
        GRAY("Gray"),
        DARK_GRAY("Dark Gray"),
        BLUE("Blue"),
        GREEN("Green"),
        AQUA("Aqua"),
        RED("Red"),
        LIGHT_PURPLE("Light Purple"),
        YELLOW("Yellow"),
        WHITE("White")
    }

    enum class GroupPermission(val displayName: String, val valueType: KClass<out PermissionValue>)  {
        FLY("Fly", PermissionValue.BooleanValue::class),
        WOOD_DOOR("Wood Door", PermissionValue.BooleanValue::class),
        IRON_DOOR("Iron Door", PermissionValue.BooleanValue::class),
        WOOD_TRAP_DOOR("Wood Trap Door", PermissionValue.BooleanValue::class),
        IRON_TRAP_DOOR("Iron Trap Door", PermissionValue.BooleanValue::class),
        FENCE_GATE("Fence Gate", PermissionValue.BooleanValue::class),
        BUTTON("Button", PermissionValue.BooleanValue::class),
        LEVER("Lever", PermissionValue.BooleanValue::class),
        USE_LAUNCH_PADS("Use Launch Pads", PermissionValue.BooleanValue::class),
        TP("/tp", PermissionValue.BooleanValue::class),
        TP_OTHER_PLAYERS("/tp Other Players", PermissionValue.BooleanValue::class),
        JUKEBOX("Jukebox", PermissionValue.BooleanValue::class),
        KICK("Kick", PermissionValue.BooleanValue::class),
        BAN("Ban", PermissionValue.BooleanValue::class),
        MUTE("Mute", PermissionValue.BooleanValue::class),
        CHAT("Chat", PermissionValue.ChatValue::class),
        PET_SPAWNING("Pet Spawning", PermissionValue.BooleanValue::class),
        BUILD("Build", PermissionValue.BooleanValue::class),
        OFFLINE_BUILD("Offline Build", PermissionValue.BooleanValue::class),
        FLUID("Fluid", PermissionValue.BooleanValue::class),
        PRO_TOOLS("Pro Tools", PermissionValue.BooleanValue::class),
        USE_CHESTS("Use Chests", PermissionValue.BooleanValue::class),
        USE_ENDER_CHESTS("Use Ender Chests", PermissionValue.BooleanValue::class),
        ITEM_EDITOR("Item Editor", PermissionValue.BooleanValue::class),
        DEFAULT_GAME_MODE("Default Game Mode", PermissionValue.GameModeValue::class),
        SWITCH_GAME_MODE("Switch Game Mode", PermissionValue.BooleanValue::class),
        EDIT_VARIABLES("Edit Variables", PermissionValue.BooleanValue::class),
        CHANGE_PLAYER_GROUP("Change Player Group", PermissionValue.BooleanValue::class),
        CHANGE_GAMERULES("Change Gamerules", PermissionValue.BooleanValue::class),
        HOUSING_MENU("Housing Menu", PermissionValue.BooleanValue::class),
        TEAM_CHAT_SPY("Team Chat Spy", PermissionValue.BooleanValue::class),
        EDIT_ACTIONS("Edit Actions", PermissionValue.BooleanValue::class),
        EDIT_REGIONS("Edit Regions", PermissionValue.BooleanValue::class),
        EDIT_SCOREBOARD("Edit Scoreboard", PermissionValue.BooleanValue::class),
        EDIT_EVENT_ACTIONS("Edit Event Actions", PermissionValue.BooleanValue::class),
        EDIT_COMMANDS("Edit Commands", PermissionValue.BooleanValue::class),
        EDIT_FUNCTIONS("Edit Functions", PermissionValue.BooleanValue::class),
        EDIT_INVENTORY_LAYOUTS("Edit Inventory Layouts", PermissionValue.BooleanValue::class),
        EDIT_TEAMS("Edit Teams", PermissionValue.BooleanValue::class),
        EDIT_CUSTOM_MENUS("Edit Custom Menus", PermissionValue.BooleanValue::class),
        VIEW_ANALYTICS("View Analytics", PermissionValue.BooleanValue::class),
        VIEW_LOGGER("View Logger", PermissionValue.BooleanValue::class),
        ITEM_MAILBOX("Item: Mailbox", PermissionValue.BooleanValue::class),
        ITEM_EGG_HUNT("Item: Egg Hunt", PermissionValue.BooleanValue::class),
        ITEM_TELEPORT_PAD("Item: Teleport Pad", PermissionValue.BooleanValue::class),
        ITEM_LAUNCH_PAD("Item: Launch Pad", PermissionValue.BooleanValue::class),
        ITEM_ACTION_PAD("Item: Action Pad", PermissionValue.BooleanValue::class),
        ITEM_HOLOGRAM("Item: Hologram", PermissionValue.BooleanValue::class),
        ITEM_NPCS("Item: NPCs", PermissionValue.BooleanValue::class),
        ITEM_ACTION_BUTTON("Item: Action Button", PermissionValue.BooleanValue::class),
        ITEM_LEADERBOARD("Item: Leaderboard", PermissionValue.BooleanValue::class),
        ITEM_TRASH_CAN("Item: Trash Can", PermissionValue.BooleanValue::class),
        ITEM_BIOME_STICK("Item: Biome Stick", PermissionValue.BooleanValue::class)
    }

    sealed class PermissionValue(val displayName: String) {
        data class BooleanValue(val value: Boolean) : PermissionValue(displayName)
        data class ChatValue(val value: ChatValues) : PermissionValue(displayName)
        enum class ChatValues(val displayName: String) {
            ON("On"),
            ONE("Slow 1s"),
            TWO("Slow 2s"),
            THREE("Slow 3s"),
            FIVE("Slow 5s"),
            TEN("Slow 10s"),
            FIFTEEN("Slow 15s"),
            THIRTY("Slow 30s"),
            FORTY_FIVE("Slow 45s"),
            SIXTY("Slow 60s"),
            OFF("Off")
        }
        data class GameModeValue(val value: GameModeValues) : PermissionValue(displayName)
        enum class GameModeValues(val displayName: String) {
            ADVENTURE("ADVENTURE"),
            SURVIVAL("SURVIVAL"),
            CREATIVE("CREATIVE")
        }
    }
}