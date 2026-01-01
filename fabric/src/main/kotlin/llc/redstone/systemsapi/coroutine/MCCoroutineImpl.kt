package llc.redstone.systemsapi.coroutine

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
}