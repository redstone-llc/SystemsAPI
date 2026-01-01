package llc.redstone.test.tests

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import llc.redstone.systemsapi.SystemsAPI
import llc.redstone.systemsapi.api.HouseSettings
import llc.redstone.test.TestMod
import llc.redstone.test.TestMod.MC
import llc.redstone.test.TestMod.sendFeedback
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.of
import java.awt.Color
import kotlin.time.Duration

object HouseSettingsTest {
    fun LiteralArgumentBuilder<FabricClientCommandSource>.withHouseSettingsSubCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(
            literal("house_settings").executes { context ->
                execute(context)
            }
        )
    }

    fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        SystemsAPI.launch {
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

                context.sendFeedback("Daylight Cycle", settings.getDaylightCycle())
                context.sendFeedback("Max Players", settings.getMaxPlayers())
                context.sendFeedback("House Tags", settings.getHouseTags())
                context.sendFeedback("House Languages", settings.getHouseLanguages())
                context.sendFeedback("Parkour Announce", settings.getParkourAnnounce())
                context.sendFeedback("Player State Duration", settings.getPlayerStateDuration())
                context.sendFeedback("Player State Types", settings.getPlayerStateTypes())
                context.sendFeedback("Default Variable Duration", settings.getDefaultVariableDuration())
                context.sendFeedback("Test Variable Duration", settings.getVariableDurationOverride("test").toString())
                context.sendFeedback("PvP Settings", settings.getPvpSettings())
                context.sendFeedback("Fishing Settings", settings.getFishingSettings())
            } catch (e: Exception) {
                e.printStackTrace()
                MC.player?.sendMessage(
                    MutableText.of(
                        of("[Test Mod] An error occurred: ${e.message}")
                    ).withColor(Color.RED.rgb), false
                )
            }
        }
        return 1
    }
}
