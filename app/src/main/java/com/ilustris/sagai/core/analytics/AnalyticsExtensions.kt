package com.ilustris.sagai.core.analytics

import android.os.Bundle
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

private const val TAG = "AnalyticsExtensions"
private const val BUNDLE_SIZE_WARNING_THRESHOLD = 50 // Warn if bundle has more than 50 properties

/**
 * Converts any data class to a Firebase Analytics Bundle.
 * Uses reflection to automatically extract properties and convert camelCase to snake_case.
 *
 * @return Bundle with all non-null properties from the data class
 */
fun Any.toAnalyticsBundle(): Bundle {
    val bundle = Bundle()
    var propertyCount = 0

    try {
        val kClass = this::class

        // Validate that this is a data class
        if (!kClass.isData) {
            val warning = "Analytics event class ${kClass.simpleName} is not a data class"
            Log.w(TAG, warning)
            FirebaseCrashlytics.getInstance().log(warning)
        }

        kClass.memberProperties.forEach { property ->
            try {
                @Suppress("UNCHECKED_CAST")
                val prop = property as KProperty1<Any, Any?>
                val value = prop.get(this)

                // Skip null properties
                if (value == null) {
                    return@forEach
                }

                val propertyName = property.name.toSnakeCase()

                when (value) {
                    is String -> {
                        bundle.putString(propertyName, value)
                    }

                    is Int -> {
                        bundle.putInt(propertyName, value)
                    }

                    is Long -> {
                        bundle.putLong(propertyName, value)
                    }

                    is Float -> {
                        bundle.putFloat(propertyName, value)
                    }

                    is Double -> {
                        bundle.putDouble(propertyName, value)
                    }

                    is Boolean -> {
                        bundle.putBoolean(propertyName, value)
                    }

                    else -> {
                        val warning =
                            "Unsupported property type for ${property.name}: ${value::class.simpleName}"
                        Log.w(TAG, warning)
                        FirebaseCrashlytics.getInstance().log(warning)
                        return@forEach
                    }
                }

                propertyCount++
            } catch (e: Exception) {
                val error =
                    AnalyticsPropertyException(
                        "Failed to map property ${property.name}",
                        e,
                    )
                Log.w(TAG, error.message, error)
                FirebaseCrashlytics.getInstance().recordException(error)
            }
        }

        // Warn if bundle is too large
        if (propertyCount > BUNDLE_SIZE_WARNING_THRESHOLD) {
            Log.w(TAG, "Bundle size is large: $propertyCount properties for ${kClass.simpleName}")
        }
    } catch (e: Exception) {
        val error =
            AnalyticsBundleException(
                "Failed to convert ${this::class.simpleName} to analytics bundle",
                e,
            )
        Log.e(TAG, error.message, error)
        FirebaseCrashlytics.getInstance().recordException(error)
    }

    return bundle
}

/**
 * Converts camelCase to snake_case.
 * Handles acronyms correctly (e.g., HTTPRequest -> http_request).
 *
 * Rules:
 * - Split on uppercase characters
 * - Insert underscore before uppercase (except first character)
 * - For acronyms, insert _ before the last uppercase in sequence
 * - Convert all to lowercase
 */
fun String.toSnakeCase(): String {
    if (isEmpty()) return this

    val result = StringBuilder()
    var previousWasUpperCase = false

    forEachIndexed { index, char ->
        when {
            // First character - just add it lowercase
            index == 0 -> {
                result.append(char.lowercaseChar())
                previousWasUpperCase = char.isUpperCase()
            }

            // Current char is uppercase
            char.isUpperCase() -> {
                // Check if next char exists and is lowercase (end of acronym)
                val nextCharIsLower = index < length - 1 && this[index + 1].isLowerCase()

                // Add underscore if:
                // - Previous was not uppercase (start of new word), OR
                // - Previous was uppercase AND next is lowercase (end of acronym)
                if (!previousWasUpperCase || (previousWasUpperCase && nextCharIsLower)) {
                    result.append('_')
                }

                result.append(char.lowercaseChar())
                previousWasUpperCase = true
            }

            // Current char is lowercase
            else -> {
                result.append(char)
                previousWasUpperCase = false
            }
        }
    }

    return result.toString()
}

/**
 * Converts a class name to a human-readable event name.
 * Example: SagaCreationEvent -> Saga Creation Event
 */
fun String.toEventName(): String {
    // Remove "Event" suffix if present
    val nameWithoutSuffix =
        if (endsWith("Event")) {
            substring(0, length - 5)
        } else {
            this
        }

    // Convert from PascalCase to space-separated words
    val result = StringBuilder()
    nameWithoutSuffix.forEachIndexed { index, char ->
        if (index > 0 && char.isUpperCase()) {
            result.append(' ')
        }
        result.append(char)
    }

    return result.toString()
}
