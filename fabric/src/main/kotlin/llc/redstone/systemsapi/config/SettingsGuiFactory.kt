@file:Suppress("UnstableApiUsage")

package llc.redstone.systemsapi.config

import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.dsl.*
import llc.redstone.systemsapi.SystemsAPI
import net.minecraft.client.gui.screen.Screen

fun createSettingsGui(parent: Screen?): Screen = YetAnotherConfigLib(SystemsAPI.MOD_ID) {
    save(SystemsAPISettings::saveToFile)

    categories.register("importer") {
        groups.register("menu") {
            defaultDescription()

            options.register(SystemsAPISettings.baseClickDelay) {
                defaultDescription(2)
                controller = slider(0..1000L, step = 10L)
            }
        }
        groups.register("timeouts") {
            defaultDescription()

            options.register(SystemsAPISettings.menuTimeout) {
                defaultDescription()
                controller = numberField(0, 10000L)
            }
            options.register(SystemsAPISettings.menuItemLoadedTimeout) {
                defaultDescription()
                controller = numberField(0, 10000L)
            }
            options.register(SystemsAPISettings.menuItemTimeout) {
                defaultDescription()
                controller = numberField(0, 10000L)
            }
            options.register(SystemsAPISettings.previousInputTimeout) {
                defaultDescription()
                controller = numberField(0, 10000L)
            }
        }
    }
}.generateScreen(parent)

private fun GroupDsl.defaultDescription(lines: Int? = null) = descriptionBuilder {
    addDefaultText(lines)
}

private fun OptionDsl<*>.defaultDescription(lines: Int? = null) = descriptionBuilder {
    addDefaultText(lines)
}