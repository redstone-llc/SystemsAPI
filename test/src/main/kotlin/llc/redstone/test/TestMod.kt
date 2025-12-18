package llc.redstone.test

import com.github.shynixn.mccoroutine.fabric.launch
import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import llc.redstone.systemsapi.SystemsAPI
import llc.redstone.systemsapi.data.Action.*
import llc.redstone.systemsapi.data.Comparison
import llc.redstone.systemsapi.data.Condition.*
import llc.redstone.systemsapi.data.GameMode
import llc.redstone.systemsapi.data.InventoryLocation
import llc.redstone.systemsapi.data.InventorySlot
import llc.redstone.systemsapi.data.ItemAmount
import llc.redstone.systemsapi.data.ItemCheck
import llc.redstone.systemsapi.data.ItemStack
import llc.redstone.systemsapi.data.Location
import llc.redstone.systemsapi.data.StatOp
import llc.redstone.systemsapi.data.StatValue
import llc.redstone.systemsapi.data.Time
import llc.redstone.systemsapi.data.Weather
import llc.redstone.systemsapi.data.enums.Enchantment
import llc.redstone.systemsapi.data.enums.Lobby
import llc.redstone.systemsapi.data.enums.Permission
import llc.redstone.systemsapi.data.enums.PotionEffect
import llc.redstone.systemsapi.data.enums.Sound
import llc.redstone.systemsapi.util.ItemConverterUtils
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*
import net.minecraft.item.Items
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.of
import java.awt.Color

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


        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
            dispatcher.register(
                literal("testmod")
                    .executes {
                        launch {
                            try {
                                val itemstack = Items.BIRCH_STAIRS.defaultStack
                                val nbt = ItemConverterUtils.toNBT(itemstack)
                                val function = SystemsAPI.getHousingImporter().getFunction("test2")!!
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
                                val actions = listOf(
                                    ChangePlayerGroup(
                                        newGroup = "test",
                                        includeHigherGroups = true
                                    ),
                                    PlayerVariable(
                                        variable = "hello",
                                        op = StatOp.Dec,
                                        amount = StatValue.I32(10),
                                        unset = false
                                    ),
                                    ApplyPotionEffect(
                                        effect = PotionEffect.Strength,
                                        duration = 10,
                                        level = 10,
                                        override = true,
                                        showIcon = false
                                    ),
                                    ChangeHealth(
                                        op = StatOp.Inc,
                                        amount = 5.0
                                    ),
                                    ApplyInventoryLayout(
                                        layout = "test"
                                    ),
                                    DropItem(
                                        item = ItemStack(
                                            nbt = nbt,
                                            relativeFileLocation = "test"
                                        ),
                                        location = testLocation,
                                        dropNaturally = true,
                                        disableMerging = true,
                                        prioritizePlayer = true,
                                        inventoryFallback = true,
                                        despawnDurationTicks = 20,
                                        pickupDelayTicks = 20
                                    ),
                                    ChangeVelocity(
                                        0.1,
                                        0.2,
                                        0.3
                                    ),
                                    LaunchToTarget(
                                        location = testLocation,
                                        strength = 1.5,
                                    ),
                                    SetPlayerWeather(
                                        Weather.RAINING
                                    ),
                                    SetPlayerTime(
                                        Time.Custom(12000)
                                    ),
                                    ToggleNametagDisplay(
                                        displayNametag = false
                                    ),
                                    GlobalVariable(
                                        variable = "hello",
                                        op = StatOp.Set,
                                        amount = StatValue.I32(2),
                                        unset = false
                                    ),
                                    TeamVariable(
                                        teamName = "test",
                                        variable = "test",
                                        op = StatOp.Set,
                                        amount = StatValue.I32(5),
                                        unset = false
                                    ),
                                    ClearAllPotionEffects(),
                                    DisplayActionBar(message = "message hehe he ha"),
                                    DisplayTitle(
                                        title = "Hello World!",
                                        subtitle = "This is a subtitle!",
                                        fadeIn = 5,
                                        stay = 2,
                                        fadeOut = 3
                                    ),
                                    FailParkour(reason = "This is a reason!"),
                                    FullHeal(),
                                    GiveExperienceLevels(levels = 10),
                                    GiveItem(
                                        item = ItemStack(
                                            nbt = nbt,
                                            relativeFileLocation = "test"
                                        ),
                                        allowMultiple = false,
                                        inventorySlot = InventorySlot.HelmetSlot(),
                                        replaceExistingItem = false
                                    ),
                                    KillPlayer(),
                                    ParkourCheckpoint(),
                                    PlaySound(
                                        sound = Sound.AnvilLand,
                                        volume = 0.8,
                                        pitch = 1.1,
                                        location = testLocation
                                    ),
                                    RemoveItem(
                                        item = ItemStack(
                                            nbt = nbt,
                                            relativeFileLocation = "test"
                                        )
                                    ),
                                    ResetInventory(),
                                    SendMessage(message = "Hello there !This is a message"),
                                    SendToLobby(location = Lobby.MainLobby),
                                    SetCompassTarget(location = testLocation),
                                    SetGameMode(gamemode = GameMode.Creative),
                                    ChangeHunger(op = StatOp.Set, amount = 4.0),
                                    ChangeMaxHealth(op = StatOp.Set, amount = 40.0, healOnChange = true),
                                    TeleportPlayer(location = testLocation, preventInsideBlocks = false),
                                    ExecuteFunction(name = "e", global = true),
                                    EnchantHeldItem(enchantment = Enchantment.Protection, level = 10),
                                    DisplayMenu(menu = "test"),
                                    PauseExecution(ticks = 5),
                                    SetPlayerTeam(team = "test"),
                                )
                                val conditions = listOf(
                                    PlayerVariableRequirement(
                                        variable = "test",
                                        op = Comparison.Eq,
                                        value = StatValue.I32(5),
                                    ),
                                    GlobalVariableRequirement(
                                        variable = "test",
                                        op = Comparison.Le,
                                        value = StatValue.I32(10),
                                    ),
                                    TeamVariableRequirement(
                                        team = "test",
                                        variable = "test",
                                        op = Comparison.Ge,
                                        value = StatValue.I32(15),
                                    ),
                                    RequiredGroup(
                                        group = "test",
                                        includeHigherGroups = true,
                                    ),
                                    HasPermission(
                                        permission = Permission.EditCommands
                                    ),
                                    InRegion(
                                        region = "test"
                                    ),
                                    HasItem(
                                        item = ItemStack(
                                            nbt = nbt,
                                            relativeFileLocation = "test"
                                        ),
                                        whatToCheck = ItemCheck.ItemType,
                                        whereToCheck = InventoryLocation.Anywhere,
                                        amount = ItemAmount.Ge,
                                    ),
                                    InParkour(),
                                    RequiredEffect(
                                        effect = PotionEffect.Poison
                                    ),
                                    PlayerSneaking(),
                                    PlayerFlying(),
                                    RequiredHealth(
                                        mode = Comparison.Ge,
                                        amount = 10.0
                                    ),
                                    RequiredMaxHealth(
                                        mode = Comparison.Eq,
                                        amount = 20.0
                                    ),
                                    RequiredHungerLevel(
                                        mode = Comparison.Le,
                                        amount = 15.0
                                    ),
                                    RequiredGameMode(
                                        gameMode = GameMode.Survival
                                    ),
                                    RequiredPlaceholderNumber(
                                        placeholder = "%test%",
                                        mode = Comparison.Eq,
                                        amount = StatValue.I32(10),
                                    ),
                                    RequiredTeam(
                                        team = "test"
                                    ),
                                )
                                try {
//                                function.getActionContainer().addActions(
//                                    listOf(
//                                        Conditional(
//                                            conditions = conditions,
//                                            matchAnyCondition = false,
//                                            ifActions = actions,
//                                            elseActions = actions
//                                        ),
//                                        RandomAction(
//                                            actions = actions
//                                        )
//                                    )
//
//                                )
                                    val randomActions = actions.mapNotNull {
                                        if (Math.random() < 0.3) {
                                            it
                                        } else {
                                            null
                                        }
                                    }

                                    val actions = listOf(
                                        GiveItem(
                                            item = ItemStack(
                                                nbt = nbt,
                                                relativeFileLocation = "test"
                                            ),
                                            allowMultiple = false,
                                            inventorySlot = InventorySlot.HelmetSlot(),
                                            replaceExistingItem = false
                                        )
                                    )

                                    function.getActionContainer().getActions().forEach {
                                        println(it)
                                    }
                                } catch (e: Exception) {
                                    MC.player?.sendMessage(
                                        MutableText.of(
                                            of("[Test Mod] An error occurred: ${e.message}")
                                        ).withColor(Color.RED.rgb), false
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                MC.player?.sendMessage(
                                    MutableText.of(
                                        of("[Test Mod] An error occurred: ${e.message}")
                                    ).withColor(Color.RED.rgb), false
                                )
                            }
                        }
                        1
                    }
            )
        }
    }
}