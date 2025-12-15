package llc.redstone.systemsapi.util

import llc.redstone.systemsapi.api.Event
import llc.redstone.systemsapi.data.EventScope

object ScopeUtils {

    fun isAllowedForEvent(actionClass: Class<*>, event: Event.Events): Boolean {
        val ann = actionClass.getAnnotation(EventScope::class.java) ?: return true
        if (ann.only.isNotEmpty()) return ann.only.contains(event)
        if (ann.except.isNotEmpty()) return !ann.except.contains(event)
        return true
    }

}