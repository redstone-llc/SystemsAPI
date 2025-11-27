package llc.redstone.systemsapi.util

import com.mojang.brigadier.suggestion.Suggestions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import llc.redstone.systemsapi.SystemsAPI.MC
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object CommandUtils {
    fun runCommand(command: String, delay: Long = 0L) {
        if (delay == 0L) {
            MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand(command)
                ?: error("Could not run command $command")
        } else {
            CompletableFuture.runAsync({

                MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand(command)
                    ?: error("Could not run command $command")

            }, CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS))
        }
    }

    private var pending: CompletableDeferred<List<String>>? = null
    suspend fun getTabCompletions(baseCommand: String): List<String> {
        val partialCommand = buildString {
            append(if (baseCommand.startsWith('/')) baseCommand else "/$baseCommand")
            if (!endsWith(' ')) append(' ')
        }

        val deferred = CompletableDeferred<List<String>>()
        pending?.cancel()
        pending = deferred

        try {
            MC.networkHandler?.sendPacket(RequestCommandCompletionsC2SPacket(-1, partialCommand))
                ?: error("Could not access Network Handler while looking for '$partialCommand...' tab completions.")
            return withTimeout(1_000) { deferred.await() }
        } catch (e: TimeoutCancellationException) {
            if (pending === deferred) pending = null
            error("[getTabCompletions] Timed out waiting for '$partialCommand...' tab completions.")
        } finally {
            if (pending === deferred) pending = null
        }
    }

    fun handleSuggestions(suggestions: Suggestions) {
        pending?.let { current ->
            pending = null
            current.complete(suggestions.list.map { it.text })
        }
    }
}