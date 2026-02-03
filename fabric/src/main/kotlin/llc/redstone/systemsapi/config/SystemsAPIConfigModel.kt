package llc.redstone.systemsapi.config

import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Modmenu
import io.wispforest.owo.config.annotation.SectionHeader
import llc.redstone.systemsapi.SystemsAPI.MOD_ID


@Modmenu(modId = MOD_ID)
@Config(name = MOD_ID, wrapperName = "SystemsAPIConfig")
class SystemsAPIConfigModel {
    @JvmField
    @SectionHeader("menus")
    var baseClickDelay: Long = 50L

    @JvmField
    @SectionHeader("timeouts")
    var menuTimeout: Long = 1000L
    @JvmField
    var menuItemLoadedTimeout: Long = 1000L
    @JvmField
    var menuItemTimeout: Long = 1000L
    @JvmField
    var previousInputTimeout: Long = 1000L
}