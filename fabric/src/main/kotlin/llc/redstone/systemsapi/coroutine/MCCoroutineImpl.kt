package llc.redstone.systemsapi.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import llc.redstone.systemsapi.SystemsAPI
import kotlin.coroutines.CoroutineContext

object MCCoroutineImpl {
    private val items = HashMap<Any, CoroutineSessionImpl>()

    /**
     * Get coroutine session for the given mod.
     * When using an extension, coroutine scope is bound to the lifetime of the extension.
     */
    fun getCoroutineSession(handler: Any): CoroutineSessionImpl {
        if (!items.containsKey(handler)) {
            startCoroutineSession(handler)
        }

        return items[handler]!!
    }

    /**
     * Disposes the given coroutine session.
     */
    fun disable(handler: Any) {
        if (!items.containsKey(handler)) {
            return
        }

        val session = items[handler]!!
        session.dispose()
        items.remove(handler)
    }

    /**
     * Starts a new coroutine session.
     */
    private fun startCoroutineSession(extension: Any) {
        val mcCoroutineConfiguration = MCCoroutineConfigurationImpl(extension, this)
        items[extension] = CoroutineSessionImpl(extension, mcCoroutineConfiguration)
    }

    val SystemsAPI.minecraftDispatcher: CoroutineContext
        get() = getCoroutineSession(this).dispatcherMinecraft
    val SystemsAPI.scope: CoroutineScope
        get() = getCoroutineSession(this).scope
    val SystemsAPI.mcCoroutineConfiguration: MCCoroutineConfigurationImpl
        get() = getCoroutineSession(this).mcCoroutineConfiguration
}