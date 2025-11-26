package llc.redstone.systemsapi.util

import kotlinx.coroutines.delay
import llc.redstone.systemsapi.SystemsAPI.MC
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.ingame.AnvilScreen
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture.*
import java.util.concurrent.TimeUnit


object TextUtils {

    fun convertTextToString(text: Text, colors: Boolean = true): String {
        return text.siblings.joinToString("") {
            var part = it.string
            val style = it.style
            if (style.color != null && colors) {
                val color: TextColor = style.color!!
                for (format in Formatting.entries) {
                    if (color.rgb == format.colorValue) {
                        part = (format.toString() + part).replace("§", "&")
                    }
                }
            }
            part
        }
    }

    fun sendMessage(message: String) {
        MC.networkHandler?.sendChatMessage(message)
    }

    fun sendMessage(message: String, delayMs: Long) {
        runAsync({
            sendMessage(message)
        }, delayedExecutor(delayMs, TimeUnit.MILLISECONDS))
    }

    suspend fun input(message: String) {
        val screen = MenuUtils.onOpen(null, AnvilScreen::class, ChatScreen::class, null)
        if (screen is AnvilScreen) {
            if (screen.screenHandler.setNewItemName(message)) {
                MC.networkHandler?.sendPacket(RenameItemC2SPacket(message))
            }
            delay(100)
            MenuUtils.interactionClick(screen, 2, 0)
        } else if (screen is ChatScreen) { //If they have Housing Toolbox and the setting is enabled
            sendMessage(message)
        } else if (screen == null) {
            MC.currentScreen = ChatScreen("", false)
            MenuUtils.onOpen(null, ChatScreen::class)
            sendMessage(message)
        }
    }

    suspend fun input(message: String, delayMs: Long) {
        delay(delayMs)
        input(message)
        delay(200)
    }
}