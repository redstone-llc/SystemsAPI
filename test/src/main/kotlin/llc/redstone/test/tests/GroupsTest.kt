package llc.redstone.test.tests

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import llc.redstone.systemsapi.SystemsAPI
import llc.redstone.systemsapi.api.Group
import llc.redstone.test.TestMod
import llc.redstone.test.TestMod.MC
import llc.redstone.test.TestMod.sendFeedback
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.of
import java.awt.Color

object GroupsTest {
    fun LiteralArgumentBuilder<FabricClientCommandSource>.withGroupsSubCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(
            literal("groups").executes { context ->
                execute(context)
            }
        )
    }

    fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        SystemsAPI.launch {
            try {
                val group = SystemsAPI.getHousingImporter().getGroup("test")!!

                group.setName("glorb")
                group.setTag("glorb")
                group.setTagVisibleInChat(false)
                group.setColor(Group.GroupColor.LIGHT_PURPLE)
                group.setPriority(20)

                context.sendFeedback("Name", group.getName())
                context.sendFeedback("Tag", group.getTag().toString())
                context.sendFeedback("Tag Visible in Chat", group.getTagVisibleInChat())
                context.sendFeedback("Color", group.getColor())
                context.sendFeedback("Priority", group.getPriority())
                context.sendFeedback("Permissions", group.getPermissions())
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

