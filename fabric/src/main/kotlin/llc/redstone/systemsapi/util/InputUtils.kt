package llc.redstone.systemsapi.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.util.ItemStackUtils.getLoreLineMatches
import llc.redstone.systemsapi.util.ItemStackUtils.loreLines
import llc.redstone.systemsapi.util.TextUtils.convertTextToString
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.ingame.AnvilScreen
import net.minecraft.client.resource.language.I18n
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket
import net.minecraft.screen.slot.Slot

object InputUtils {

    // For cycling inputs where the current value is indicated in lore with a "➠" symbol
    fun getKeyedCycle(slot: Slot): String {
        val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
        val currentLine = stack.getLoreLineMatches(false) { str -> str.contains("➠") }
        return currentLine.substringAfter("➠ ")
    }
    suspend fun setKeyedCycle(slot: Slot, value: String) {
        repeat(slot.stack.loreLines(false).size - 3) {
            val currentValue = getKeyedCycle(slot)
            if (currentValue != value) {
                MenuUtils.packetClick(slot.id)
                delay(100)
            } else return
        }
        throw IllegalStateException("Could not find the correct selection for Keyed Cycle")
    }

    // For cycling inputs where the current value is displayed in the title, like "Join/Leave Messages: On"
    fun getKeyedTitleCycle(slot: Slot, key: String): String {
        val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
        return stack.name.string.substringAfter("$key: ")
    }
    suspend fun setKeyedTitleCycle(slot: Slot, key: String, value: String) {
        repeat(50) {
            val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
            val current = stack.name.string.substringAfter("$key: ")
            if (current == value) return
            MenuUtils.packetClick(slot.id)
            delay(200)
            if (MenuUtils.currentMenu().title.string == "Are you sure?") {
                MenuUtils.clickItems("Confirm")
                delay(200)
            }
        }
        throw IllegalStateException("Could not find the correct selection for Titled Cycle")
    }

    // for cycling inputs where the current value is displayed in lore
    fun getLoreCycle(slot: Slot, possibleValues: List<String>): String {
        val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
        return stack.loreLines(false).firstNotNullOfOrNull { line ->
            possibleValues.firstOrNull { pv -> line.contains(pv) }
        } ?: throw IllegalStateException("Could not find the current selection for Lore Cycle")
    }
    suspend fun setLoreCycle(slot: Slot, possibleValues: List<String>, value: String, maxTries: Int = 10) {
        repeat(maxTries) {
            val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
            val current = stack.loreLines(false).firstNotNullOfOrNull { line ->
                possibleValues.firstOrNull { pv -> line.contains(pv) }
            } ?: throw IllegalStateException("Could not find the current selection for Lore Cycle")
            if (current == value) return
            MenuUtils.packetClick(slot.id)
            delay(150)
            if (maxTries == 1) return
        }
        throw IllegalStateException("Could not find the correct selection for Lored Cycle")
    }

    fun getKeyedLoreCycle(slot: Slot, key: String): String {
        val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
        val lines = stack.loreLines(false)
        val index = lines.indexOfFirst { it == "$key:" }
        if (index == -1 || index + 1 >= lines.size) throw IllegalStateException("Could not find the correct selection for Lored Keyed Cycle")
        return lines[index + 1]
    }
    suspend fun setKeyedLoreCycle(slot: Slot, key: String, newValue: String, button: Int = 0) {
        repeat(10) {
            val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
            val lines = stack.loreLines(false)
            val index = lines.indexOfFirst { it == "$key:" }
            if (index == -1 || index + 1 >= lines.size) return@repeat
            val content = lines[index + 1]

            if (content == newValue) return
            MenuUtils.packetClick(slot.id, button = button)
            delay(150)
        }
    }

    fun getInlineKeyedLoreCycle(slot: Slot, key: String): String {
        val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
        return stack.loreLines(false).firstNotNullOfOrNull { line ->
            val result = line.substringAfter("$key: ")
            if (result != line) result else null
        } ?: throw IllegalStateException("Could not find the current selection for Lored Keyed Cycle")
    }
    suspend fun setInlineKeyedLoreCycle(slot: Slot, key: String, newValue: String, button: Int = 0) {
        repeat(10) {
            val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
            val current = stack.loreLines(false).firstNotNullOfOrNull { line ->
                val result = line.substringAfter("$key: ")
                if (result != line) result else null
            } ?: throw IllegalStateException("Could not find the current selection for Lored Keyed Cycle")
            if (current == newValue) return
            MenuUtils.packetClick(slot.id, button = button)
            delay(150)
        }
    }

