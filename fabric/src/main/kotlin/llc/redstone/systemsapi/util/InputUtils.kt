@file:Suppress("UnstableApiUsage")

package llc.redstone.systemsapi.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import llc.redstone.systemsapi.SystemsAPI.CONFIG
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.SystemsAPI.scaledDelay
import llc.redstone.systemsapi.progress.CostModel
import llc.redstone.systemsapi.progress.OpKind
import llc.redstone.systemsapi.progress.OpRecorder
import llc.redstone.systemsapi.util.ItemStackUtils.getLoreLineMatches
import llc.redstone.systemsapi.util.ItemStackUtils.loreLines
import llc.redstone.systemsapi.util.TextUtils.convertTextToString
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.inventory.AnvilScreen
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object InputUtils {

    // For cycling inputs where the current value is indicated in lore with a "➠" symbol
    fun getKeyedCycle(slot: Slot): String {
        val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
        val currentLine = stack.getLoreLineMatches(false) { str -> str.contains("➠") }
        return currentLine.substringAfter("➠ ")
    }
    /**
     * @param cycleKey identifies which cycle this is, so the clicks needed to reach a given value
     * can be learned. Cycling's cost is entirely determined by how far around the cycle the target
     * sits, so an average is a poor estimate.
     */
    suspend fun setKeyedCycle(slot: Slot, value: String, cycleKey: String = slot.item.hoverName.string) {
        val entryCount = slot.item.loreLines(false).size - 3
        var steps = 0
        repeat(entryCount) {
            if (getKeyedCycle(slot) == value) {
                CostModel.recordCycleSteps(cycleKey, value, steps)
                return
            }
            MenuUtils.packetClick(slot.index)
            MenuUtils.onCurrentScreenUpdate()
            steps++
        }
        throw IllegalStateException("Could not find the correct selection for Keyed Cycle")
    }

    // For cycling inputs where the current value is displayed in the title, like "Join/Leave Messages: On"
    fun getKeyedTitleCycle(slot: Slot, key: String): String {
        val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
        return stack.hoverName.string.substringAfter("$key: ")
    }
    suspend fun setKeyedTitleCycle(slot: Slot, key: String, value: String, confirm: Boolean = false) {
        repeat(50) {
            val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
            val current = stack.hoverName.string.substringAfter("$key: ")
            if (current == value) return
            MenuUtils.packetClick(slot.index)
            if (confirm) {
                val oldScreenName = MenuUtils.currentMenu().title.string
//                MenuUtils.onOpen("Are you sure?") // TODO: Why does this not work?
                scaledDelay(8.0)
                if (MenuUtils.currentMenu().title.string != "Are you sure?") throw IllegalStateException("Couldn't find confirmation menu")
                MenuUtils.clickItems("Confirm")
                MenuUtils.onOpen(oldScreenName)
            } else MenuUtils.onCurrentScreenUpdate()
        }
        throw IllegalStateException("Could not find the correct selection for Titled Cycle")
    }

    // for cycling inputs where the current value is displayed in lore
    fun getLoreCycle(slot: Slot, possibleValues: List<String>): String {
        val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
        return stack.loreLines(false).firstNotNullOfOrNull { line ->
            possibleValues.firstOrNull { pv -> line.contains(pv) }
        } ?: throw IllegalStateException("Could not find the current selection for Lore Cycle")
    }
    suspend fun setLoreCycle(slot: Slot, possibleValues: List<String>, value: String, maxTries: Int = 10) {
        repeat(maxTries) {
            val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
            val current = stack.loreLines(false).firstNotNullOfOrNull { line ->
                possibleValues.firstOrNull { pv -> line.contains(pv) }
            } ?: throw IllegalStateException("Could not find the current selection for Lore Cycle")
            if (current == value) return
            MenuUtils.packetClick(slot.index)
            MenuUtils.onCurrentScreenUpdate()
        }
        throw IllegalStateException("Could not find the correct selection for Lored Cycle")
    }

    fun getKeyedLoreCycle(slot: Slot, key: String): String {
        val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
        val lines = stack.loreLines(false)
        val index = lines.indexOfFirst { it == "$key:" }
        if (index == -1 || index + 1 >= lines.size) throw IllegalStateException("Could not find the correct selection for Lored Keyed Cycle")
        return lines[index + 1]
    }
    suspend fun setKeyedLoreCycle(slot: Slot, key: String, newValue: String, button: Int = 0) {
        repeat(10) {
            val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
            val lines = stack.loreLines(false)
            val index = lines.indexOfFirst { it == "$key:" }
            if (index == -1 || index + 1 >= lines.size) return@repeat
            val content = lines[index + 1]

            if (content == newValue) return
            MenuUtils.packetClick(slot.index, button = button)
            MenuUtils.onCurrentScreenUpdate()
        }
    }

    fun getInlineKeyedLoreCycle(slot: Slot, key: String): String {
        val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
        return stack.loreLines(false).firstNotNullOfOrNull { line ->
            val result = line.substringAfter("$key: ")
            if (result != line) result else null
        } ?: throw IllegalStateException("Could not find the current selection for Lored Keyed Cycle")
    }
    suspend fun setInlineKeyedLoreCycle(slot: Slot, key: String, newValue: String, button: Int = 0) {
        repeat(10) {
            val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
            val current = stack.loreLines(false).firstNotNullOfOrNull { line ->
                val result = line.substringAfter("$key: ")
                if (result != line) result else null
            } ?: throw IllegalStateException("Could not find the current selection for Lored Keyed Cycle")
            if (current == newValue) return
            MenuUtils.packetClick(slot.index, button = button)
            MenuUtils.onCurrentScreenUpdate()
        }
    }

    fun getDyeToggle(slot: Slot): Boolean? {
        val stack = MenuUtils.currentMenu().menu.getSlot(slot.index).item
        return when (stack.item) {
            Dyes.LIME -> true
            Dyes.LIGHT_GRAY, Dyes.GRAY, Dyes.RED -> false
            Items.STONE_BUTTON -> null
            else -> throw IllegalStateException("Dye Toggle found to be of unexpected type ${stack.item.getName(stack).string}")
        }
    }

    suspend fun setDyeToggle(slot: Slot, newValue: Boolean?) {
        repeat(10) {
            val current = getDyeToggle(slot)
            if (current == newValue) return
            if (newValue != null) MenuUtils.packetClick(slot.index) else MenuUtils.packetClick(slot.index, button = 1)
            MenuUtils.onCurrentScreenUpdate()
        }
        throw IllegalStateException("Could not find the correct selection for Dye Toggle")
    }

    // For anvil and chat inputs
    suspend fun textInput(message: String) = OpRecorder.span(OpKind.TEXT_INPUT) {
        val message = message.ifEmpty { "&r" }
        ErrorCorrection.lastTextInput = message
        when (val screen = MenuUtils.onOpen(null, AnvilScreen::class, ChatScreen::class, null)) {
            is AnvilScreen -> {
                scaledDelay(4.0)
                if (screen.menu.setItemName(message)) {
                    MC.connection?.send(ServerboundRenameItemPacket(message))
                }
                MenuUtils.interactionClick(2)
            }

            null, is ChatScreen -> { //If they have Housing Toolbox and the setting is enabled
                MC.connection?.sendChat(message)
            }

            else -> throw IllegalStateException("Expected AnvilScreen or ChatScreen, got ${screen.javaClass.name}")
        }
    }

    internal var pendingStack: CompletableDeferred<ItemStack>? = null
    private var pendingItemDisplayName: String? = null
    private var pendingItemCompareStack: ItemStack? = null

    // Returns an item that requires clicking and receiving in your inventory
    // Display Name shouldn't have colors in it, but can, compareStack only looks at the item type
    suspend fun getItemFromMenu(
        displayName: String?, compareStack: ItemStack?,
        click: suspend () -> Unit
    ): ItemStack = OpRecorder.span(OpKind.ITEM_RECEIVE) { span ->
        val deferred = CompletableDeferred<ItemStack>()
        pendingStack?.cancel()
        pendingStack = deferred
        pendingItemDisplayName = displayName
        pendingItemCompareStack = compareStack

        try {
            click()
            withTimeout(CONFIG.menuItemTimeout) { deferred.await() }
        } catch(_: Exception) {
            span.timedOut = true
            error("Failed to get item from menu after timeout")
        } finally {
            if (pendingStack === deferred) pendingStack = null
        }
    }
    internal fun onItemReceived(stack: ItemStack) {
        pendingStack?.let { current ->
            val customName =
                convertTextToString(stack.customName, false) ?: I18n.get(stack.item.descriptionId)
            if (customName.contains("Housing Menu")) return // Housing menu item, not an actual item
            if (pendingItemDisplayName != null) {
                //Translate text to string
                val customName =
                    convertTextToString(stack.customName, false) ?: I18n.get(stack.item.descriptionId)
                val words = customName.split(" ")
                for (word in words) {
                    if (!pendingItemDisplayName!!.contains(word)) return
                }
            }

            if (pendingItemCompareStack != null) {
                if (stack.item != pendingItemCompareStack!!.item) return
            }

            pendingStack = null
            pendingItemDisplayName = null
            pendingItemCompareStack = null

            current.complete(stack)
        }
    }


    internal var pendingString: CompletableDeferred<String>? = null
    suspend fun getPreviousInput(click: suspend () -> Unit): String = OpRecorder.span(OpKind.PREV_INPUT) {
        val deferred = CompletableDeferred<String>()
        pendingString?.cancel()
        pendingString = deferred

        try {
            click()
            withTimeout(CONFIG.previousInputTimeout) { deferred.await() }
        } finally {
            if (pendingString === deferred) pendingString = null
            if (MC.screen is AnvilScreen) {
                throw IllegalStateException("Received a potentially inaccurate value while exporting. To fix this, set your text input method to Chat in /settings and export again.")
            }
        }
    }
    internal fun receivePreviousInput(value: String) {
        CommandUtils.runCommand("chatinput cancel")
        pendingString?.let { current ->
            pendingString = null
            current.complete(value)
        }
    }

}

