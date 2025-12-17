package llc.redstone.systemsapi.data

import llc.redstone.systemsapi.api.Event

@Target(AnnotationTarget.CLASS)
annotation class ActionDefinition(
    val displayName: String,
    val defaultLimit: Int,
    val scopes: Array<Scope> = []
)

@Repeatable
annotation class Scope(
    val scope: ScopeType,
    val limit: Int = -1,
    val events: Array<Event.Events> = []
)

enum class ScopeType {
    FUNCTION,
    REGION,
    COMMAND,
    MENU,
    ITEM,
    CONDITIONAL,
    EVENT,
    RANDOM
}