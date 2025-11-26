package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.api.Command
import llc.redstone.systemsapi.api.Function
import llc.redstone.systemsapi.api.HousingImporter

internal object HousingImporter: HousingImporter {

    override fun getFunctionOrNull(name: String): Function? {
        return FunctionImporter(name)
    }

    override fun getAllFunctions(): List<Function> {
        TODO("Not yet implemented")
    }

    override fun getCommandOrNull(name: String): Command? {
        return CommandImporter(name)
    }
}