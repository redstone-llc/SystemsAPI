package llc.redstone.systemsapi.api

interface Gamerule {

    suspend fun getGamerule(gamerule: Gamerules): Boolean
    suspend fun setGamerule(gamerule: Gamerules, newValue: Boolean)

    enum class Gamerules(val displayName: String) {
        JOIN_MESSAGES("joinMessages"),
        DOOR_FENCE_RESET("doorFenceReset"),
        STATUSES("statuses"),
        DESPAWN_VEHICLE_DISMOUNT("despawnVehicleDismount"),
        ITEM_DROPS("itemDrops"),
        PLAYER_CRAFTING("playerCrafting"),
        EGG_HUNT_ALLOW_FLYING("eggHuntAlloFlying"),
        COSMETIC_PETS("cosmeticPets"),
        COSMETIC_PARTICLE_PACKS("cosmeticParticlesPacks"),
        COSMETIC_CLOAK("cosmeticCloak"),
        INVENTORY_BIN("inventoryBin")
    }

}