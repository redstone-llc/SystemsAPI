@file:Suppress("UnstableApiUsage")

package llc.redstone.systemsapi

import kotlinx.coroutines.*
import llc.redstone.systemsapi.api.House
import llc.redstone.systemsapi.config.SystemsAPIConfig
import llc.redstone.systemsapi.coroutine.MCCoroutineImpl.mcCoroutineConfiguration
import llc.redstone.systemsapi.coroutine.MCCoroutineImpl.minecraftDispatcher
import llc.redstone.systemsapi.coroutine.MCCoroutineImpl.scope
import llc.redstone.systemsapi.hook.DynamicFPSHook
import llc.redstone.systemsapi.progress.CalibrationStore
import llc.redstone.systemsapi.progress.CostModel
import llc.redstone.systemsapi.progress.OpKind
import llc.redstone.systemsapi.progress.OpRecorder
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

//? if >=26.2 {
/*val Minecraft.screen: net.minecraft.client.gui.screens.Screen?
    get() = this.gui.screen()
*///?}

object SystemsAPI : ClientModInitializer {
    internal const val MOD_ID = "systemsapi"
    internal val LOGGER: Logger = LoggerFactory.getLogger("SystemsAPI")
    internal const val VERSION = /*$ mod_version*/ "0.2.16";
    internal const val MINECRAFT = /*$ minecraft*/ "1.21.11";
    internal val CONFIG = SystemsAPIConfig.createAndLoad()
    internal val MC: Minecraft
        get() = Minecraft.getInstance()
    internal var DYNAMIC_FPS: DynamicFPSHook? = null
    init {
        mcCoroutineConfiguration.minecraftExecutor = Minecraft.getInstance()
    }

    override fun onInitializeClient() {


        if (FabricLoader.getInstance().isModLoaded("dynamic_fps")) {
            DYNAMIC_FPS = DynamicFPSHook()
        }

        // Seeds the timing model from the last session.
        if (CONFIG.persistCalibration) {
            CalibrationStore.load()?.let {
                CostModel.load(it)
                LOGGER.info("Loaded import timing calibration.")
            }
        }

        LOGGER.info("Loaded v$VERSION for Minecraft $MINECRAFT.")
    }

    fun getHousingImporter(): House {
        return llc.redstone.systemsapi.importer.HouseImporter
    }

    suspend fun scaledDelay(mul: Double = 1.0) {
        val ms = (CONFIG.baseClickDelay * mul).toLong()
        delay(ms)
        // Suppressed when nested, since the delays inside onOpen and the paginated find loop are
        // already part of those operations' cost.
        OpRecorder.flat(OpKind.FIXED_DELAY, ms)
    }

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