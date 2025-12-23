package llc.redstone.systemsapi.api

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

    suspend fun getPermissions(): PermissionSet
    suspend fun setPermissions(newPermissions: PermissionSet)

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

    enum class ChatSpeed(val displayName: String) {
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
        OFF("Off");
        companion object {
            fun fromDisplay(name: String): ChatSpeed? = entries.find { it.displayName == name }
        }
    }

    enum class DefaultGameMode(val displayName: String) {
        ADVENTURE("ADVENTURE"),
        SURVIVAL("SURVIVAL"),
        CREATIVE("CREATIVE");
        companion object {
            fun fromDisplay(name: String): DefaultGameMode? = entries.find { it.displayName == name }
        }
    }

    sealed class PermissionKey<T>(val displayName: String) {
        abstract fun parseFromMenu(text: String): T
        abstract fun toMenuText(value: T): String

        class BoolType(name: String) : PermissionKey<Boolean>(name) {
            override fun parseFromMenu(text: String): Boolean = text == "Enabled" || text == "On"
            override fun toMenuText(value: Boolean): String = if (value) "On" else "Off"
        }

        class EnumType<E : Enum<E>>(
            name: String,
            private val values: Array<E>,
            private val displaySelector: (E) -> String
        ) : PermissionKey<E>(name) {
            override fun parseFromMenu(text: String): E =
                values.find { displaySelector(it) == text }
                ?: error("Unknown value '$text' for permission '$displayName'")
            override fun toMenuText(value: E): String = displaySelector(value)
        }
    }

    object Permissions {
        val FLY = PermissionKey.BoolType("Fly")
        val WOOD_DOOR = PermissionKey.BoolType("Wood Door")
        val IRON_DOOR = PermissionKey.BoolType("Iron Door")
        val WOOD_TRAP_DOOR = PermissionKey.BoolType("Wood Trap Door")
        val IRON_TRAP_DOOR = PermissionKey.BoolType("Iron Trap Door")
        val FENCE_GATE = PermissionKey.BoolType("Fence Gate")
        val BUTTON = PermissionKey.BoolType("Button")
        val LEVER = PermissionKey.BoolType("Lever")
        val USE_LAUNCH_PADS = PermissionKey.BoolType("Use Launch Pads")
        val TP = PermissionKey.BoolType("/tp")
        val TP_OTHER_PLAYERS = PermissionKey.BoolType("/tp Other Players")
        val JUKEBOX = PermissionKey.BoolType("Jukebox")
        val KICK = PermissionKey.BoolType("Kick")
        val BAN = PermissionKey.BoolType("Ban")
        val MUTE = PermissionKey.BoolType("Mute")
        val CHAT = PermissionKey.EnumType("Chat", ChatSpeed.entries.toTypedArray(), { it.displayName })
        val PET_SPAWNING = PermissionKey.BoolType("Pet Spawning")
        val BUILD = PermissionKey.BoolType("Build")
        val OFFLINE_BUILD = PermissionKey.BoolType("Offline Build")
        val FLUID = PermissionKey.BoolType("Fluid")
        val PRO_TOOLS = PermissionKey.BoolType("Pro Tools")
        val USE_CHESTS = PermissionKey.BoolType("Use Chests")
        val USE_ENDER_CHESTS = PermissionKey.BoolType("Use Ender Chests")
        val ITEM_EDITOR = PermissionKey.BoolType("Item Editor")
        val DEFAULT_GAME_MODE = PermissionKey.EnumType("Default Game Mode", DefaultGameMode.entries.toTypedArray(), { it.displayName })
        val SWITCH_GAME_MODE = PermissionKey.BoolType("Switch Game Mode")
        val EDIT_VARIABLES = PermissionKey.BoolType("Edit Variables")
        val CHANGE_PLAYER_GROUP = PermissionKey.BoolType("Change Player Group")
        val CHANGE_GAMERULES = PermissionKey.BoolType("Change Gamerules")
        val HOUSING_MENU = PermissionKey.BoolType("Housing Menu")
        val TEAM_CHAT_SPY = PermissionKey.BoolType("Team Chat Spy")
        val EDIT_ACTIONS = PermissionKey.BoolType("Edit Actions")
        val EDIT_REGIONS = PermissionKey.BoolType("Edit Regions")
        val EDIT_SCOREBOARD = PermissionKey.BoolType("Edit Scoreboard")
        val EDIT_EVENT_ACTIONS = PermissionKey.BoolType("Edit Event Actions")
        val EDIT_COMMANDS = PermissionKey.BoolType("Edit Commands")
        val EDIT_FUNCTIONS = PermissionKey.BoolType("Edit Functions")
        val EDIT_INVENTORY_LAYOUTS = PermissionKey.BoolType("Edit Inventory Layouts")
        val EDIT_TEAMS = PermissionKey.BoolType("Edit Teams")
        val EDIT_CUSTOM_MENUS = PermissionKey.BoolType("Edit Custom Menus")
        val VIEW_ANALYTICS = PermissionKey.BoolType("View Analytics")
        val VIEW_LOGGER = PermissionKey.BoolType("View Logger")
        val ITEM_MAILBOX = PermissionKey.BoolType("Item: Mailbox")
        val ITEM_EGG_HUNT = PermissionKey.BoolType("Item: Egg Hunt")
        val ITEM_TELEPORT_PAD = PermissionKey.BoolType("Item: Teleport Pad")
        val ITEM_LAUNCH_PAD = PermissionKey.BoolType("Item: Launch Pad")
        val ITEM_ACTION_PAD = PermissionKey.BoolType("Item: Action Pad")
        val ITEM_HOLOGRAM = PermissionKey.BoolType("Item: Hologram")
        val ITEM_NPCS = PermissionKey.BoolType("Item: NPCs")
        val ITEM_ACTION_BUTTON = PermissionKey.BoolType("Item: Action Button")
        val ITEM_LEADERBOARD = PermissionKey.BoolType("Item: Leaderboard")
        val ITEM_TRASH_CAN = PermissionKey.BoolType("Item: Trash Can")
        val ITEM_BIOME_STICK = PermissionKey.BoolType("Item: Biome Stick")

        val all: List<PermissionKey<*>> = listOf(
            FLY, WOOD_DOOR, IRON_DOOR, WOOD_TRAP_DOOR, IRON_TRAP_DOOR, FENCE_GATE, BUTTON, LEVER, USE_LAUNCH_PADS,
            TP, TP_OTHER_PLAYERS, JUKEBOX, KICK, BAN, MUTE, CHAT, PET_SPAWNING, BUILD, OFFLINE_BUILD, FLUID,
            PRO_TOOLS, USE_CHESTS, USE_ENDER_CHESTS, ITEM_EDITOR, DEFAULT_GAME_MODE, SWITCH_GAME_MODE,
            EDIT_VARIABLES, CHANGE_PLAYER_GROUP, CHANGE_GAMERULES, HOUSING_MENU, TEAM_CHAT_SPY, EDIT_ACTIONS,
            EDIT_REGIONS, EDIT_SCOREBOARD, EDIT_EVENT_ACTIONS, EDIT_COMMANDS, EDIT_FUNCTIONS, EDIT_INVENTORY_LAYOUTS,
            EDIT_TEAMS, EDIT_CUSTOM_MENUS, VIEW_ANALYTICS, VIEW_LOGGER, ITEM_MAILBOX, ITEM_EGG_HUNT,
            ITEM_TELEPORT_PAD, ITEM_LAUNCH_PAD, ITEM_ACTION_PAD, ITEM_HOLOGRAM, ITEM_NPCS, ITEM_ACTION_BUTTON,
            ITEM_LEADERBOARD, ITEM_TRASH_CAN, ITEM_BIOME_STICK
        )
    }

    class PermissionSet {
        private val data = mutableMapOf<PermissionKey<*>, Any?>()

        operator fun <T> set(key: PermissionKey<T>, value: T) {
            data[key] = value
        }

        operator fun <T> get(key: PermissionKey<T>): T? {
            @Suppress("UNCHECKED_CAST")
            return data[key] as? T
        }

        fun contains(key: PermissionKey<*>) = data.containsKey(key)
    }
}