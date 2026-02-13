package llc.redstone.systemsapi.api

/**
 * Represents a group within Housing.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Group {
    /**
     * The unique name of this group.
     */
    var name: String

    /**
     * Returns the current name of this group.
     *
     * @return the group's name
     */
    suspend fun getName(): String = name

    /**
     * Sets the name of this group.
     *
     * @param newName the new name to set (0..16 characters)
     * @throws IllegalArgumentException if newName is out of acceptable range
     * @return the updated Group instance
     */
    suspend fun setName(newName: String): Group

    /**
     * Gets the group's current tag.
     *
     * @return the group's tag or null if none set
     */
    suspend fun getTag(): String?

    /**
     * Sets the group's chat tag.
     *
     * @param newTag the new tag to set (1..10 characters)
     * @throws IllegalArgumentException if newTag length is out of acceptable range
     * @return the updated [Group] instance
     */
    suspend fun setTag(newTag: String): Group

    /**
     * Returns whether the group's tag is visible in chat.
     *
     * @return true if visible in chat, false otherwise
     */
    suspend fun getTagVisibleInChat(): Boolean

    /**
     * Sets visibility of the group's tag in chat.
     *
     * @param newVisibleInChat true to show the tag, false to hide
     * @return the updated [Group] instance
     */
    suspend fun setTagVisibleInChat(newVisibleInChat: Boolean): Group

    /**
     * Returns the group's display color.
     *
     * @return the group's [GroupColor]
     */
    suspend fun getColor(): GroupColor

    /**
     * Sets the group's display color.
     *
     * @param newColor the new [GroupColor] to apply
     * @return the updated [Group] instance
     */
    suspend fun setColor(newColor: GroupColor): Group

    /**
     * Returns the group's priority. Higher values indicate higher priority.
     *
     * @return integer priority
     */
    suspend fun getPriority(): Int

    /**
     * Sets the group's priority.
     *
     * @param newPriority new integer priority (1..20)
     * @throws IllegalArgumentException if newPriority is out of acceptable range
     * @return the [Group] instance
     */
    suspend fun setPriority(newPriority: Int): Group

    /**
     * Returns the group's permissions as a [PermissionSet].
     *
     * @return the group's permission set
     */
    suspend fun getPermissions(): PermissionSet

    /**
     * Replaces the group's permissions with the provided set.
     *
     * @param newPermissions permission data to apply
     * @return the [Group] instance
     */
    suspend fun setPermissions(newPermissions: PermissionSet): Group

    /**
     * Removes all players from this group.
     *
     * @return the [Group] instance
     */
    suspend fun clearGroupPlayers(): Group

    /**
     * Deletes this group.
     */
    suspend fun delete()


    /**
     * Possible display colors for a group.
     *
     * @property displayName name of color shown in menus
     */
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

    /**
     * Chat speeds used for chat throttling permissions.
     *
     * @property displayName name of value shown in menus
     */
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
    }

    /**
     * Default game modes that can be applied to players in the group.
     *
     * @property displayName name of game mode shown in menus
     */
    enum class DefaultGameMode(val displayName: String) {
        ADVENTURE("ADVENTURE"),
        SURVIVAL("SURVIVAL"),
        CREATIVE("CREATIVE");
    }

    /**
     * A key representing a typed permission entry.
     *
     * Subclasses define how to parse and format values shown in menus.
     *
     * @param displayName name of permission shown in menus
     */
    sealed class PermissionKey<T>(val displayName: String) {
        /**
         * Parse the permission value from the menu text representation.
         *
         * @param text text from the menu representing the value
         * @return parsed value of type T
         */
        abstract fun parseFromMenu(text: String): T

        /**
         * Convert a value to the text representation shown in menus.
         *
         * @param value value to convert
         * @return text to display in menus for this permission value
         */
        abstract fun toMenuText(value: T): String

        /**
         * BoolType represents boolean permissions that are displayed as "On"/"Off" or "Enabled"/"Disabled".
         */
        class BoolType(name: String) : PermissionKey<Boolean>(name) {
            override fun parseFromMenu(text: String): Boolean = text == "Enabled" || text == "On"
            override fun toMenuText(value: Boolean): String = if (value) "On" else "Off"
        }

        /**
         * EnumType represents permissions backed by an enum. It maps between enum entries and their display strings.
         *
         * @param values array of enum entries to choose from
         * @param displaySelector function to obtain the display string for an enum entry
         */
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

    /**
     * Available permissions.
     *
     * Each entry maps to a PermissionKey describing how to parse/format values for that permission.
     */
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
        val CHAT = PermissionKey.EnumType("Chat", ChatSpeed.entries.toTypedArray()) { it.displayName }
        val PET_SPAWNING = PermissionKey.BoolType("Pet Spawning")
        val BUILD = PermissionKey.BoolType("Build")
        val OFFLINE_BUILD = PermissionKey.BoolType("Offline Build")
        val FLUID = PermissionKey.BoolType("Fluid")
        val PRO_TOOLS = PermissionKey.BoolType("Pro Tools")
        val USE_CHESTS = PermissionKey.BoolType("Use Chests")
        val USE_ENDER_CHESTS = PermissionKey.BoolType("Use Ender Chests")
        val ITEM_EDITOR = PermissionKey.BoolType("Item Editor")
        val DEFAULT_GAME_MODE = PermissionKey.EnumType("Default Game Mode", DefaultGameMode.entries.toTypedArray()) { it.displayName }
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

        /**
         * All known permission keys in a stable order.
         */
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

    /**
     * Mutable container for permission values keyed by [PermissionKey].
     *
     * Use the typed operators to set and get permission values.
     */
    class PermissionSet {
        private val data = mutableMapOf<PermissionKey<*>, Any?>()

        /**
         * Set a permission value.
         *
         * @param key permission key
         * @param value value to set for the key
         */
        operator fun <T> set(key: PermissionKey<T>, value: T) {
            data[key] = value
        }

        /**
         * Get a permission value by key.
         *
         * @param key permission key to retrieve
         * @return stored value of type T or null if not present
         */
        operator fun <T> get(key: PermissionKey<T>): T? {
            @Suppress("UNCHECKED_CAST")
            return data[key] as? T
        }

        /**
         * Checks whether a permission key is present in this set.
         *
         * @param key permission key to check
         * @return true if the key exists in the set
         */
        fun contains(key: PermissionKey<*>) = data.containsKey(key)
    }
}