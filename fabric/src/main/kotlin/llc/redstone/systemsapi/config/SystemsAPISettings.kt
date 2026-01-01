@file:Suppress("UnstableApiUsage")

package llc.redstone.systemsapi.config

import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.config.v3.value
import llc.redstone.systemsapi.SystemsAPI.MOD_ID
import net.fabricmc.loader.api.FabricLoader

object SystemsAPISettings : JsonFileCodecConfig<SystemsAPISettings>(
    FabricLoader.getInstance().configDir.resolve("${MOD_ID}.json")
) {
    val baseClickDelay by register(default = 50, LONG)

    val menuTimeout by register(default = 1000, LONG)
    val menuItemLoadedTimeout by register(default = 1000, LONG)
    val menuItemTimeout by register(default = 1000, LONG)
    val previousInputTimeout by register(default = 1000, LONG)

    private val _firstLaunch by register(default = true, BOOL)
    var firstLaunch = false
        private set

    init {
        if (!loadFromFile()) {
            saveToFile()
        }

        if (_firstLaunch.value) {
            firstLaunch = true
            _firstLaunch.value = false
            saveToFile()
        }
    }
}