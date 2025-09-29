package com.ilustris.sagai.core.utils

import android.util.Log
import com.google.firebase.ai.type.Schema
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import java.lang.reflect.ParameterizedType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.toString

fun toFirebaseSchema(
    clazz: Class<*>,
    excludeFields: List<String> = emptyList(),
) = Schema.obj(
    properties = clazz.toSchemaMap(excludeFields),
)

fun Class<*>.toSchema(
    nullable: Boolean,
    excludeFields: List<String>,
): Schema {
    if (this.isEnum) {
        val enumConstants = this.enumConstants?.map { it.toString() } ?: emptyList()

        return Schema.enumeration(enumConstants, nullable = nullable)
    }

    return when (this) {
        String::class.java -> {
            Schema.string(
                nullable = nullable,
            )
        }

        Int::class.java, Integer::class.java -> {
            Schema.integer(
                nullable = nullable,
            )
        }

        Long::class.java -> {
            Schema.long(
                nullable = nullable,
            )
        }

        Boolean::class.java -> {
            Schema.boolean(
                nullable = nullable,
            )
        }

        Double::class.java -> {
            Schema.double(
                nullable = nullable,
            )
        }

        Float::class.java -> {
            Schema.float(
                nullable = nullable,
            )
        }

        List::class.java, Array::class.java -> {
            val itemType =
                this.genericInterfaces
                    .filterIsInstance<ParameterizedType>()
                    .firstOrNull()
                    ?.actualTypeArguments
                    ?.firstOrNull() as? Class<*>

            Schema.array(
                itemType?.toSchema(nullable = nullable, excludeFields = excludeFields)
                    ?: Schema.string(nullable = nullable),
            )
        }

        else -> {
            Schema.obj(
                properties = this.toSchemaMap(excludeFields),
                nullable = nullable,
            )
        }
    }
}

fun Class<*>.toSchemaMap(excludeFields: List<String> = emptyList()): Map<String, Schema> =
    declaredFields
        .filter {
            excludeFields.plus("\$stable").contains(it.name).not()
        }.associate {
            val memberIsNullable =
                this
                    .kotlin.members
                    .find { member -> member.name == it.name }
                    ?.returnType
                    ?.isMarkedNullable
            Log.d("SchemaMapper", "Mapping field ${it.name} nullable: $memberIsNullable type: ${it.type.name}")
            it.name to it.type.toSchema(memberIsNullable == true, excludeFields)
        }

fun String.removePackagePrefix(): String =
    this
        .substringAfterLast(".")
        .replace(".", "")

fun Pair<String, String>.formatToString(showSender: Boolean = true) =
    buildString {
        if (showSender) {
            append(first)
            append(": ")
        }
        append(second)
    }

fun toJsonMap(
    clazz: Class<*>,
    filteredFields: List<String> = emptyList(),
    fieldCustomDescriptions: List<Pair<String, String>> = emptyList(),
): String {
    val deniedFields =
        filteredFields
            .plus("\$stable")
            .plus("companion")
    val fields =
        clazz
            .declaredFields
            .filter {
                deniedFields.contains(it.name).not()
            }.joinToString(separator = ",\n") { field ->
                val fieldName = field.name
                val fieldType = field.type
                val fieldValue =
                    when {
                        fieldType.isEnum -> "${fieldType.enumConstants?.joinToString(" | ") { it.toString() }}"
                        fieldType == String::class.java -> "\"\""
                        fieldType == Int::class.java || fieldType == Integer::class.java -> "0"
                        fieldType == Boolean::class.java -> "false"
                        fieldType == Double::class.java -> "0.0"
                        fieldType == Float::class.java -> "0.0f"
                        fieldType == Long::class.java -> "0L"
                        List::class.java.isAssignableFrom(fieldType) ||
                            Array::class.java.isAssignableFrom(
                                fieldType,
                            )
                        -> "[]"

                        else -> toJsonMap(fieldType)
                    }
                val customDescription =
                    fieldCustomDescriptions.find { it.first == field.name }
                if (customDescription != null) {
                    "\"${customDescription.first}\": \"${customDescription.second}\""
                } else {
                    "\"$fieldName\": $fieldValue"
                }
            }
    return "{\n$fields\n}"
}

fun Any?.toJsonFormat(): String {
    if (this == null) return emptyString()
    return GsonBuilder()
        .setPrettyPrinting()
        .create()
        .toJson(this)
}

fun Any?.toJsonFormatIncludingFields(fieldsToInclude: List<String>): String {
    if (this == null) return emptyString()

    val inclusionStrategy =
        object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean = !fieldsToInclude.contains(f.name)

            override fun shouldSkipClass(clazz: Class<*>): Boolean = false
        }

    val gson =
        GsonBuilder()
            .addSerializationExclusionStrategy(inclusionStrategy)
            .setPrettyPrinting()
            .create()
    return gson.toJson(this)
}

