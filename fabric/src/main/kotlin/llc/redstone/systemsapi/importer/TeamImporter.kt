package llc.redstone.systemsapi.importer

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.LOGGER
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.api.Team
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.MenuUtils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot

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

        MenuItems.RENAME_TEAM.click()
        InputUtils.textInput(newName, 100L)

        name = newName
    }

    override suspend fun getTag(): String {
        openTeamMenu()

        val tag = MenuItems.CHANGE_TAG.find()
            .stack
            ?.getProperty("Current Tag")
            ?.removeSurrounding("[", "]")
            ?: throw IllegalStateException("Failed to get team tag")

        return tag
    }

    override suspend fun setTag(newTag: String) {
        openTeamMenu()

        val tag = MenuItems.CHANGE_TAG.find()
            .stack
            ?.getProperty("Current Tag")
            ?.removeSurrounding("[", "]")
            ?: throw IllegalStateException("Failed to get team tag")

        if (tag == newTag) return

        MenuItems.CHANGE_TAG.click()
        InputUtils.textInput(newTag, 100L)
    }

    override suspend fun getColor(): Team.TeamColor {
        openTeamMenu()

        val color = Team.TeamColor.entries.find {
            it.displayName == MenuItems.CHANGE_COLOR.find()
                .stack
                ?.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get team color")

        return color
    }

    override suspend fun setColor(newColor: Team.TeamColor) {
        if (newColor == Team.TeamColor.WHITE) throw IllegalArgumentException("Housing does not support setting the team color as White")
        openTeamMenu()

        val color = Team.TeamColor.entries.find {
            it.displayName == MenuItems.CHANGE_COLOR.find()
                .stack
                ?.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get team color")

        if (color == newColor) return

        MenuItems.CHANGE_COLOR.click()
        MenuUtils.onOpen("Select Team Color")
        MenuUtils.clickItems(newColor.displayName)
    }

    override suspend fun getFriendlyFire(): Boolean {
        openTeamMenu()

        val friendlyFire = MenuItems.FRIENDLY_FIRE.find()
            .stack
            ?.getProperty("Current Value")
            ?.equals("Enabled") ?: throw IllegalStateException("Failed to get team friendly fire")

        return friendlyFire
    }

    override suspend fun setFriendlyFire(newFriendlyFire: Boolean) {
        openTeamMenu()

        for (attempt in 1..10) {
            val friendlyFire = MenuItems.FRIENDLY_FIRE.find()
                .stack
                ?.getProperty("Current Value")
                ?.equals("Enabled") ?: throw IllegalStateException("Failed to get team friendly fire")

            if (friendlyFire == newFriendlyFire) return

            LOGGER.info("attempt $attempt")

            MenuItems.FRIENDLY_FIRE.click()
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

    private enum class MenuItems(
        val label: String,
        val type: Item? = null
    ) {
        RENAME_TEAM("Rename Team", Items.PAPER),
        CHANGE_TAG("Change Tag", Items.OAK_SIGN),
        CHANGE_COLOR("Change Color", Items.REDSTONE),
        FRIENDLY_FIRE("Friendly Fire", Items.IRON_SWORD);

        suspend fun click() = if (type != null) MenuUtils.clickItems(label, type) else MenuUtils.clickItems(label)
        fun find(): Slot = if (type != null) MenuUtils.findSlots(label, type).first() else MenuUtils.findSlots(label).first()
    }

}