package llc.redstone.test

import com.github.shynixn.mccoroutine.fabric.launch
import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import com.mojang.brigadier.context.CommandContext
import llc.redstone.systemsapi.SystemsAPI
import llc.redstone.systemsapi.api.HouseSettings
import llc.redstone.systemsapi.api.Region
import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.data.StatOp
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.of
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import kotlin.time.Duration

class TestMod : ClientModInitializer {
    val MOD_ID = "testmod"
    val LOGGER: Logger = LoggerFactory.getLogger("TestMod")
    val VERSION = /*$ mod_version*/ "0.0.1"
    val MINECRAFT = /*$ minecraft*/ "1.21.9"
    val MC: MinecraftClient
        get() = MinecraftClient.getInstance()

    // helper lambda: sends label (dark blue) + value (light blue)
    fun CommandContext<FabricClientCommandSource>.sendFeedback(label: String, value: Any) {
            val darkBlue = TextColor.fromRgb(0x1C5796)   // darker blue
            val lightBlue = TextColor.fromRgb(0x48719E)  // lighter blue
            val labelText: MutableText = MutableText.of(of("$label: ")).setStyle(Style.EMPTY.withColor(darkBlue))
            val valueText: MutableText = MutableText.of(of(value.toString())).setStyle(Style.EMPTY.withColor(lightBlue))
            this.source.sendFeedback(labelText.append(valueText))
        }

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
                        it.source.sendFeedback(MutableText.of(of("Usage: /testmod <feature>")))
                        1
                    }
                    .then(
                        literal("house_settings").executes {
                            launch {
                                try {
                                    val settings = SystemsAPI.getHousingImporter().getHouseSettings()
                                    settings.setDaylightCycle(false)
                                    settings.setMaxPlayers(HouseSettings.MaxPlayers.ONE_TWENTY_FIVE)
                                    settings.setHouseTags(setOf(
                                        HouseSettings.HouseTag.GUILD_HANGOUT,
                                        HouseSettings.HouseTag.CHAT
                                    ))
                                    settings.setHouseLanguages(setOf(
                                        HouseSettings.HouseLanguage.ENGLISH,
                                        HouseSettings.HouseLanguage.NORWEGIAN,
                                        HouseSettings.HouseLanguage.SPANISH,
                                    ))
                                    settings.setParkourAnnounce(HouseSettings.ParkourAnnounce.OFF)
                                    settings.setPlayerStateDuration(Duration.parse("10h5s"))
                                    settings.setPlayerStateTypes(mutableMapOf(
                                        Pair(HouseSettings.PlayerStateType.POTIONS_METADATA, true),
                                        Pair(HouseSettings.PlayerStateType.LOCATION, true),
                                        Pair(HouseSettings.PlayerStateType.HEALTH, true),
                                        Pair(HouseSettings.PlayerStateType.PARKOUR, false)
                                    ))
                                    settings.setDefaultVariableDuration(Duration.parse("364d"))
                                        settings.setVariableDurationOverride("test", Duration.ZERO)
                                    settings.setPvpSettings(mutableMapOf(
                                        Pair(HouseSettings.PvpSettings.PVP, true),
                                        Pair(HouseSettings.PvpSettings.KEEP_INVENTORY, true),
                                        Pair(HouseSettings.PvpSettings.FIRE_DAMAGE, true)
                                    ))
                                    settings.setFishingSettings(mutableMapOf(
                                        Pair(HouseSettings.FishingSettings.ALLOW_USING_FISHING_RODS, true),
                                        Pair(HouseSettings.FishingSettings.PLAY_CAUGHT_SOUND, true),
                                        Pair(HouseSettings.FishingSettings.SHOW_CATCH_TIMER, true)
                                    ))

                                    it.sendFeedback("Daylight Cycle", settings.getDaylightCycle())
                                    it.sendFeedback("Max Players", settings.getMaxPlayers())
                                    it.sendFeedback("House Tags", settings.getHouseTags())
                                    it.sendFeedback("House Languages", settings.getHouseLanguages())
                                    it.sendFeedback("Parkour Announce", settings.getParkourAnnounce())
                                    it.sendFeedback("Player State Duration", settings.getPlayerStateDuration())
                                    it.sendFeedback("Player State Types", settings.getPlayerStateTypes())
                                    it.sendFeedback("Default Variable Duration", settings.getDefaultVariableDuration())
                                    it.sendFeedback("Test Variable Duration", settings.getVariableDurationOverride("test").toString())
                                    it.sendFeedback("PvP Settings", settings.getPvpSettings())
                                    it.sendFeedback("Fishing Settings", settings.getFishingSettings())
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
                    .then(
                        literal("regions").executes {
                            launch {
                                try {
                                    val region = SystemsAPI.getHousingImporter().getRegion("test") ?: throw Exception()
                                    region.setPvpSettings(
                                        mutableMapOf(
                                            Pair(Region.PvpSettings.PVP, true),
                                            Pair(Region.PvpSettings.KEEP_INVENTORY, true),
                                            Pair(Region.PvpSettings.FIRE_DAMAGE, null),
                                            Pair(Region.PvpSettings.FALL_DAMAGE, false)
                                        )
                                    )
                                    region.getEntryActionContainer().addActions(
                                        listOf(
                                            Action.FullHeal(),
                                            Action.ChangeHealth(10.0, StatOp.Set)
                                        )
                                    )
                                    region.getExitActionContainer().addActions(
                                        listOf(
                                            Action.FullHeal(),
                                            Action.ChangeHealth(10.0, StatOp.Set)
                                        )
                                    )

                                    it.sendFeedback("PvP Settings", region.getPvpSettings())
                                    it.sendFeedback("Entry Actions", region.getEntryActionContainer().getActions())
                                    it.sendFeedback("Exit Actions", region.getExitActionContainer().getActions())
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
            )
        }
    }
}