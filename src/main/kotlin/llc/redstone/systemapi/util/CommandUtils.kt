package llc.redstone.systemapi.util

import com.mojang.brigadier.suggestion.Suggestions
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeoutOrNull
import llc.redstone.systemapi.SystemAPI.MC
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

    private var pendingCompletion: CompletableFuture<List<String>>? = null
    suspend fun getTabCompletions(baseCommand: String): List<String> {
        val partialCommand = buildString {
            append(if (baseCommand.startsWith('/')) baseCommand else "/$baseCommand")
            if (!endsWith(' ')) append(' ')
        }

        val deferred = CompletableFuture<List<String>>()
        pendingCompletion = deferred

        MC.networkHandler?.sendPacket(RequestCommandCompletionsC2SPacket(-1, partialCommand))
            ?: error("Could not access Network Handler while looking for '$partialCommand...' tab completions.")

        val result = withTimeoutOrNull(1000) {
            try {
                deferred.await()
            } finally {
                pendingCompletion = null
            }
        }

        // Probably not necessary, but I don't want false negatives
        if (result == null) pendingCompletion = null
        return result ?: error("Tab completions for command '$partialCommand...' returned null.")
    }

    fun handleSuggestions(suggestions: Suggestions) {
        if (pendingCompletion != null && !pendingCompletion!!.isDone) pendingCompletion!!.complete(suggestions.list.map { it.text })
    }
}