package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.api.Event
import llc.redstone.systemsapi.data.Action
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.MenuUtils

internal class EventImporter : Event {
    private fun openEventActionsMenu(event: Event.Events) {
        CommandUtils.runCommand("eventactions")
        MenuUtils.clickMenuSlot(event.item)
    }

    override suspend fun getActionContainerForEvent(event: Event.Events): ActionContainer {
        openEventActionsMenu(event)
        return ActionContainer("Edit Actions")
    }
}