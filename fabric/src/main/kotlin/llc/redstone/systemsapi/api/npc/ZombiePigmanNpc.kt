package llc.redstone.systemsapi.api.npc

import net.minecraft.item.ItemStack

interface ZombiePigmanNpc: AgedNpc {
    suspend fun getEquipment(): List<ItemStack>
    suspend fun setEquipment(newEquipment: List<ItemStack>)
}