package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.api.HouseSettings
import llc.redstone.systemsapi.util.CommandUtils

object HouseSettingsImporter : HouseSettings {
    override suspend fun getHouseName(): String {
        TODO("Not yet implemented")
    }

    override suspend fun setHouseName(newName: String) {
        if (newName.length !in 3..32) throw IllegalArgumentException("Name length must be in range 3..32")
        CommandUtils.runCommand("house name $newName")
    }

    override suspend fun getHouseTags(): Set<HouseSettings.HouseTag> {
        TODO("Not yet implemented")
    }

    override suspend fun setHouseTags(newTags: Set<HouseSettings.HouseTag>) {
        TODO("Not yet implemented")
    }

    override suspend fun getHouseLanguages(): Set<HouseSettings.HouseLanguage> {
        TODO("Not yet implemented")
    }

    override suspend fun setHouseLanguages(newLanguages: Set<HouseSettings.HouseLanguage>) {
        TODO("Not yet implemented")
    }

    override suspend fun getParkourAnnounce(): HouseSettings.ParkourAnnounce {
        TODO("Not yet implemented")
    }

    override suspend fun setParkourAnnounce(newPark: HouseSettings.ParkourAnnounce) {
        TODO("Not yet implemented")
    }

    override suspend fun getMaxPlayers(): HouseSettings.MaxPlayers {
        TODO("Not yet implemented")
    }

    override suspend fun setMaxPlayers(newMaxPlayers: HouseSettings.MaxPlayers) {
        TODO("Not yet implemented")
    }

    override suspend fun getDaylightCycle(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun setDaylightCycle(newDaylightCycle: Boolean) {
        TODO("Not yet implemented")
    }
}