fun Any?.toJsonFormatExcludingFields(fieldsToExclude: List<String>): String {
    if (this == null) return emptyString()

    val exclusionStrategy =
        object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean = fieldsToExclude.contains(f.name)

            override fun shouldSkipClass(clazz: Class<*>): Boolean = false
        }

    val gson =
        GsonBuilder()
            .addSerializationExclusionStrategy(exclusionStrategy)
            .setPrettyPrinting()
            .create()

    return gson.toJson(this)
}

fun doNothing() = {}

enum class DateFormatOption(
    val pattern: String,
) {
    SIMPLE_DD_MM_YYYY("dd/MM/yyyy"),
    DAY_OF_WEEK_DD_MM_YYYY("EEE, dd/MM/yyyy"),
    FULL_DAY_MONTH_YEAR("dd 'of' MMMM yyyy"),
    HOUR_MINUTE_DAY_OF_MONTH_YEAR("HH:mm 'of' dd 'of' MMMM"),
    ISO_DATE("yyyy-MM-dd"),
    MONTH_DAY_YEAR("MM/dd/yyyy"),
}

fun Long.formatDate(
    option: DateFormatOption = DateFormatOption.SIMPLE_DD_MM_YYYY,
    locale: Locale = Locale.getDefault(),
): String {
    val date = Date(this)
    val format = SimpleDateFormat(option.pattern, locale)
    return format.format(date)
}

fun Long.formatHours(): String {
    val date = Date(this)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

fun String?.sanitizeAndExtractJsonString(): String {
    val logTag = "StringSanitization"
    if (this.isNullOrBlank()) {
        Log.w(logTag, "Input string is null or blank, cannot sanitize.")
        throw IllegalArgumentException("Input string is null or blank.")
    }

    var cleanedJsonString = this
    Log.i(logTag, "Sanitizing raw string: $cleanedJsonString")

    // 1. Remove common markdown code block delimiters
    cleanedJsonString = cleanedJsonString.replace("```json", "").replace("```", "")

    // 2. Trim leading/trailing whitespace
    cleanedJsonString = cleanedJsonString.trim()

    // 3. If not starting with a JSON char, find the start (basic heuristic)
    val firstJsonChar = cleanedJsonString.indexOfFirst { it == '{' || it == '[' }
    if (firstJsonChar > 0) {
        cleanedJsonString = cleanedJsonString.substring(firstJsonChar)
    } else if (firstJsonChar == -1 && cleanedJsonString.isNotEmpty()) {
        Log.e(logTag, "No JSON start character '{' or '[' found in response: $cleanedJsonString")
        throw IllegalArgumentException("Response does not appear to contain JSON after initial cleaning.")
    }

    // 4. Ensure we only take content up to the corresponding last bracket (basic heuristic)
    if (cleanedJsonString.startsWith("[")) {
        val lastBracket = cleanedJsonString.lastIndexOf(']')
        if (lastBracket != -1) {
            cleanedJsonString = cleanedJsonString.substring(0, lastBracket + 1)
        } else if (cleanedJsonString.isNotEmpty()) {
            Log.e(logTag, "JSON array starts with '[' but no closing ']' found: $cleanedJsonString")
            throw IllegalArgumentException("Malformed JSON array: No closing bracket.")
        }
    } else if (cleanedJsonString.startsWith("{")) {
        val lastBracket = cleanedJsonString.lastIndexOf('}')
        if (lastBracket != -1) {
            cleanedJsonString = cleanedJsonString.substring(0, lastBracket + 1)
        } else if (cleanedJsonString.isNotEmpty()) {
            Log.e(
                logTag,
                "JSON object starts with '{' but no closing '}' found: $cleanedJsonString",
            )
            throw IllegalArgumentException("Malformed JSON object: No closing bracket.")
        }
    }

    // 5. Remove any remaining problematic backticks (final cleanup)
    cleanedJsonString = cleanedJsonString.replace("`", "")

    Log.i(logTag, "Sanitization complete, cleaned JSON: $cleanedJsonString")
    if (cleanedJsonString.isBlank()) {
        Log.e(logTag, "Cleaned JSON string is blank after sanitization.")
        throw IllegalArgumentException("Resulting JSON string is blank after sanitization.")
    }
    return cleanedJsonString
}

fun Int.toRoman(): String {
    val values = listOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    val symbols = listOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    var num = this
    val result = StringBuilder()

    for (i in values.indices) {
        while (num >= values[i]) {
            num -= values[i]
            result.append(symbols[i])
        }
    }
    return result.toString()
}
