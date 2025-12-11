package llc.redstone.systemsapi.util

import llc.redstone.systemsapi.SystemsAPI.LOGGER
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

private val preferredIdentifiers = listOf("name", "title")

internal fun Any.fail(message: String) {

    val scope = {
        val base = (this.javaClass.simpleName ?: "Unknown")
            .removeSuffix("Importer")
            .removeSuffix("Container")

        val identifier = this.javaClass.kotlin.memberProperties
            .firstOrNull { prop ->
                prop.name in preferredIdentifiers &&
                        prop.returnType.classifier == String::class &&
                        prop.getter.parameters.size == 1
            }
            ?.also { it.isAccessible = true }
            ?.getter
            ?.call(this) as? String
            ?: base

        if (identifier.isBlank()) base else "$base $identifier"
    }
    error("[$scope] $message")
}

internal fun Any.warn(message: String) {
    val scope = {
        val base = (this.javaClass.simpleName ?: "Unknown")
            .removeSuffix("Importer")
            .removeSuffix("Container")

        val identifier = this.javaClass.kotlin.memberProperties
            .firstOrNull { prop ->
                prop.name in preferredIdentifiers &&
                        prop.returnType.classifier == String::class &&
                        prop.getter.parameters.size == 1
            }
            ?.also { it.isAccessible = true }
            ?.getter
            ?.call(this) as? String
            ?: base

        if (identifier.isBlank()) base else "$base $identifier"
    }
    LOGGER.warn("[$scope] $message")
}