package llc.redstone.systemsapi.util

import dev.isxander.yacl3.config.v3.value
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.SystemsAPI.scaledDelay
import llc.redstone.systemsapi.config.SystemsAPISettings
import llc.redstone.systemsapi.importer.HouseImporter.setImporting
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameContains
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameWithin
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.screen.sync.ItemStackHash
import kotlin.reflect.KClass

object MenuUtils {
    //Debug info
    var waitingOn: String? = null
    var lastWaitingOn: String? = null
    var lastSuccessful: String? = null
    var currentScreen: String? = null

    var pendingScreen: CompletableDeferred<Screen?>? = null
    var pendingClazz: Array<out KClass<out Screen>?> = arrayOf()
    var pendingNameMatch: NameMatch? = null

    suspend fun onCurrentScreenUpdate() {
        val screen = MC.currentScreen ?: return
        onOpen(screen.title?.string ?: return, checkIfOpen = false)
    }

    suspend fun onOpen(
        name: String,
        vararg clazz: KClass<out Screen>? = arrayOf(GenericContainerScreen::class),
        checkIfOpen: Boolean = true
    ): Screen? {
        return onOpen(NameContains(name), *clazz, checkIfOpen = checkIfOpen)
    }

    suspend fun onOpen(
        nameMatch: NameMatch?,
        vararg clazz: KClass<out Screen>? = arrayOf(GenericContainerScreen::class),
        checkIfOpen: Boolean = false
    ): Screen? {
        suspend fun reset() {
            pendingScreen = null
            pendingNameMatch = null
            pendingClazz = arrayOf()
            lastSuccessful = waitingOn
            lastWaitingOn = waitingOn
            waitingOn = null
            if (MC.currentScreen is HandledScreen<*>) awaitUntilMenuItemsLoaded()
        }

        waitingOn = "$nameMatch"
        val deferred = CompletableDeferred<Screen?>()
        pendingScreen?.cancel()
        pendingScreen = deferred
        pendingClazz = clazz
        pendingNameMatch = nameMatch

        if (checkIfOpen) {
            MC.currentScreen?.let { screen ->
                if (pendingClazz.any { it?.isInstance(screen) == true }) {
                    val title = screen.title?.string ?: "null"
                    if (pendingNameMatch?.matches(title) != false) {
                        reset()
                        return screen
                    }
                }
            }
        }


        return try {
            withTimeout(SystemsAPISettings.menuTimeout.value) {
                deferred.await()
            }
        } catch (_: Exception) {
            if (checkScreen(MC.currentScreen)) {
                println("Menu opened during timeout: $nameMatch")
                MC.currentScreen
            } else {
                setImporting(false)
                error("Timed out waiting for menu: $nameMatch")
            }
        } finally {
            reset()
        }
    }

    internal fun completeOnClose() {
        val pending = pendingScreen ?: return
        val screen = MC.currentScreen
        if (screen != null) return
        if (!checkScreen(screen)) return
        pendingScreen = null
        pending.complete(null)
    }

    private fun checkScreen(screen: Screen?): Boolean {
        if (screen == null && pendingNameMatch == null && pendingClazz.contains(null)) return true
        if (pendingClazz.isNotEmpty()) {
            val matchesClass = pendingClazz.any { it?.isInstance(screen) == true }
            if (!matchesClass) return false
        }
        if (pendingNameMatch != null) {
            val title = screen?.title?.string ?: "null"
            if (!pendingNameMatch!!.matches(title)) return false
        }
        return true
    }

    internal fun completeOnOpenScreen(screen: Screen) {
        val pending = pendingScreen ?: return
        if (!checkScreen(screen)) return
        pendingScreen = null
        pending.complete(screen)
    }

    var isLoading = false
    var lastItemAddedTimestamp = 0L
    var itemsLoaded = mutableMapOf<String, ItemStack>()

    var pendingLoaded: CompletableDeferred<Screen>? = null
    private suspend fun awaitUntilMenuItemsLoaded(): Screen {
        val deferred = CompletableDeferred<Screen>()
        pendingLoaded?.cancel()
        pendingLoaded = deferred

        return try {
            isLoading = true
            itemsLoaded.clear()
            lastItemAddedTimestamp = System.currentTimeMillis()
            withTimeout(SystemsAPISettings.menuItemLoadedTimeout.value) {
                deferred.await()
            }
        } finally {
            if (pendingLoaded === deferred) pendingLoaded = null
            scaledDelay(4.0)
        }
    }

