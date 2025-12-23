package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer
import net.minecraft.item.Item
import net.minecraft.item.Items

interface Event {
    enum class Events(val label: String, val item: Item) {
        PLAYER_JOIN("Player Join", Items.OAK_DOOR),
        PLAYER_QUIT("Player Quit", Items.DARK_OAK_DOOR),
        PLAYER_DEATH("Player Death", Items.BONE),
        PLAYER_KILL("Player Kill", Items.DIAMOND_SWORD),
        PLAYER_RESPAWN("Player Respawn", Items.APPLE),
        GROUP_CHANGE("Group Change", Items.PAPER),
        PVP_STATE_CHANGE("PvP State Change", Items.IRON_SWORD),
        FISH_CATCH("Fish Caught", Items.FISHING_ROD),
        PLAYER_ENTER_PORTAL("Player Enter Portal", Items.OBSIDIAN),
        PLAYER_DAMAGE("Player Damage", Items.LAVA_BUCKET),
        PLAYER_BLOCK_BREAK("Player Block Break", Items.GRASS_BLOCK),
        START_PARKOUR("Start Parkour", Items.LIGHT_WEIGHTED_PRESSURE_PLATE),
        COMPLETE_PARKOUR("Complete Parkour", Items.LIGHT_WEIGHTED_PRESSURE_PLATE),
        PLAYER_DROP_ITEM("Player Drop Item", Items.DROPPER),
        PLAYER_PICK_UP_ITEM("Player Pick Up Item", Items.HOPPER),
        PLAYER_CHANGE_HELD_ITEM("Player Change Held Item", Items.BOOK),
        PLAYER_TOGGLE_SNEAK("Player Toggle Sneak", Items.HAY_BLOCK),
        PLAYER_TOGGLE_FLIGHT("Player Toggle Flight", Items.FEATHER)
    }

    suspend fun getActionContainerForEvent(event: Events): ActionContainer
}