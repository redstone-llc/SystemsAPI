package llc.redstone.test.tests

import com.github.shynixn.mccoroutine.fabric.launch
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import llc.redstone.systemsapi.SystemsAPI
import llc.redstone.systemsapi.api.Region
import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.data.StatOp
import llc.redstone.test.TestMod
import llc.redstone.test.TestMod.MC
import llc.redstone.test.TestMod.sendFeedback
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.of
import java.awt.Color

object RegionsTest {
    fun LiteralArgumentBuilder<FabricClientCommandSource>.withRegionsSubCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(
            literal("regions").executes { context ->
                execute(context)
            }
        )
    }

    fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        TestMod.launch {
            try {
                val region = SystemsAPI.getHousingImporter().getRegion("test")!!
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

                context.sendFeedback("PvP Settings", region.getPvpSettings())
                context.sendFeedback("Entry Actions", region.getEntryActionContainer().getActions())
                context.sendFeedback("Exit Actions", region.getExitActionContainer().getActions())
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