    internal fun render() {
        if (!isLoading) return
        val screen = MC.currentScreen as? HandledScreen<*> ?: return
        val delay = 0L // your gui delay
        if (System.currentTimeMillis() - lastItemAddedTimestamp < delay) return

        val slots = screen.screenHandler.slots
        var startIndex = slots.size - 44
        if (startIndex < 0) {
            startIndex = 0
        }
        val hotbarSlots = slots.subList(startIndex, startIndex + 9)
        if (hotbarSlots.all { it.stack.isEmpty }) return
        isLoading = false
        val pending = pendingLoaded ?: return
        pendingLoaded = null
        pending.complete(screen)
    }

    internal fun renderStack(stack: ItemStack) {
        if (!isLoading) return
        val displayName = stack.name.string
        if (itemsLoaded.containsKey(displayName)) return
        lastItemAddedTimestamp = System.currentTimeMillis()
        itemsLoaded[displayName] = stack
    }

    fun clickPlayerSlot(slot: Int, button: Int = 0) {
        val gui = currentMenu()
        val playerSlot = slot + gui.screenHandler.slots.size - 45
        packetClick(playerSlot, button)
    }

    // CORE UTILS

    fun packetClick(slot: Int, button: Int = 0) {
        val gui = currentMenu()

        val pkt = ClickSlotC2SPacket(
            gui.screenHandler.syncId,
            gui.screenHandler.revision,
            slot.toShort(),
            button.toByte(),
            SlotActionType.PICKUP,
            Int2ObjectOpenHashMap(),
            ItemStackHash.EMPTY
        )

        MC.networkHandler?.sendPacket(pkt) ?: error("Failed to send click packet")
    }

    fun interactionClick(slot: Int, button: Int = 0) {
        val gui = currentMenu()

        MC.interactionManager?.clickSlot(
            gui.screenHandler.syncId,
            slot,
            button,
            SlotActionType.PICKUP,
            MC.player
        )
    }

    fun currentMenu(): GenericContainerScreen =
        MC.currentScreen as? GenericContainerScreen
        ?: throw ClassCastException("Expected GenericContainerScreen but found ${MC.currentScreen?.javaClass?.name}")

    // FINDING ITEMS IN MENUS

    suspend fun findSlots(predicate: (ItemStack) -> Boolean, paginated: Boolean = false): List<Slot> {
        fun currentSlots() = currentMenu().screenHandler.slots.filter { predicate(it.stack) }

        var slots = currentSlots()
        while (slots.isEmpty() && paginated) {
            val nextPageSlot = findSlots(GlobalMenuItems.NEXT_PAGE).firstOrNull() ?: return emptyList()
            packetClick(nextPageSlot.id)
            scaledDelay()
            slots = currentSlots()
        }
        return slots
    }

    suspend fun findSlots(name: String, paginated: Boolean = false, partial: Boolean = false): List<Slot> {
        return findSlots({
            if (!partial) it.name.string == name else it.name.string.contains(name)
        }, paginated)
    }

    suspend fun findSlots(name: String, item: Item, paginated: Boolean = false): List<Slot> {
        return findSlots({
            it.name.string == name &&
            it.item == item
        }, paginated)
    }

    suspend fun findSlots(selector: ItemSelector, paginated: Boolean = false): List<Slot> {
        return findSlots(selector.toPredicate(), paginated)
    }

    fun getSlot(slotIndex: Int): Slot {
        return currentMenu().screenHandler.getSlot(slotIndex)
    }

    // CLICKING ITEMS IN MENUS
    suspend fun clickItems(predicate: (ItemStack) -> Boolean, packet: Boolean = true, button: Int = 0, paginated: Boolean = false) {
        findSlots(predicate, paginated).forEach { slot ->
            when (packet) {
                true -> packetClick(slot.id, button)
                false -> interactionClick(slot.id, button)
            }
        }
    }

    suspend fun clickItems(name: String, packet: Boolean = false, button: Int = 0, paginated: Boolean = false) {
        clickItems(
            {
                it.name.string == name
            },
            packet,
            button,
            paginated
        )
    }

    suspend fun clickItems(name: String, item: Item, packet: Boolean = true, button: Int = 0, paginated: Boolean = false) {
        clickItems(
            {
                it.name.string == name &&
                it.item == item
            },
            packet,
            button,
            paginated
        )
    }

    suspend fun clickItems(selector: ItemSelector, packet: Boolean = true, button: Int = 0, paginated: Boolean = false) {
        clickItems(
            selector.toPredicate(),
            packet,
            button,
            paginated
        )
    }

    object GlobalMenuItems {
        val NEXT_PAGE = ItemSelector(
            name = NameWithin(listOf("Next Page", "Left-click for next page!")),
            item = ItemExact(Items.ARROW)
        )
        val PREVIOUS_PAGE = ItemSelector(
            name = NameWithin(listOf("Last Page", "Right-click for previous page!")),
            item = ItemExact(Items.ARROW)
        )
    }
}
