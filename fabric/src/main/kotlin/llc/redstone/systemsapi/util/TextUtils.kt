package llc.redstone.systemsapi.util

import llc.redstone.systemsapi.SystemsAPI.MC
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting


object TextUtils {
    fun convertTextToString(text: Text?, colors: Boolean = true): String? {
        if (text == null) return null
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

}