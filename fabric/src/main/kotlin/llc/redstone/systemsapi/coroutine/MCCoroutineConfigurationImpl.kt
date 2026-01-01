package llc.redstone.systemsapi.coroutine

import java.util.concurrent.Executor
import java.util.logging.Logger


class MCCoroutineConfigurationImpl(private val extension: Any, private val mcCoroutine: MCCoroutineImpl) {
    /**
     * Strategy handling how MCCoroutine is disposed.
     * Defaults to ShutdownStrategy.MANUAL.
     */
    var shutdownStrategy: ShutdownStrategy = ShutdownStrategy.MANUAL

    /**
     * The executor being used to schedule tasks on the main thread of minecraft.
     * Can be retrieved from the MinecraftServer instance.
     */
    var minecraftExecutor: Executor = Executor {
        throw RuntimeException("You need to set the minecraft scheduler to MCCoroutine. e.g. ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server ->  mcCoroutineConfiguration.minecraftExecutor = Executor { r -> server.submitAndJoin(r)}})")
    }

    /**
     * The logger being used by MCCoroutine.
     */
    var logger: Logger = Logger.getLogger(extension.javaClass.simpleName)

    /**
     * Manually disposes the MCCoroutine session for the given plugin.
     */
    fun disposePluginSession() {
        mcCoroutine.disable(extension)
    }
}