package llc.redstone.systemsapi.api.npc

import net.minecraft.item.ItemStack

interface ArmorStandNpc: Npc {
    suspend fun getEquipment(): List<ItemStack>
    suspend fun setEquipment(newEquipment: List<ItemStack>)

    suspend fun hasArms(): Boolean
    suspend fun setArms(hasArms: Boolean)

    suspend fun hasBasePlate(): Boolean
    suspend fun setBasePlate(hasBasePlate: Boolean)

    suspend fun isVisible(): Boolean
    suspend fun setVisible(isVisible: Boolean)

    suspend fun isSmall(): Boolean
    suspend fun setSmall(isSmall: Boolean)
}