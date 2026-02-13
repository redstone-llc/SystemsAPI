package llc.redstone.systemsapi.api

/**
 * Provides accessors and mutators for gamerules in a house.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Gamerule {

    /**
     * Retrieves the current boolean value of the given gamerule.
     *
     * @param gamerule the gamerule to query
     * @return true if the gamerule is enabled, false otherwise
     */
    suspend fun getGamerule(gamerule: Gamerules): Boolean

    /**
     * Sets the boolean value of the given gamerule.
     *
     * @param gamerule the gamerule to update
     * @param newValue the new boolean value to set
     * @return the [Gamerule] object
     */
    suspend fun setGamerule(gamerule: Gamerules, newValue: Boolean): Gamerule

    /**
     * Enumeration of supported gamerules with their canonical display names.
     *
     * @param displayName name of gamerule shown in menus.
     */
    enum class Gamerules(val displayName: String) {
        /** Enable or disable join/leave messages. */
        JOIN_MESSAGES("joinMessages"),

        /** Whether doors and fences reset automatically. */
        DOOR_FENCE_RESET("doorFenceReset"),

        /** Enable or disable status effects or similar statuses. */
        STATUSES("statuses"),

        /** Whether vehicles despawn when a player dismounts. */
        DESPAWN_VEHICLE_DISMOUNT("despawnVehicleDismount"),

        /** Enable or disable item drops. */
        ITEM_DROPS("itemDrops"),

        /** Allow or disallow player crafting. */
        PLAYER_CRAFTING("playerCrafting"),

        /** Allow flying during egg hunt events. */
        EGG_HUNT_ALLOW_FLYING("eggHuntAllowFlying"),

        /** Enable or disable cosmetic pets. */
        COSMETIC_PETS("cosmeticPets"),

        /** Enable or disable cosmetic particle packs. */
        COSMETIC_PARTICLE_PACKS("cosmeticParticlesPacks"),

        /** Enable or disable cosmetic cloaks. */
        COSMETIC_CLOAK("cosmeticCloak"),

        /** Enable or disable the inventory bin feature. */
        INVENTORY_BIN("inventoryBin")
    }

}