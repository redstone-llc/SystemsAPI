@file:Suppress("UnstableApiUsage")

package llc.redstone.systemsapi.config

import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.dsl.*
import llc.redstone.systemsapi.SystemsAPI
import net.minecraft.client.gui.screen.Screen

fun createSettingsGui(parent: Screen?) = SettingsGuiFactory().createSettingsGui(parent)

private class SettingsGuiFactory {
    val settings = SystemsAPISettings(SystemsAPISettings)

    fun createSettingsGui(parent: Screen?) = YetAnotherConfigLib(SystemsAPI.MOD_ID) {
        save(SystemsAPISettings::saveToFile)

        val importer by categories.registering {
            val menu by groups.registering {
                options.register(SystemsAPISettings.menuTimeout) {
                    defaultDescription()
                    controller = numberField(0, 10000)
                }
                options.register(SystemsAPISettings.menuItemLoadedTimeout) {
                    defaultDescription()
                    controller = numberField(0, 10000)
                }
                options.register(SystemsAPISettings.menuItemTimeout) {
                    defaultDescription()
                    controller = numberField(0, 10000)
                }
                options.register(SystemsAPISettings.previousInputTimeout) {
                    defaultDescription()
                    controller = numberField(0, 10000)
                }
            }
        }
    }.generateScreen(parent)

}

private fun OptionDsl<*>.defaultDescription() {
    descriptionBuilder {
        addDefaultText()
    }
}

private fun ButtonOptionDsl.defaultDescription() {
    descriptionBuilder {
        addDefaultText()
    }
}