    fun getDyeToggle(slot: Slot): Boolean? {
        val stack = MenuUtils.currentMenu().screenHandler.getSlot(slot.id).stack
        return when (stack.item) {
            Items.LIME_DYE -> true
            Items.LIGHT_GRAY_DYE, Items.GRAY_DYE, Items.RED_DYE -> false
            Items.STONE_BUTTON -> null
            else -> throw IllegalStateException("Dye Toggle found to be of unexpected type ${stack.item.name.string}")
        }
    }

    suspend fun setDyeToggle(slot: Slot, newValue: Boolean?) {
        repeat(10) {
            val current = getDyeToggle(slot)
            if (current == newValue) return
            if (newValue != null) MenuUtils.packetClick(slot.id) else MenuUtils.packetClick(slot.id, button = 1)
            delay(200)
        }
        throw IllegalStateException("Could not find the correct selection for Dye Toggle")
    }

    // For anvil and chat inputs
    suspend fun textInput(message: String) {
        when (val screen = MenuUtils.onOpen(null, AnvilScreen::class, ChatScreen::class, null)) {
            is AnvilScreen -> {
                delay(100)
                if (screen.screenHandler.setNewItemName(message)) {
                    MC.networkHandler?.sendPacket(RenameItemC2SPacket(message))
                }
                MenuUtils.interactionClick(2)
            }

            is ChatScreen -> { //If they have Housing Toolbox and the setting is enabled
                TextUtils.sendMessage(message)
            }

            null -> {
                MC.setScreen(
                    ChatScreen(
                        /*? >=1.21.9 {*/ "", false /*?} else {*//* "" *//*?}*/
                    )
                )
                MenuUtils.onOpen(null, ChatScreen::class)
                TextUtils.sendMessage(message)
            }
        }
    }

    suspend fun textInput(message: String, delayMs: Long) {
        delay(delayMs)
        textInput(message)
        delay(200)
    }

    var pendingStack: CompletableDeferred<ItemStack>? = null
    var pendingItemDisplayName: String? = null
    var pendingItemCompareStack: ItemStack? = null

    // Returns an item that requires clicking and receiving in your inventory
    // Display Name shouldn't have colors in it, but can, compareStack only looks at the item type
    suspend fun getItemFromMenu(
        displayName: String?, compareStack: ItemStack?,
        click: suspend () -> Unit
    ): ItemStack {
        val deferred = CompletableDeferred<ItemStack>()
        pendingStack?.cancel()
        pendingStack = deferred
        pendingItemDisplayName = displayName
        pendingItemCompareStack = compareStack

        return try {
            click()
            withTimeout(1000) { deferred.await() }
        } finally {
            if (pendingStack === deferred) pendingStack = null
        }
    }
    fun onItemReceived(stack: ItemStack) {
        pendingStack?.let { current ->
            if (pendingItemDisplayName != null) {
                //Translate text to string
                val customName =
                    convertTextToString(stack.customName, false) ?: I18n.translate(stack.item.translationKey)
                val words = customName.split(" ")
                for (word in words) {
                    if (!pendingItemDisplayName!!.contains(word)) return
                }
            }

            if (pendingItemCompareStack != null) {
                println("${stack.item} != ${pendingItemCompareStack!!.item}")
                if (stack.item != pendingItemCompareStack!!.item) return
            }

            pendingStack = null
            pendingItemDisplayName = null
            pendingItemCompareStack = null

            current.complete(stack)
        }
    }


    var pendingString: CompletableDeferred<String>? = null
    suspend fun getPreviousInput(click: suspend () -> Unit): String {
        val deferred = CompletableDeferred<String>()
        pendingString?.cancel()
        pendingString = deferred

        return try {
            click()
            withTimeout(1000) { deferred.await() }
        } finally {
            if (pendingString === deferred) pendingString = null
        }
    }
    fun receivePreviousInput(value: String) {
        CommandUtils.runCommand("chatinput cancel", 0)
        pendingString?.let { current ->
            pendingString = null
            current.complete(value)
        }
    }

}

