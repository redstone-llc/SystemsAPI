package llc.redstone.systemsapi.api.npc

import net.minecraft.item.ItemStack

interface ZombieNpc: AgedNpc {
    suspend fun getEquipment(): List<ItemStack>
    suspend fun setEquipment(newEquipment: List<ItemStack>)

    suspend fun isZombieVillager(): Boolean
    suspend fun setZombieVillager(isVillager: Boolean)
}