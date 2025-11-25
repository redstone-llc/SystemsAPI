package dev.wekend.housingtoolbox.feature.importer

import llc.redstone.systemapi.api.Event
import llc.redstone.systemapi.data.Action
import llc.redstone.systemapi.importer.ActionInteraction
import llc.redstone.systemapi.util.CommandUtils
import llc.redstone.systemapi.util.MenuUtils

internal class EventImporter : Event {
    private fun openEventActionsMenu(event: Event.Events) {
        CommandUtils.runCommand("eventactions")
        MenuUtils.clickMenuSlot(event.item)
    }

    override suspend fun getActionsForEvent(event: Event.Events): List<Action> {
        TODO("Not yet implemented")
    }

    override suspend fun setActionsForEvent(event: Event.Events, newActions: List<Action>, optimized: Boolean) {
        openEventActionsMenu(event)
        ActionInteraction("Edit Actions").addActions(newActions)
    }
}