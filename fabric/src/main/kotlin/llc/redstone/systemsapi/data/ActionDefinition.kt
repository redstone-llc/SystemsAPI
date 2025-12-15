package llc.redstone.systemsapi.data

import llc.redstone.systemsapi.api.Event

@Target(AnnotationTarget.CLASS)
annotation class ActionDefinition(
    val displayName: String,
    val limit: Int,
    val allowedScopes: Array<Scope>,
    val prohibitedScopes: Array<Scope> = [],
)

@Repeatable
annotation class Scope(
    val scope: ScopeType,
    val limit: Int = -1,
    val allowedEvents: Array<Event.Events> = [],
    val prohibitedEvents: Array<Event.Events> = []
)

@Repeatable
annotation class EventScope()

enum class ScopeType {
    ALL,
    FUNCTION,
    MENU,
    EVENT,
    ITEM,
    CONDITIONAL,
    RANDOM
}