package llc.redstone.systemapi.api.npc

import net.minecraft.item.ItemStack

interface PlayerNpc: Npc {
    suspend fun getSkin(): String
    suspend fun setSkin(newSkin: String)

    suspend fun getEquipment(): List<net.minecraft.item.ItemStack>
    suspend fun setEquipment(newEquipment: List<ItemStack>)
}