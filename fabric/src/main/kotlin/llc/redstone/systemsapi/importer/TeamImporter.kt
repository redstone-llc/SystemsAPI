package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.LOGGER
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Team
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.MenuUtils.MenuSlot
import llc.redstone.systemsapi.util.TextUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Items

class TeamImporter(override var name: String) : Team {
    private fun isTeamMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Manage Team: $name")
    }

    private suspend fun openTeamMenu() {
        if (isTeamMenuOpen()) return

        CommandUtils.runCommand("team edit $name")
        MenuUtils.onOpen("Manage Team: $name")
        delay(50)
    }

    override suspend fun setName(newName: String) {
        if (newName.length !in 1..16) throw IllegalArgumentException("Team name length must be in range 1..16")
        openTeamMenu()

        MenuUtils.clickMenuSlot(MenuItems.RENAME_TEAM)
        TextUtils.input(newName, 100L)

        name = newName
    }

    override suspend fun getTag(): String {
        openTeamMenu()

        val tag = MenuUtils.findSlot(MenuItems.CHANGE_TAG)
            ?.stack
            ?.getProperty("Current Tag")
            ?.removeSurrounding("[", "]")
            ?: throw IllegalStateException("Failed to get team tag")

        return tag
    }

    override suspend fun setTag(newTag: String) {
        openTeamMenu()

        val tag = MenuUtils.findSlot(MenuItems.CHANGE_TAG)
            ?.stack
            ?.getProperty("Current Tag")
            ?.removeSurrounding("[", "]")
            ?: throw IllegalStateException("Failed to get team tag")

        if (tag == newTag) return

        MenuUtils.clickMenuSlot(MenuItems.CHANGE_TAG)
        TextUtils.input(newTag, 100L)
    }

    override suspend fun getColor(): Team.TeamColor {
        openTeamMenu()

        val color = Team.TeamColor.entries.find {
            it.displayName == MenuUtils.findSlot(MenuItems.CHANGE_COLOR)
                ?.stack
                ?.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get team color")

        return color
    }

    override suspend fun setColor(newColor: Team.TeamColor) {
        if (newColor == Team.TeamColor.WHITE) throw IllegalArgumentException("Housing does not support setting the team color as White")
        openTeamMenu()

        val color = Team.TeamColor.entries.find {
            it.displayName == MenuUtils.findSlot(MenuItems.CHANGE_COLOR)
                ?.stack
                ?.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get team color")

        if (color == newColor) return

        MenuUtils.clickMenuSlot(MenuItems.CHANGE_COLOR)
        MenuUtils.onOpen("Select Team Color")
        MenuUtils.clickMenuSlot(MenuSlot(null, newColor.displayName))
    }

    override suspend fun getFriendlyFire(): Boolean {
        openTeamMenu()

        val friendlyFire = MenuUtils.findSlot(MenuItems.FRIENDLY_FIRE)
            ?.stack
            ?.getProperty("Current Value")
            ?.equals("Enabled") ?: throw IllegalStateException("Failed to get team friendly fire")

        return friendlyFire
    }

    override suspend fun setFriendlyFire(newFriendlyFire: Boolean) {
        openTeamMenu()

        for (attempt in 1..10) {
            val friendlyFire = MenuUtils.findSlot(MenuItems.FRIENDLY_FIRE)
                ?.stack
                ?.getProperty("Current Value")
                ?.equals("Enabled") ?: throw IllegalStateException("Failed to get team friendly fire")

            if (friendlyFire == newFriendlyFire) return

            LOGGER.info("attempt $attempt")

            MenuUtils.clickMenuSlot(MenuItems.FRIENDLY_FIRE)
            delay(50)
        }
        throw IllegalStateException("Failed to set team friendly fire")
    }

    // TODO: Make this work for paginated menus i guess
    suspend fun exists(): Boolean {
        CommandUtils.runCommand("team")
        MenuUtils.onOpen("Teams")

        val gui = MenuUtils.currentMenu()
        val slot = gui.screenHandler.slots
            .find { it.stack.name.string == name }
            ?: return false
        return slot.id <= 44
    }

    suspend fun create() {
        if (this.exists()) throw IllegalStateException("Team already exists")
        CommandUtils.runCommand("team create $name")
        MenuUtils.onOpen("Manage Team: $name")
    }

    override suspend fun delete() = CommandUtils.runCommand("team delete $name")

    private object MenuItems {
        val RENAME_TEAM = MenuSlot(Items.PAPER, "Rename Team")
        val CHANGE_TAG = MenuSlot(Items.OAK_SIGN, "Change Tag")
        val CHANGE_COLOR = MenuSlot(Items.REDSTONE, "Change Color")
        val FRIENDLY_FIRE = MenuSlot(Items.IRON_SWORD, "Friendly Fire")
    }

}