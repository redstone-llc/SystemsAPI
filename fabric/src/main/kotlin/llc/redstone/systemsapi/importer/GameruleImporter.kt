package llc.redstone.systemsapi.importer

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import llc.redstone.systemsapi.api.Gamerule
import llc.redstone.systemsapi.util.CommandUtils

object GameruleImporter : Gamerule {

    // TODO: Implement cooldown to avoid triggering the real one in-game

    internal var pendingChat: CompletableDeferred<Boolean>? = null
    override suspend fun getGamerule(gamerule: Gamerule.Gamerules): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        pendingChat?.cancel()
        pendingChat = deferred

        return try {
            CommandUtils.runCommand("gamerule ${gamerule.displayName}")
            withTimeout(1000) { deferred.await() }
        } finally {
            if (pendingChat === deferred) pendingChat = null
        }
    }
    internal fun receiveChat(value: Boolean) {
        pendingChat?.let { current ->
            pendingChat = null
            current.complete(value)
        }
    }

    override suspend fun setGamerule(gamerule: Gamerule.Gamerules, newValue: Boolean) {
        val value = if (newValue) "enabled" else "disabled"
        CommandUtils.runCommand("gamerule ${gamerule.displayName} $value")
    }

}