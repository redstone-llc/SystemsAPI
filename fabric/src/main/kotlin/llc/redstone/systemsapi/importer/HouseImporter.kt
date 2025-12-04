package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.api.Command
import llc.redstone.systemsapi.api.Menu
import llc.redstone.systemsapi.api.Function
import llc.redstone.systemsapi.api.House

internal object HouseImporter: House {

    override fun getFunctionOrNull(name: String): Function? {
        return FunctionImporter(name)
    }

    override fun getAllFunctions(): List<Function> {
        TODO("Not yet implemented")
    }

    override fun getMenuOrNull(title: String): Menu? {
        return MenuImporter(title)
    }

    override fun getCommandOrNull(name: String): Command? {
        return CommandImporter(name)
    }
}