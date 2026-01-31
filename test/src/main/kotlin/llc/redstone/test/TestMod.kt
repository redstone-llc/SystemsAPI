package llc.redstone.test

import com.mojang.brigadier.context.CommandContext
import llc.redstone.test.tests.Function.withFunctionSubCommand
import llc.redstone.test.tests.GroupsTest.withGroupsSubCommand
import llc.redstone.test.tests.HouseSettingsTest.withHouseSettingsSubCommand
import llc.redstone.test.tests.RegionsTest.withRegionsSubCommand
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

object TestMod : ClientModInitializer {
    const val MOD_ID = "testmod"
    val LOGGER: Logger = LoggerFactory.getLogger("TestMod")
    const val VERSION = /*$ mod_version*/ "0.0.1";
    const val MINECRAFT = /*$ minecraft*/ "1.21.9";
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


        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
            dispatcher.register(
                literal("testmod")
                    .executes {
                        it.source.sendFeedback(MutableText.of(of("Usage: /testmod <feature>")))
                        1
                    }
                    .withFunctionSubCommand()
                    .withHouseSettingsSubCommand()
                    .withRegionsSubCommand()
                    .withGroupsSubCommand()
            )
        }
    }
}