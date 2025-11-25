package llc.redstone.systemapi.data.enums

import llc.redstone.systemapi.data.Keyed

enum class Enchantment(override val key: String) : Keyed {
    Protection("Protection"),
    FireProtection("Fire Protection"),
    FeatherFalling("Feather Falling"),
    BlastProtection("Blast Protection"),
    ProjectileProtection("Projectile Protection"),
    Respiration("Respiration"),
    AquaAffinity("Aqua Affinity"),
    Thorns("Thorns"),
    DepthStrider("Depth Strider"),
    Sharpness("Sharpness"),
    Smite("Smite"),
    BaneOfArthropods("Bane Of Arthropods"),
    Knockback("Knockback"),
    FireAspect("Fire Aspect"),
    Looting("Looting"),
    Efficiency("Efficiency"),
    SilkTouch("Silk Touch"),
    Unbreaking("Unbreaking"),
    Fortune("Fortune"),
    Power("Power"),
    Punch("Punch"),
    Flame("Flame"),
    Infinity("Infinity");

    companion object {
        fun fromKey(key: String): Enchantment? = entries.find { it.key.equals(key, true) }
    }
}