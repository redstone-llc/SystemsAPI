package llc.redstone.systemsapi

import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import llc.redstone.systemsapi.api.House
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SystemsAPI : ClientModInitializer {
    internal val MOD_ID = "systemsapi"
    internal val LOGGER: Logger = LoggerFactory.getLogger("SystemsAPI")
    internal val VERSION = /*$ mod_version*/ "0.0.1";
    internal val MINECRAFT = /*$ minecraft*/ "1.21.9";
    val MC: MinecraftClient
        get() = MinecraftClient.getInstance()

    override fun onInitializeClient() {
        LOGGER.info("Loaded v$VERSION for Minecraft $MINECRAFT.")

        mcCoroutineConfiguration.minecraftExecutor = MinecraftClient.getInstance()
    }

    fun getHousingImporter(): House {
        return llc.redstone.systemsapi.importer.HouseImporter
    }
}