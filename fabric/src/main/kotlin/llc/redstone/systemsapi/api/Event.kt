package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer
import net.minecraft.item.Item
import net.minecraft.item.Items

/**
 * Provides accessors for Housing events, allowing retrieval of the [ActionContainer]
 * associated with a specific event.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Event {
    /**
     * Available events that actions may be placed in.
     *
     * @param label Housing's string label for the event.
     * @param item  Housing's item for the event.
     */
    enum class Events(val label: String, val item: Item) {
        /** Event fired when a player joins the house. */
        PLAYER_JOIN("Player Join", Items.OAK_DOOR),
        /** Event fired when a player leaves the house. */
        PLAYER_QUIT("Player Quit", Items.DARK_OAK_DOOR),
        /** Event fired when a player dies. */
        PLAYER_DEATH("Player Death", Items.BONE),
        /** Event fired when a player kills another player. */
        PLAYER_KILL("Player Kill", Items.DIAMOND_SWORD),
        /** Event fired when a player respawns. */
        PLAYER_RESPAWN("Player Respawn", Items.APPLE),
        /** Event fired when a player's group changes. */
        GROUP_CHANGE("Group Change", Items.PAPER),
        /** Event fired when a player's PvP state changes. */
        PVP_STATE_CHANGE("PvP State Change", Items.IRON_SWORD),
        /** Event fired when a player catches a fish. */
        FISH_CATCH("Fish Caught", Items.FISHING_ROD),
        /** Event fired when a player enters a portal. */
        PLAYER_ENTER_PORTAL("Player Enter Portal", Items.OBSIDIAN),
        /** Event fired when a player takes damage. */
        PLAYER_DAMAGE("Player Damage", Items.LAVA_BUCKET),
        /** Event fired when a player breaks a block. */
        PLAYER_BLOCK_BREAK("Player Block Break", Items.GRASS_BLOCK),
        /** Event fired when a player starts a parkour course. */
        START_PARKOUR("Start Parkour", Items.LIGHT_WEIGHTED_PRESSURE_PLATE),
        /** Event fired when a player completes a parkour course. */
        COMPLETE_PARKOUR("Complete Parkour", Items.LIGHT_WEIGHTED_PRESSURE_PLATE),
        /** Event fired when a player drops an item. */
        PLAYER_DROP_ITEM("Player Drop Item", Items.DROPPER),
        /** Event fired when a player picks up an item. */
        PLAYER_PICK_UP_ITEM("Player Pick Up Item", Items.HOPPER),
        /** Event fired when a player changes their held item. */
        PLAYER_CHANGE_HELD_ITEM("Player Change Held Item", Items.BOOK),
        /** Event fired when a player toggles sneaking. */
        PLAYER_TOGGLE_SNEAK("Player Toggle Sneak", Items.HAY_BLOCK),
        /** Event fired when a player toggles flight. */
        PLAYER_TOGGLE_FLIGHT("Player Toggle Flight", Items.FEATHER)
    }

    /**
     * Retrieve the action container associated with a specific event.
     *
     * @param event the event to obtain actions for
     * @return the [ActionContainer] that defines actions executed when the event occurs
     */
    suspend fun getActionContainerForEvent(event: Events): ActionContainer
}