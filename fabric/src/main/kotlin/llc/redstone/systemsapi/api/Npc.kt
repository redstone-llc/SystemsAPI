package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.api.npc.*
import llc.redstone.systemsapi.data.ItemStack
import llc.redstone.systemsapi.importer.ActionContainer
import kotlin.reflect.KClass

interface Npc {
    var name: String
    suspend fun getName(): String = name
    suspend fun setName(newName: String): Npc

    suspend fun getNpcType(): NpcType
    suspend fun setNpcType(newNpcType: NpcType): Npc

    suspend fun getLookAtPlayers(): Boolean
    suspend fun setLookAtPlayers(newLookAtPlayers: Boolean): Npc

    suspend fun getHideNameTag(): Boolean
    suspend fun setHideNameTag(newHideNameTag: Boolean): Npc

    suspend fun getLeftClickActionContainer(): ActionContainer
    suspend fun getRightClickActionContainer(): ActionContainer

    suspend fun getLeftClickRedirect(): Boolean
    suspend fun setLeftClickRedirect(newLeftClickRedirect: Boolean): Npc

    suspend fun delete()

    enum class NpcType(val displayName: String, val npcInterface: KClass<out Npc>) {
        ARMOR_STAND("Armor Stand", ArmorStandNpc::class),
        BLAZE("Blaze", BlazeNpc::class),
        CAVE_SPIDER("Cave Spider", CaveSpiderNpc::class),
        CHICKEN("Chicken", ChickenNpc::class),
        COW("Cow", CowNpc::class),
        CREEPER("Creeper", CreeperNpc::class),
        ENDERMAN("Enderman", EndermanNpc::class),
        ENDERMITE("Endermite", EndermiteNpc::class),
        GHAST("Ghast", GhastNpc::class),
        IRON_GOLEM("Iron Golem", IronGolemNpc::class),
        MAGMA_CUBE("Magma Cube", MagmaCubeNpc::class),
        MOOSHROOM("Mooshroom", MooshroomNpc::class),
        OCELOT("Ocelot", OcelotNpc::class),
        PIG("Pig", PigNpc::class),
        PLAYER("Player", PlayerNpc::class),
        RABBIT("Rabbit", RabbitNpc::class),
        SHEEP("Sheep", SheepNpc::class),
        SILVERFISH("Silverfish", SilverfishNpc::class),
        SKELETON("Skeleton", SkeletonNpc::class),
        SLIME("Slime", SlimeNpc::class),
        SNOW_GOLEM("Snow Golem", SnowGolemNpc::class),
        SPIDER("Spider", SpiderNpc::class),
        SQUID("Squid", SquidNpc::class),
        VILLAGER("Villager", VillagerNpc::class),
        WITCH("Witch", WitchNpc::class),
        WOLF("Wolf", WolfNpc::class),
        ZOMBIE("Zombie", ZombieNpc::class),
        ZOMBIE_PIGMAN("Zombie Pigman", ZombiePigmanNpc::class),
    }

    sealed interface NpcCapabilities {
        interface Ageable : NpcCapabilities {
            suspend fun getAge(): Age
            suspend fun setAge(newAge: Age)

            enum class Age {
                ADULT,
                BABY
            }
        }
        interface Sizeable : NpcCapabilities {
            suspend fun getSize(): Int
            suspend fun setSize(newSize: Int)
        }
        interface Equippable: NpcCapabilities {
            suspend fun getEquipment(): List<ItemStack>
            suspend fun setEquipment(newEquipment: List<ItemStack>)
        }
    }
}