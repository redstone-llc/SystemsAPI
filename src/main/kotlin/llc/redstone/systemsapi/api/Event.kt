package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import net.minecraft.item.Items

interface Event {
    enum class Events(val item: MenuSlot) {
        PLAYER_JOIN(MenuSlot(Items.OAK_DOOR, "Player Join")),
        PLAYER_QUIT(MenuSlot(Items.DARK_OAK_DOOR, "Player Quit")),
        PLAYER_DEATH(MenuSlot(Items.BONE, "Player Death")),
        PLAYER_KILL(MenuSlot(Items.DIAMOND_SWORD, "Player Kill")),
        PLAYER_RESPAWN(MenuSlot(Items.APPLE, "Player Respawn")),
        GROUP_CHANGE(MenuSlot(Items.PAPER, "Group Change")),
        PVP_STATE_CHANGE(MenuSlot(Items.IRON_SWORD, "PvP State Change")),
        FISH_CATCH(MenuSlot(Items.FISHING_ROD, "Fish Caught")),
        PLAYER_ENTER_PORTAL(MenuSlot(Items.OBSIDIAN, "Player Enter Portal")),
        PLAYER_DAMAGE(MenuSlot(Items.LAVA_BUCKET, "Player Damage")),
        PLAYER_BLOCK_BREAK(MenuSlot(Items.GRASS_BLOCK, "Player Block Break")),
        START_PARKOUR(MenuSlot(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, "Start Parkour")),
        COMPLETE_PARKOUR(MenuSlot(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, "Complete Parkour")),
        PLAYER_DROP_ITEM(MenuSlot(Items.DROPPER, "Player Drop Item")),
        PLAYER_PICK_UP_ITEM(MenuSlot(Items.HOPPER, "Player Pick Up Item")),
        PLAYER_CHANGE_HELD_ITEM(MenuSlot(Items.BOOK, "Player Change Held Item")),
        PLAYER_TOGGLE_SNEAK(MenuSlot(Items.HAY_BLOCK, "Player Toggle Sneak")),
        PLAYER_TOGGLE_FLIGHT(MenuSlot(Items.FEATHER, "Player Toggle Flight"))
    }

    suspend fun getActionContainerForEvent(event: Events): ActionContainer
}