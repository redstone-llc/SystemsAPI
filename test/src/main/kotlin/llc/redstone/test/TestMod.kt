package llc.redstone.test

import com.github.shynixn.mccoroutine.fabric.launch
import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import llc.redstone.systemsapi.SystemsAPI
import llc.redstone.systemsapi.data.Action.*
import llc.redstone.systemsapi.data.GameMode
import llc.redstone.systemsapi.data.InventorySlot
import llc.redstone.systemsapi.data.ItemStack
import llc.redstone.systemsapi.data.Location
import llc.redstone.systemsapi.data.StatOp
import llc.redstone.systemsapi.data.StatValue
import llc.redstone.systemsapi.data.enums.Enchantment
import llc.redstone.systemsapi.data.enums.Lobby
import llc.redstone.systemsapi.data.enums.PotionEffect
import llc.redstone.systemsapi.data.enums.Sound
import llc.redstone.systemsapi.util.CommandUtils
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.minecraft.item.Items
import net.minecraft.nbt.NbtOps
import kotlin.jvm.optionals.getOrNull

class TestMod : ClientModInitializer {
    val MOD_ID = "testmod"
    val LOGGER: Logger = LoggerFactory.getLogger("TestMod")
    val VERSION = /*$ mod_version*/ "0.0.1";
    val MINECRAFT = /*$ minecraft*/ "1.21.9";
    val MC: MinecraftClient
        get() = MinecraftClient.getInstance()

    override fun onInitializeClient() {
        LOGGER.info("Loaded v$VERSION for Minecraft $MINECRAFT.")

        try {
            mcCoroutineConfiguration.minecraftExecutor = MinecraftClient.getInstance()
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }

        var ran = false
        ClientEntityEvents.ENTITY_LOAD.register { entity, world ->
            if (ran) {
                return@register
            }
            ran = true
            CommandUtils.runCommand("myhouses test", 200)
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
            dispatcher.register(
                literal("testmod")
                    .executes {
                        launch {
                            val function = SystemsAPI.getHousingImporter().getFunctionOrNull("test")!!
                            val itemstack = Items.BIRCH_STAIRS.defaultStack
                            val nbt =
                                net.minecraft.item.ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, itemstack).result()
                                    .getOrNull()?.asCompound()?.getOrNull()
                                    ?: error("Could not convert itemstack to nbt")
                            val testLocation = Location.Custom(
                                x = 1.0,
                                y = 2.0,
                                z = 3.0,
                                relX = false,
                                relY = false,
                                relZ = false,
                                relPitch = false,
                                relYaw = false,
                                pitch = 0f,
                                yaw = 0f,
                            )
                            function.getActionContainer().addActions(
                                listOf(
//                                    PlayerVariable(
//                                        variable = "hello",
//                                        op = StatOp.Dec,
//                                        amount = StatValue.I32(10),
//                                        unset = false
//                                    ),
//                                    ApplyPotionEffect(
//                                        effect = PotionEffect.Strength,
//                                        duration = 10,
//                                        level = 10,
//                                        override = true,
//                                        showIcon = false
//                                    ),
                                    GlobalVariable(
                                        variable = "hello",
                                        op = StatOp.Set,
                                        amount = StatValue.I32(2),
                                        unset = false
                                    ),
//                                    ClearAllPotionEffects(),
//                                    DisplayActionBar(message = "message hehe he ha"),
//                                    DisplayTitle(
//                                        title = "Hello World!",
//                                        subtitle = "This is a subtitle!",
//                                        fadeIn = 5,
//                                        stay = 2,
//                                        fadeOut = 3
//                                    ),
//                                    FailParkour(reason = "This is a reason!"),
//                                    FullHeal(),
//                                    GiveExperienceLevels(levels = 10),
//                                    GiveItem(
//                                        item = ItemStack(
//                                            nbt = nbt,
//                                            relativeFileLocation = "test"
//                                        ),
//                                        allowMultiple = false,
//                                        inventorySlot = InventorySlot(39),
//                                        replaceExistingItem = false
//                                    ),
//                                    KillPlayer(),
//                                    ParkourCheckpoint(),
//                                    PlaySound(
//                                        sound = Sound.AnvilLand,
//                                        volume = 0.7,
//                                        pitch = 1.0,
//                                        location = testLocation
//                                    ),
//                                    RemoveItem(
//                                        item = ItemStack(
//                                            nbt = nbt,
//                                            relativeFileLocation = "test"
//                                        )
//                                    ),
//                                    ResetInventory(),
//                                    SendMessage(message = "Hello there !This is a message"),
//                                    SendToLobby(location = Lobby.MainLobby),
//                                    SetCompassTarget(location = testLocation),
//                                    SetGameMode(gamemode = GameMode.Creative),
//                                    ChangeHunger(op = StatOp.Set, amount = 4.0),
//                                    ChangeMaxHealth(op = StatOp.Set, amount = 40.0, healOnChange = true),
//                                    TeleportPlayer(location = testLocation, preventInsideBlocks = false),
//                                    ExecuteFunction(name = "e", global = true),
//                                    UseHeldItem(),
//                                    EnchantHeldItem(enchantment = Enchantment.Protection, level = 10),
//                                    DisplayMenu(menu = "Your Statistics"),
//                                    CloseMenu(),
//                                    PauseExecution(ticks = 5),
//                                    SetPlayerTeam(team = "Blue"),
//                                    BalancePlayerTeam(),
                                )
                            )
                        }

                        1
                    }
            )
        }
    }
}