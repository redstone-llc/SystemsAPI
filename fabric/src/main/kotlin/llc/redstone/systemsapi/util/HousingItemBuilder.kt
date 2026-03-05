package llc.redstone.systemsapi.util

import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class EnchantmentBuilder {
    internal val enchantments = mutableMapOf<Enchantment, Int>()

    fun add(enchantment: Enchantment, level: Int = 1) {
        enchantments[enchantment] = level
    }
}

class LoreBuilder {
    internal val lines = mutableListOf<String>()

    fun line(text: String) { lines.add(text) }
    operator fun String.unaryPlus() { lines.add(this) }
}

class FlagBuilder {
    internal val flags = mutableMapOf<Flag, Boolean>()

    fun set(flag: Flag, value: Boolean) {
        flags[flag] = value
    }
}

enum class Flag {
    ENCHANTMENTS,
    MODIFIERS,
    UNBREAKABLE,
    ADDITIONAL,
}

class HousingItemBuilder(private val material: Item) {
    private var name: String = ""
    private var loreBuilder = LoreBuilder()
    private var enchantmentBuilder = EnchantmentBuilder()
    private var flagBuilder = FlagBuilder()
    private var unbreakable: Boolean = false

    fun name(name: String) { this.name = name }
    fun unbreakable(unbreakable: Boolean) { this.unbreakable = unbreakable }
    fun lore(block: LoreBuilder.() -> Unit) {
        loreBuilder.apply(block)
    }
    fun enchantments(block: EnchantmentBuilder.() -> Unit) {
        enchantmentBuilder.apply(block)
    }
    fun flags(block: FlagBuilder.() -> Unit) {
        flagBuilder.apply(block)
    }

    fun build(): ItemStack {
        val item = ItemStack(material, 1)
        TODO("Ender job")
    }
}

fun item(material: Item, block: HousingItemBuilder.() -> Unit): ItemStack =
    HousingItemBuilder(material).apply(block).build()