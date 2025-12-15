package llc.redstone.systemsapi.data

import llc.redstone.systemsapi.api.Event

annotation class AllScope()
annotation class FunctionScope()
annotation class ItemScope()
annotation class EventScope(
    vararg val events: Event.Events
)
annotation class ConditionalScope()
annotation class MenuScope()
