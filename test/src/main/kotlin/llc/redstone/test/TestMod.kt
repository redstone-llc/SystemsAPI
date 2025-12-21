package llc.redstone.test

import com.github.shynixn.mccoroutine.fabric.launch
import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import llc.redstone.systemsapi.SystemsAPI
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.of
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

class TestMod : ClientModInitializer {
    val MOD_ID = "testmod"
    val LOGGER: Logger = LoggerFactory.getLogger("TestMod")
    val VERSION = /*$ mod_version*/ "0.0.1"
    val MINECRAFT = /*$ minecraft*/ "1.21.9"
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
                                val layout = SystemsAPI.getHousingImporter().getInventoryLayout("test")
                                layout?.setInventory(arrayOf(
                                    Items.GOLDEN_APPLE.defaultStack,
                                    Items.BEACON.defaultStack,
                                    Items.GOLDEN_APPLE.defaultStack,
                                    Items.BEACON.defaultStack,
                                    Items.GOLDEN_AXE.defaultStack,
                                    ItemStack.EMPTY,
                                    ItemStack.EMPTY,
                                    Items.GOLDEN_APPLE.defaultStack,
                                    Items.BEACON.defaultStack,
                                    Items.GOLDEN_APPLE.defaultStack,
                                    Items.BEACON.defaultStack,
                                    Items.GOLDEN_AXE.defaultStack,
                                    ItemStack.EMPTY,
                                    ItemStack.EMPTY,
                                    Items.GOLDEN_APPLE.defaultStack,
                                    Items.BEACON.defaultStack,
                                    Items.GOLDEN_APPLE.defaultStack,
                                    Items.BEACON.defaultStack,
                                    Items.GOLDEN_AXE.defaultStack,
                                    ItemStack.EMPTY,
                                    ItemStack.EMPTY
                                ))
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