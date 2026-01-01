@file:Suppress("UnstableApiUsage")

package llc.redstone.systemsapi.config

import com.mojang.serialization.Codec
import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.config.v3.value
import llc.redstone.systemsapi.SystemsAPI.MOD_ID
import net.fabricmc.loader.api.FabricLoader

open class SystemsAPISettings(): JsonFileCodecConfig<SystemsAPISettings>(
    FabricLoader.getInstance().configDir.resolve("${MOD_ID}.json")
) {
    val clickDelayBase by register<Long>(default = 50, Codec.LONG)
    val menuTimeout by register<Long>(default = 1000, Codec.LONG)
    val menuItemLoadedTimeout by register<Long>(default = 200, Codec.LONG)
    val menuItemTimeout by register<Long>(default = 1000, Codec.LONG)
    val previousInputTimeout by register<Long>(default = 1000, Codec.LONG)

    var firstLaunch = false
    val _firstLaunch by register<Boolean>(default = true, Codec.BOOL)

    final val allSettings = arrayOf(
        menuTimeout,
        menuItemLoadedTimeout,
        menuItemTimeout,
        previousInputTimeout,
        _firstLaunch
    )

    constructor(settings: SystemsAPISettings) : this() {
        this.clickDelayBase.value = settings.clickDelayBase.value
        this.menuTimeout.value = settings.menuTimeout.value
        this.menuItemLoadedTimeout.value = settings.menuItemLoadedTimeout.value
        this.menuItemTimeout.value = settings.menuItemTimeout.value
        this.previousInputTimeout.value = settings.previousInputTimeout.value
        this._firstLaunch.value = settings._firstLaunch.value
    }

    companion object : SystemsAPISettings() {
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
}