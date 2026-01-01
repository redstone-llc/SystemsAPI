package llc.redstone.systemsapi.coroutine

enum class ShutdownStrategy {
    /**
     * Default shutdown strategy.
     * The coroutine session needs to be explicitly disposed.
     */
    MANUAL
}