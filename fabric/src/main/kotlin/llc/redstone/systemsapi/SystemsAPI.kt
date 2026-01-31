@file:Suppress("UnstableApiUsage")

package llc.redstone.systemsapi

import kotlinx.coroutines.*
import llc.redstone.systemsapi.api.House
import llc.redstone.systemsapi.config.SystemsAPIConfig
import llc.redstone.systemsapi.coroutine.MCCoroutineImpl.mcCoroutineConfiguration
import llc.redstone.systemsapi.coroutine.MCCoroutineImpl.minecraftDispatcher
import llc.redstone.systemsapi.coroutine.MCCoroutineImpl.scope
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

object SystemsAPI : ClientModInitializer {
    internal const val MOD_ID = "systemsapi"
    internal val LOGGER: Logger = LoggerFactory.getLogger("SystemsAPI")
    internal const val VERSION = /*$ mod_version*/ "0.1.11";
    internal const val MINECRAFT = /*$ minecraft*/ "1.21.9";
    internal val CONFIG = SystemsAPIConfig.createAndLoad()
    internal val MC: MinecraftClient
        get() = MinecraftClient.getInstance()

    override fun onInitializeClient() {
        LOGGER.info("Loaded v$VERSION for Minecraft $MINECRAFT.")

        mcCoroutineConfiguration.minecraftExecutor = MinecraftClient.getInstance()
    }

    fun getHousingImporter(): House {
        return llc.redstone.systemsapi.importer.HouseImporter
    }

    suspend fun scaledDelay(mul: Double = 1.0) = delay((CONFIG.baseClickDelay * mul).toLong())

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