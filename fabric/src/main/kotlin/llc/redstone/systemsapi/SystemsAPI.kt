package llc.redstone.systemsapi

import dev.isxander.yacl3.config.v3.value
import kotlinx.coroutines.*
import llc.redstone.systemsapi.api.House
import llc.redstone.systemsapi.config.SystemsAPISettings
import llc.redstone.systemsapi.coroutine.MCCoroutineImpl
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

object SystemsAPI : ClientModInitializer {
    internal val MOD_ID = "systemsapi"
    internal val LOGGER: Logger = LoggerFactory.getLogger("SystemsAPI")
    internal val VERSION = /*$ mod_version*/ "0.0.1";
    internal val MINECRAFT = /*$ minecraft*/ "1.21.9";
    internal val MC: MinecraftClient
        get() = MinecraftClient.getInstance()

    var minecraftDispatcher = MCCoroutineImpl.getCoroutineSession(this).dispatcherMinecraft
    var scope: CoroutineScope = MCCoroutineImpl.getCoroutineSession(this).scope
    var mcCoroutineConfiguration = MCCoroutineImpl.getCoroutineSession(this).mcCoroutineConfiguration

    override fun onInitializeClient() {
        LOGGER.info("Loaded v$VERSION for Minecraft $MINECRAFT.")

        mcCoroutineConfiguration.minecraftExecutor = MinecraftClient.getInstance()
    }

    fun getHousingImporter(): House {
        return llc.redstone.systemsapi.importer.HouseImporter
    }

    suspend fun scaledDelay(mul: Double = 1.0) = delay((SystemsAPISettings.clickDelayBase.value * mul).toLong())

    fun launch(
        context: CoroutineContext = minecraftDispatcher,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        if (!scope.isActive) {
            return Job()
        }

        return scope.launch(context, start, block)
    }
}