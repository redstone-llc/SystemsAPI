package llc.redstone.test

import llc.redstone.systemsapi.api.HouseProgress
import llc.redstone.systemsapi.api.ImportProgress
import llc.redstone.systemsapi.api.ProgressOutcome
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor
*///?}
//? if <1.21.11 {
/*import net.minecraft.resources.ResourceLocation
*///?} else {
import net.minecraft.resources.Identifier
//?}

/**
 * Draws the current import/export progress in the corner of the screen.
 *
 * Lives in the test mod rather than the library: polling [ImportProgress.current] every frame is how
 * a downstream consumer is meant to use the API, and it keeps the library free of a rendering
 * dependency.
 */
object ProgressHud {

    private const val X = 8
    private const val Y = 8
    private const val WIDTH = 180
    private const val BAR_HEIGHT = 6

    // Text colours are ARGB, and there is no implicit alpha fixup -- an alpha of zero draws nothing.
    private const val HEADING_COLOR = 0xFFFFFFFF.toInt()
    private const val DETAIL_COLOR = 0xFFAAAAAA.toInt()
    private const val PANEL_COLOR = 0x90000000.toInt()
    private const val TRACK_COLOR = 0xFF303030.toInt()

    //? if <1.21.11 {
    /*private val ID = ResourceLocation.fromNamespaceAndPath("testmod", "progress_hud")
    *///?} else {
    private val ID = Identifier.fromNamespaceAndPath("testmod", "progress_hud")
    //?}

    fun register() {
        HudElementRegistry.addLast(ID) { context, _ ->
            val progress = ImportProgress.current() ?: return@addLast
            render(context, progress)
        }
    }

    //? if <26.1 {
    private fun render(context: GuiGraphics, progress: HouseProgress) {
        val font = TestMod.MC.font

        val remaining = progress.remainingSeconds
            ?.let { if (progress.indeterminate) "~%.1fs".format(it) else "%.1fs".format(it) }
            ?: "--"

        val heading = "${progress.phase} ${(progress.fraction * 100f).toInt()}%  $remaining left"
        val detail = buildString {
            append(progress.completedSteps).append('/').append(progress.totalSteps)
            append("  elapsed ").append("%.1fs".format(progress.elapsedSeconds))
            progress.currentLabel?.let { append("  ").append(it) }
        }

        context.fill(X - 2, Y - 2, X + WIDTH + 2, Y + BAR_HEIGHT + 24, PANEL_COLOR)
        context.fill(X, Y, X + WIDTH, Y + BAR_HEIGHT, TRACK_COLOR)
        val filled = (WIDTH * progress.fraction).toInt().coerceIn(0, WIDTH)
        context.fill(X, Y, X + filled, Y + BAR_HEIGHT, colorFor(progress.outcome))

        context.drawString(font, heading, X, Y + BAR_HEIGHT + 3, HEADING_COLOR)
        context.drawString(font, detail, X, Y + BAR_HEIGHT + 14, DETAIL_COLOR)
    }
    //?} else {
    /*private fun render(context: GuiGraphicsExtractor, progress: HouseProgress) {
        val font = TestMod.MC.font

        val remaining = progress.remainingSeconds
            ?.let { if (progress.indeterminate) "~%.1fs".format(it) else "%.1fs".format(it) }
            ?: "--"

        val heading = "${progress.phase} ${(progress.fraction * 100f).toInt()}%  $remaining left"
        val detail = buildString {
            append(progress.completedSteps).append('/').append(progress.totalSteps)
            append("  elapsed ").append("%.1fs".format(progress.elapsedSeconds))
            progress.currentLabel?.let { append("  ").append(it) }
        }

        context.fill(X - 2, Y - 2, X + WIDTH + 2, Y + BAR_HEIGHT + 24, PANEL_COLOR)
        context.fill(X, Y, X + WIDTH, Y + BAR_HEIGHT, TRACK_COLOR)
        val filled = (WIDTH * progress.fraction).toInt().coerceIn(0, WIDTH)
        context.fill(X, Y, X + filled, Y + BAR_HEIGHT, colorFor(progress.outcome))

        context.text(font, heading, X, Y + BAR_HEIGHT + 3, HEADING_COLOR)
        context.text(font, detail, X, Y + BAR_HEIGHT + 14, DETAIL_COLOR)
    }
    *///?}

    private fun colorFor(outcome: ProgressOutcome) = when (outcome) {
        ProgressOutcome.RUNNING -> 0xFF48719E.toInt()
        ProgressOutcome.COMPLETED -> 0xFF4CAF50.toInt()
        ProgressOutcome.CANCELLED -> 0xFFB0873A.toInt()
        ProgressOutcome.FAILED -> 0xFFB04A4A.toInt()
    }
}
