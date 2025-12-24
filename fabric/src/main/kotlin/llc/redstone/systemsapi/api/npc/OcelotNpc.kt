package llc.redstone.systemsapi.api.npc

import llc.redstone.systemsapi.api.Npc

interface OcelotNpc: Npc, Npc.NpcCapabilities.Ageable {
    suspend fun getOcelotType(): OcelotType
    suspend fun setOcelotType(newOcelotType: OcelotType)

    suspend fun getSitting(): Boolean
    suspend fun setSitting(newSitting: Boolean)

    enum class OcelotType {
        BLACK_CAT,
        SIAMESE_CAT,
        WILD_OCELOT,
        RED_CAT
    }
}