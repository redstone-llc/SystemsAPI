package llc.redstone.systemsapi.util

import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting


object TextUtils {
    fun convertTextToString(text: Text?, colors: Boolean = true): String? {
        if (text == null) return null
        val parts = if (text.siblings.isEmpty()) {
            mutableListOf(text)
        } else {
            mutableListOf(*text.siblings.toTypedArray())
        }
        return parts.joinToString("") { it ->
            var part = it.string.replace("§", "&")
            val style = it.style
            if (style.color != null && colors) {
                val color: TextColor = style.color!!
                for (format in Formatting.entries) {
                    if (color.rgb == format.colorValue) {
                        part = (format.toString() + part).replace("§", "&")
                    }
                }
            }
            if (!colors) {
                part.replace(Regex("(?i)&[0-9A-FK-OR]"), "")
            } else {
                part
            }
        }
    }

}