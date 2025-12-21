package llc.redstone.systemsapi.importer

import llc.redstone.systemsapi.api.Event
import llc.redstone.systemsapi.util.CommandUtils
import llc.redstone.systemsapi.util.MenuUtils

internal object EventImporter : Event {
    private suspend fun openEventActionsMenu(event: Event.Events) {
        CommandUtils.runCommand("eventactions")
        MenuUtils.onOpen("Event Actions")
        MenuUtils.clickItems(event.label, event.item, paginated = true)
    }

    override suspend fun getActionContainerForEvent(event: Event.Events): ActionContainer {
        openEventActionsMenu(event)
        return ActionContainer("Edit Actions")
    }
}