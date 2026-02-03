package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.SystemsAPI.LOGGER
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.SystemsAPI.scaledDelay
import llc.redstone.systemsapi.api.Team
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.InputUtils
import llc.redstone.systemsapi.util.ItemStackUtils.getProperty
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Items

class TeamImporter(override var name: String) : Team {
    private fun isTeamMenuOpen(): Boolean {
        val container = MC.currentScreen as? GenericContainerScreen ?: return false
        return container.title.string.contains("Manage Team: ${this@TeamImporter.name}")
    }

    private suspend fun openTeamMenu() {
        if (isTeamMenuOpen()) return

        CommandUtils.runCommand("team edit ${this@TeamImporter.name}")
        MenuUtils.onOpen("Manage Team: ${this@TeamImporter.name}")
        scaledDelay()
    }

    override suspend fun setName(newName: String): Team {
        if (newName.length !in 1..16) throw IllegalArgumentException("Team name length must be in range 1..16")
        openTeamMenu()

        MenuUtils.clickItems(MenuItems.name)
        InputUtils.textInput(newName)

        this@TeamImporter.name = newName
        return this
    }

    override suspend fun getTag(): String {
        openTeamMenu()

        val tag = MenuUtils.findSlots(MenuItems.tag).first()
            .stack
            ?.getProperty("Current Tag")
            ?.removeSurrounding("[", "]")
            ?: throw IllegalStateException("Failed to get team tag")

        return tag
    }

    override suspend fun setTag(newTag: String): Team {
        openTeamMenu()

        val tag = MenuUtils.findSlots(MenuItems.tag).first()
            .stack
            ?.getProperty("Current Tag")
            ?.removeSurrounding("[", "]")
            ?: throw IllegalStateException("Failed to get team tag")

        if (tag == newTag) return this

        MenuUtils.clickItems(MenuItems.tag)
        InputUtils.textInput(newTag)
        return this
    }

    override suspend fun getColor(): Team.TeamColor {
        openTeamMenu()

        val color = Team.TeamColor.entries.find {
            it.displayName == MenuUtils.findSlots(MenuItems.color).first()
                .stack
                ?.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get team color")

        return color
    }

    override suspend fun setColor(newColor: Team.TeamColor): Team {
        if (newColor == Team.TeamColor.WHITE) throw IllegalArgumentException("Housing does not support setting the team color as White")
        openTeamMenu()

        val color = Team.TeamColor.entries.find {
            it.displayName == MenuUtils.findSlots(MenuItems.color).first()
                .stack
                ?.getProperty("Current Color")
        } ?: throw IllegalStateException("Failed to get team color")

        if (color == newColor) return this

        MenuUtils.clickItems(MenuItems.color)
        MenuUtils.onOpen("Select Team Color")
        MenuUtils.clickItems(newColor.displayName)
        return this
    }

    override suspend fun getFriendlyFire(): Boolean {
        openTeamMenu()

        val friendlyFire = MenuUtils.findSlots(MenuItems.friendlyFire).first()
            .stack
            ?.getProperty("Current Value")
            ?.equals("Enabled") ?: throw IllegalStateException("Failed to get team friendly fire")

        return friendlyFire
    }

    override suspend fun setFriendlyFire(newFriendlyFire: Boolean): Team {
        openTeamMenu()

        for (attempt in 1..10) {
            val friendlyFire = MenuUtils.findSlots(MenuItems.friendlyFire).first()
                .stack
                ?.getProperty("Current Value")
                ?.equals("Enabled") ?: throw IllegalStateException("Failed to get team friendly fire")

            if (friendlyFire == newFriendlyFire) return this

            LOGGER.info("attempt $attempt")

            MenuUtils.clickItems(MenuItems.friendlyFire)
            scaledDelay()
        }
        throw IllegalStateException("Failed to set team friendly fire")
    }

    // TODO: Make this work for paginated menus i guess
    suspend fun exists(): Boolean {
        CommandUtils.runCommand("team")
        MenuUtils.onOpen("Teams")

        val gui = MenuUtils.currentMenu()
        val slot = gui.screenHandler.slots
            .find { it.stack.name.string == this@TeamImporter.name }
            ?: return false
        return slot.id <= 44
    }

    suspend fun create() {
        if (this.exists()) throw IllegalStateException("Team already exists")
        CommandUtils.runCommand("team create ${this@TeamImporter.name}")
        MenuUtils.onOpen("Manage Team: ${this@TeamImporter.name}")
    }

    override suspend fun delete() = CommandUtils.runCommand("team delete ${this@TeamImporter.name}")


    private object MenuItems {
        val name = ItemSelector(
            name = NameExact("Rename Team"),
            item = ItemExact(Items.PAPER)
        )
        val tag = ItemSelector(
            name = NameExact("Change Tag"),
            item = ItemExact(Items.ARROW)
        )
        val color = ItemSelector(
            name = NameExact("Change Color"),
            item = ItemExact(Items.OAK_SIGN)
        )
        val friendlyFire = ItemSelector(
            name = NameExact("Friendly Fire"),
            item = ItemExact(Items.IRON_SWORD)
        )
    }

}