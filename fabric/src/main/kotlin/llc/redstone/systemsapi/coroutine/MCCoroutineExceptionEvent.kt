package llc.redstone.systemsapi.coroutine

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

/**
 * A Fabric event which is called when an exception is raised in one of the coroutines managed by MCCoroutine.
 * Cancelling this exception causes the error to not get logged and offers to possibility for custom logging.
 */
@FunctionalInterface
fun interface MCCoroutineExceptionEvent {
    companion object {
        val EVENT: Event<MCCoroutineExceptionEvent> =
            EventFactory.createArrayBacked(MCCoroutineExceptionEvent::class.java) { listeners ->
                MCCoroutineExceptionEvent { throwable, entryPoint ->

                    /**
                     * Gets called from MCCoroutine with the occurred [throwable] in the given scope [entryPoint].
                     * @return True If the event should be cancelled (not get logged) or false if the event should not be cancelled.
                     */
                    var cancel = false

                    for (listener in listeners) {
                        val result = listener.onMCCoroutineException(throwable, entryPoint)
                        if (result) {
                            cancel = true
                        }
                    }

                    cancel
                }
            }
    }

    /**
     * Gets called from MCCoroutine with the occurred [throwable] in the given scope [entryPoint].
     * @return True If the event should be cancelled (not get logged) or false if the event should not be cancelled.
     */
    fun onMCCoroutineException(throwable: Throwable, entryPoint: Any): Boolean
}