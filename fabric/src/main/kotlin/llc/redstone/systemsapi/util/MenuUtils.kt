package llc.redstone.systemsapi.util

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.util.TextUtils.convertTextToString
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
import net.minecraft.text.Text
import kotlin.reflect.KClass

object MenuUtils {

    var pendingStack: CompletableDeferred<ItemStack>? = null
    // Returns an item that requires clicking and receiving in your inventory
    suspend fun getItemFromMenu(click: suspend () -> Unit): ItemStack {
        val deferred = CompletableDeferred<ItemStack>()
        pendingStack?.cancel()
        pendingStack = deferred

        return try {
            click()
            withTimeout(1_000) { deferred.await() }
        } finally {
            if (pendingStack === deferred) pendingStack = null
        }
    }
    fun onItemReceived(stack: ItemStack, slot: Int) {
        pendingStack?.let { current ->
            if (slot != 67) return //Har har
            pendingStack = null
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
            withTimeout(1_000) { deferred.await() }
        } finally {
            if (pendingString === deferred) pendingString = null
        }
    }
    fun onPreviousInputReceived(value: String) {
        pendingString?.let { current ->
            pendingString = null
            current.complete(value)
        }
    }


    data class Target(val menuSlot: MenuSlot, val button: Int = 0)
    data class MenuSlot(val item: Item?, val label: String?, val slot: Int? = null)

    //Debug info
    var waitingOn: String? = null
    var lastWaitingOn: String? = null
    var lastSuccessful: String? = null
    var currentScreen: String? = null
    var attempts: Int = 0
    var lastClick: String? = null
    var clickWaitingOn: String? = null
    var clickAttempts: Int = 0

    //Used for error correction
    private var lastSlot: MenuSlot? = null
    var lastInput: String? = null
    private var lastButton = 0
    private var attempted: Boolean = false

    suspend fun findSlot(menuSlot: MenuSlot, nullable: Boolean = false): Slot? {
        val gui = currentMenu()
        fun matches(slot: Slot): Boolean {
            val stack = slot.stack
            val customName = convertTextToString(stack.name ?: Text.of(""), false)
            return (menuSlot.item == null || stack.item == menuSlot.item) &&
                   (menuSlot.label == null || customName == menuSlot.label)
        }

        clickAttempts = 0
        clickWaitingOn = "clicking ${menuSlot.item?.name?.string ?: "any item"} ${menuSlot.label ?: "with any name"}"
        currentScreen = MC.currentScreen?.title?.string ?: "null"

        while (true) {
            if (clickAttempts++ >= 10) error("Failed to find slot for $waitingOn in $currentScreen after $clickAttempts attempts.")

            val foundSlot = menuSlot.slot?.let { slot ->
                gui.screenHandler.getSlot(slot).takeIf { matches(it) }
            } ?: gui.screenHandler.slots.firstOrNull { matches(it) }

            lastClick = convertTextToString(foundSlot?.stack?.name ?: Text.of("null")) + " slot ${foundSlot?.id ?: "null"}"
            lastSlot = menuSlot

            if (nullable || (foundSlot != null)) return foundSlot
            delay(50)
        }
    }

    suspend fun onOpen(
        name: String?,
        vararg clazz: KClass<out Screen>? = arrayOf(GenericContainerScreen::class)
    ): Screen? {
        fun reset() {
            lastWaitingOn = waitingOn
            attempts = 0
            waitingOn = null
            currentScreen = null
            lastClick = null
            lastInput = null
            attempted = false
        }
        attempts = 0
        waitingOn = name ?: "null"
        while (true) {
            if (attempts++ >= 10) run {
                if (attempted) {
                    attempted = false
                    error("Failed to find screen $waitingOn after $attempts attempts.")
                } else {
                    attempted = true
                    lastSlot?.let { slot -> clickMenuTargets(Target(slot, lastButton)) }
                    lastInput?.let { TextUtils.reinput() }
                    attempts = 0
                }
            }
            delay(50)
            val screen = MC.currentScreen ?: run {
                if (clazz.contains(null)) {
                    delay(50)
                    reset()
                    return null
                }
                continue
            }
            currentScreen = screen.title.string
            if (clazz.contains(screen::class) && (name == null || currentScreen?.contains(name) == true)) {
                delay(50)
                reset()
                lastSuccessful = currentScreen + " clazz: " + screen::class.simpleName
                return screen
            }
        }
    }

    fun packetClick(slot: Int, button: Int = 0) {
        val gui = MC.currentScreen as? HandledScreen<*> ?: error("[packetClick] Current screen is not a HandledScreen")

        val pkt = ClickSlotC2SPacket(
            gui.screenHandler.syncId,
            gui.screenHandler.revision,
            slot.toShort(),
            button.toByte(),
            SlotActionType.PICKUP,
            Int2ObjectOpenHashMap(),
            ItemStackHash.EMPTY
        )
        lastButton = button

        MC.networkHandler?.sendPacket(pkt) ?: error("Failed to send click packet")
    }

    fun interactionClick(slot: Int, button: Int = 0) {
        val gui = MC.currentScreen as? HandledScreen<*> ?: error("[interactionClick] Current screen is not a HandledScreen")

        lastButton = button
        MC.interactionManager?.clickSlot(
            gui.screenHandler.syncId,
            slot,
            button,
            SlotActionType.PICKUP,
            MC.player
        )
    }

    suspend fun clickMenuSlot(vararg slots: MenuSlot): Boolean =
        clickMenuTargets(*slots.map { Target(it) }.toTypedArray())

    suspend fun clickMenuTargets(vararg attempts: Target): Boolean {
        val match = attempts.firstNotNullOfOrNull {
            it to findSlot(it.menuSlot)!!
        } ?: return false
        packetClick(match.second.id, match.first.button)
        return true
    }

    suspend fun clickMenuTargetPaginated(vararg attempts: Target): Boolean {
        val match = attempts.firstNotNullOfOrNull {
            try {
                it to findSlot(it.menuSlot)
            } catch (e: Exception) {
                null
            }
        }

        if (match == null) {
            val nextPageSlot = findSlot(GlobalMenuItems.NEXT_PAGE, true)
            if (nextPageSlot != null) {
                clickMenuSlot(GlobalMenuItems.NEXT_PAGE)
                delay(200)
                if (clickMenuTargetPaginated(*attempts)) {
                    return true
                }
            }
            return false
        } else {
            packetClick(match.second!!.id, match.first.button)
            return true
        }
    }

    fun clickPlayerSlot(slot: Int, button: Int = 0) {
        val gui = currentMenu()
        val playerSlot = slot + gui.screenHandler.slots.size - 45
        packetClick(playerSlot, button)
    }

    fun currentMenu(): GenericContainerScreen =
        MC.currentScreen as? GenericContainerScreen ?: throw ClassCastException("Expected GenericContainerScreen but found ${MC.currentScreen?.javaClass?.name}")

    object GlobalMenuItems {
        val NEXT_PAGE = MenuSlot(Items.ARROW, "Left-click for next page!")
    }
}