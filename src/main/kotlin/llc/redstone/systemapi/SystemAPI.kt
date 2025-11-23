package llc.redstone.systemapi

import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SystemAPI : ClientModInitializer {
    val MOD_ID = "systemapi"
    val LOGGER: Logger = LoggerFactory.getLogger("Housing Toolbox")
    val VERSION = /*$ mod_version*/ "0.2.3";
    val MINECRAFT = /*$ minecraft*/ "1.21.9";
    val MC: MinecraftClient
        get() = MinecraftClient.getInstance()

    override fun onInitializeClient() {
        LOGGER.info("Loaded Housing Toolbox v$VERSION for Minecraft $MINECRAFT.")

        mcCoroutineConfiguration.minecraftExecutor = MinecraftClient.getInstance()
    }
}