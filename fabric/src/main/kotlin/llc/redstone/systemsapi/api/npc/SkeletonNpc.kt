package llc.redstone.systemsapi.api.npc

import net.minecraft.item.ItemStack

interface SkeletonNpc: Npc {
    suspend fun getEquipment(): List<ItemStack>
    suspend fun setEquipment(newEquipment: List<ItemStack>)

    suspend fun getWithered(): Boolean
    suspend fun setWithered(newWithered: Boolean)
}