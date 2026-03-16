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
            excludeFields.plus($$"$stable").contains(it.name).not()
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
    when {
        clazz.isEnum -> return "${clazz.enumConstants?.joinToString(" | ") { "\"$it\"" }}"
        clazz == String::class.java -> return "\"string\""
        clazz == Int::class.java || clazz == Integer::class.java -> return "0"
        clazz == Boolean::class.java -> return "false | true"
        clazz == Double::class.java -> return "0.0"
        clazz == Float::class.java -> return "0.0"
        clazz == Long::class.java -> return "0"
        clazz == Byte::class.java -> return "0"
        clazz == Short::class.java -> return "0"
        clazz == Char::class.java -> return "\"string\""
    }

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
                        fieldType.isEnum -> {
                            "${fieldType.enumConstants?.joinToString(" | ") { "\"$it\"" }}"
                        }

                        fieldType == String::class.java -> {
                            "\"string\""
                        }

                        fieldType == Int::class.java || fieldType == Integer::class.java -> {
                            "0"
                        }

                        fieldType == Boolean::class.java -> {
                            "false"
                        }

                        fieldType == Double::class.java -> {
                            "0.0"
                        }

                        fieldType == Float::class.java -> {
                            "0.0"
                        }

                        fieldType == Long::class.java -> {
                            "0"
                        }

                        List::class.java.isAssignableFrom(fieldType) ||
                            Array::class.java.isAssignableFrom(
                                fieldType,
                            )
                        -> {
                            // Extract generic type parameter from List/Array
                            val genericType = field.genericType as? ParameterizedType
                            val itemType =
                                genericType?.actualTypeArguments?.firstOrNull() as? Class<*>

                            if (itemType != null) {
                                // Recursively get the structure - works for both primitives and complex objects
                                val itemJson =
                                    toJsonMap(itemType, filteredFields, fieldCustomDescriptions)
                                "[ $itemJson ]"
                            } else {
                                // Unknown type - just show empty array
                                "[]"
                            }
                        }

                        else -> {
                            toJsonMap(fieldType, filteredFields, fieldCustomDescriptions)
                        }
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

fun Any.toPromptVariables(): Map<String, String> {
    val gson = GsonBuilder().create()
    val json = gson.toJson(this)
    val mapType = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
    val rawMap: Map<String, Any> = gson.fromJson(json, mapType)

    return rawMap.mapValues { (_, value) ->
        if (value is Double && value % 1 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }
}

fun doNothing() {
}

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

fun String?.sanitizeAndExtractJsonString(expectedClass: Class<*>? = null): String {
    val logTag = "StringSanitization"
    if (this.isNullOrBlank()) {
        Log.w(logTag, "Input string is null or blank, cannot sanitize.")
        throw IllegalArgumentException("Input string is null or blank.")
    }

    var cleanedJsonString = this!!
    Log.i(logTag, "Sanitizing raw string: $cleanedJsonString")

    // 1. Remove common markdown code block delimiters
    cleanedJsonString = cleanedJsonString.replace("```json", "").replace("```", "")

    // 2. Trim leading/trailing whitespace
    cleanedJsonString = cleanedJsonString.trim()

    // 3. If not starting with a JSON char, find the start (basic heuristic)
    val firstJsonCharIndex = cleanedJsonString.indexOfFirst { it == '{' || it == '[' }
    if (firstJsonCharIndex > 0) {
        cleanedJsonString = cleanedJsonString.substring(firstJsonCharIndex)
    } else if (firstJsonCharIndex == -1 && cleanedJsonString.isNotEmpty()) {
        Log.e(logTag, "No JSON start character '{' or '[' found in response: $cleanedJsonString")
        throw IllegalArgumentException("Response does not appear to contain JSON after initial cleaning.")
    }

    // Helper: find the matching closing bracket index for the JSON starting char
    fun findMatchingClosingIndex(
        s: String,
        start: Int,
    ): Int {
        if (start >= s.length) return -1
        val open = s[start]
        val close =
            when (open) {
                '{' -> '}'
                '[' -> ']'
                else -> return -1
            }

        var depth = 1
        var inString = false
        var escaped = false

        var i = start + 1
        while (i < s.length) {
            val c = s[i]
            if (escaped) {
                // previous char was a backslash, this char is escaped; skip special handling
                escaped = false
            } else {
                when (c) {
                    '\\' -> {
                        escaped = true
                    }

                    '"' -> {
                        inString = !inString
                    }

                    else -> {
                        if (!inString) {
                            if (c == open) {
                                depth++
                            } else if (c == close) {
                                depth--
                                if (depth == 0) return i
                            }
                        }
                    }
                }
            }
            i++
        }
        return -1
    }

    // 4. Compute precise end index using bracket matching (handles nested structures & strings)
    if (cleanedJsonString.isNotEmpty()) {
        val startChar = cleanedJsonString.first()
        if (startChar == '{' || startChar == '[') {
            val endIndex = findMatchingClosingIndex(cleanedJsonString, 0)
            if (endIndex != -1) {
                cleanedJsonString = cleanedJsonString.substring(0, endIndex + 1)
            } else {
                // Fallback: Check if the string ends with the expected closing bracket
                // This handles cases where internal malformation (like missing quotes) breaks the strict parser
                val expectedClose = if (startChar == '{') '}' else ']'
                if (cleanedJsonString.trim().lastOrNull() == expectedClose) {
                    Log.w(
                        logTag,
                        "Strict JSON parsing failed (likely due to malformed content), but found closing bracket '$expectedClose'. Proceeding with full string.",
                    )
                } else {
                    Log.e(
                        logTag,
                        "Could not find matching closing bracket for JSON starting at: $cleanedJsonString",
                    )
                    throw IllegalArgumentException("Malformed JSON: No matching closing bracket found.")
                }
            }
        }
    }

    // 5. Repair JSON structure (fix missing quotes) BEFORE processing commas
    //    This ensures that the comma removal logic (which depends on string state) works correctly
    if (expectedClass != null && cleanedJsonString.isNotBlank()) {
        cleanedJsonString = repairJsonStructure(cleanedJsonString, expectedClass)
    }

    // 6. Remove trailing commas before closing braces/brackets (common issue)
    //    Example: { "a": 1, "b": 2, }  -> { "a": 1, "b": 2 }
    // Use a scanner to ensure commas inside strings are not removed.
    run {
        val sb = StringBuilder()
        var i = 0
        var inString = false
        var escaped = false
        while (i < cleanedJsonString.length) {
            val c = cleanedJsonString[i]
            if (escaped) {
                sb.append(c)
                escaped = false
                i++
                continue
            }
            if (c == '\\') {
                sb.append(c)
                escaped = true
                i++
                continue
            }
            if (c == '"') {
                sb.append(c)
                inString = !inString
                i++
                continue
            }

            if (c == ',' && !inString) {
                // look ahead for whitespace and closing bracket
                var j = i + 1
                while (j < cleanedJsonString.length && cleanedJsonString[j].isWhitespace()) j++
                if (j < cleanedJsonString.length && (cleanedJsonString[j] == '}' || cleanedJsonString[j] == ']')) {
                    // skip this comma
                    i++
                    continue
                }
            }

            sb.append(c)
            i++
        }
        cleanedJsonString = sb.toString()
    }

    // 7. Remove any remaining problematic backticks (final cleanup)
    cleanedJsonString = cleanedJsonString.replace("`", "")

    // 8. Remove duplicate quotes that may have been created during repair
    //    But preserve empty string values (e.g., "field": "")
    //    Only remove when we have 3+ consecutive quotes or malformed patterns
    cleanedJsonString =
        cleanedJsonString.replace("\"\"\"", "\"\"") // Convert triple quotes to double
    // Note: We intentionally do NOT replace "" -> " because "" is a valid empty string in JSON

    Log.i(logTag, "Sanitization complete, cleaned JSON: $cleanedJsonString")
    if (cleanedJsonString.isBlank()) {
        Log.e(logTag, "Cleaned JSON string is blank after sanitization.")
        throw IllegalArgumentException("Resulting JSON string is blank after sanitization.")
    }
    return cleanedJsonString
}

private fun repairJsonStructure(
    json: String,
    clazz: Class<*>,
): String {
    var currentJson = json
    try {
        val fields = getRecursiveFields(clazz)
        fields.forEach { (name, type) ->
            if (type == String::class.java) {
                val pattern = Regex("\"$name\"\\s*:\\s*")
                var startIndex = 0
                while (true) {
                    val match = pattern.find(currentJson, startIndex) ?: break
                    val valueStartRaw = match.range.last + 1

                    var charIndex = valueStartRaw
                    while (charIndex < currentJson.length && currentJson[charIndex].isWhitespace()) {
                        charIndex++
                    }

                    if (charIndex < currentJson.length) {
                        val firstChar = currentJson[charIndex]

                        // Check if this is a JSON literal (null, true, false) or number - don't quote these
                        val remainingText = currentJson.substring(charIndex)
                        val isJsonLiteral =
                            remainingText.startsWith("null") ||
                                remainingText.startsWith("true") ||
                                remainingText.startsWith("false") ||
                                firstChar.isDigit() ||
                                firstChar == '-'

                        if (isJsonLiteral) {
                            // Skip this field - it's a valid JSON literal, not a string needing quotes
                            // Find the end of the literal (comma, newline, or closing brace)
                            var literalEnd = charIndex
                            while (literalEnd < currentJson.length) {
                                val c = currentJson[literalEnd]
                                if (c == ',' || c == '}' || c == ']' || c == '\n' || c == '\r') {
                                    break
                                }
                                literalEnd++
                            }
                            startIndex = literalEnd + 1
                            continue // Continue to find next occurrence of this field
                        }

                        val needsOpeningQuote = firstChar != '"'

                        if (needsOpeningQuote) {
                            Log.w(
                                "JsonRepair",
                                "Repairing JSON: Found missing opening quote for field '$name' at index $charIndex",
                            )
                            currentJson =
                                currentJson.substring(0, charIndex) + "\"" +
                                currentJson.substring(charIndex)
                            charIndex++ // Account for the inserted quote
                        }

                        if (!needsOpeningQuote) {
                            // Original had opening quote - scan for proper closing quote
                            var valueEndIndex = charIndex + 1
                            var escaped = false
                            while (valueEndIndex < currentJson.length) {
                                val c = currentJson[valueEndIndex]

                                if (escaped) {
                                    escaped = false
                                    valueEndIndex++
                                    continue
                                }

                                when (c) {
                                    '\\' -> {
                                        escaped = true
                                    }

                                    '"' -> {
                                        // Found closing quote
                                        break
                                    }
                                }
                                valueEndIndex++
                            }
                            // Advance past this field
                            startIndex = valueEndIndex + 1
                        } else {
                            // We added opening quote - need to find where to put closing quote
                            // For an unquoted string, we need to find the STRUCTURAL end:
                            // - A newline followed by another field name pattern ("fieldName":)
                            // - A closing brace } at the end of the object
                            // - A closing bracket ] at the end of an array

                            val endPosition = findUnquotedStringEnd(currentJson, charIndex + 1)

                            if (endPosition > charIndex + 1) {
                                // The endPosition might include trailing whitespace and possibly an existing quote
                                // We need to find the actual last content character and check if there's a quote after it

                                var lastContentPos = endPosition - 1
                                var trailingQuotePos = -1

                                // Scan backwards to find: last content char, and any trailing quote
                                // Pattern we're looking for: <content><optional_whitespace><optional_quote><optional_whitespace>
                                var scanPos = endPosition - 1
                                var foundContent = false
                                var foundQuoteAfterContent = false

                                while (scanPos >= charIndex) {
                                    val ch = currentJson[scanPos]

                                    if (!foundContent) {
                                        // Still looking for the last real content
                                        if (ch == '"') {
                                            // Found a quote before any content - this is likely the trailing quote
                                            trailingQuotePos = scanPos
                                            scanPos--
                                            continue
                                        } else if (ch.isWhitespace()) {
                                            // Skip whitespace
                                            scanPos--
                                            continue
                                        } else {
                                            // Found actual content character
                                            foundContent = true
                                            lastContentPos =
                                                scanPos + 1 // Position AFTER the last content char
                                            if (trailingQuotePos != -1) {
                                                foundQuoteAfterContent = true
                                            }
                                            break
                                        }
                                    }
                                }

                                if (foundQuoteAfterContent) {
                                    // There's an existing trailing quote - remove everything from lastContentPos onwards
                                    // and add our own quote
                                    Log.d(
                                        "JsonRepair",
                                        "Field '$name' has existing closing quote at index $trailingQuotePos after content at $lastContentPos, cleaning up",
                                    )
                                    currentJson = currentJson.substring(0, lastContentPos) +
                                        "\"" +
                                        currentJson.substring(endPosition)
                                    startIndex = lastContentPos + 2
                                } else {
                                    // No existing quote found, just add the closing quote at the right position
                                    Log.w(
                                        "JsonRepair",
                                        "Repairing JSON: Adding missing closing quote for field '$name' at index $lastContentPos",
                                    )
                                    currentJson =
                                        currentJson.substring(0, lastContentPos) + "\"" +
                                        currentJson.substring(lastContentPos)
                                    startIndex = lastContentPos + 2
                                }
                            } else {
                                startIndex = endPosition + 1
                            }
                        }
                    } else {
                        break
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("JsonRepair", "Failed to repair JSON structure: ${e.message}")
    }
    return currentJson
}

/**
 * Find the end position of an unquoted string value.
 *
 * This looks for structural JSON delimiters that indicate the end of the value:
 * - A newline followed by "fieldName": pattern (next field)
 * - A closing brace } (end of object)
 * - End of string
 *
 * We can't rely on commas because the text content itself may contain commas.
 */
private fun findUnquotedStringEnd(
    json: String,
    startIndex: Int,
): Int {
    // Strategy: Look for patterns that indicate we've left the string value
    // 1. Look for `\n  "fieldName":` pattern (next field in JSON)
    // 2. Look for `\n}` pattern (end of object)
    // 3. Look for the last non-whitespace character before `}` or end

    var i = startIndex
    var lastContentIndex = startIndex

    while (i < json.length) {
        val c = json[i]

        // Track the last non-whitespace position
        if (!c.isWhitespace()) {
            lastContentIndex = i + 1
        }

        // Check if we hit a newline - potential field boundary
        if (c == '\n' || c == '\r') {
            // Look ahead for next field pattern or closing brace
            var lookAhead = i + 1
            while (lookAhead < json.length && json[lookAhead].isWhitespace() && json[lookAhead] != '\n') {
                lookAhead++
            }

            if (lookAhead < json.length) {
                // Check for closing brace (end of object)
                if (json[lookAhead] == '}') {
                    // Find the last non-whitespace before the newline
                    var endPos = i
                    while (endPos > startIndex && json[endPos - 1].isWhitespace()) {
                        endPos--
                    }
                    return endPos
                }

                // Check for next field pattern: "fieldName":
                if (json[lookAhead] == '"') {
                    // This looks like the start of a new field
                    val nextFieldPattern = Regex("\"([^\"]+)\"\\s*:")
                    val potentialMatch = nextFieldPattern.find(json, lookAhead)
                    if (potentialMatch != null && potentialMatch.range.first == lookAhead) {
                        // Confirmed: this is a new field, so our string ends before the newline
                        var endPos = i
                        while (endPos > startIndex && json[endPos - 1].isWhitespace()) {
                            endPos--
                        }
                        return endPos
                    }
                }
            }
        }

        // Check for } that might be at same line (compact JSON)
        if (c == '}' || c == ']') {
            // This could be end of object - but we need to be careful
            // Check if there's a comma before this (indicating we're at field boundary)
            var checkBack = i - 1
            while (checkBack > startIndex && json[checkBack].isWhitespace()) {
                checkBack--
            }
            // Return position just before any trailing whitespace and the brace
            return checkBack + 1
        }

        i++
    }

    // If we reach here, return the last content position
    return lastContentIndex
}

private fun getRecursiveFields(
    clazz: Class<*>,
    visited: MutableSet<Class<*>> = mutableSetOf(),
): Map<String, Class<*>> {
    if (clazz in visited || clazz.isPrimitive || clazz == String::class.java || clazz.isEnum) return emptyMap()
    visited.add(clazz)
    val fields = mutableMapOf<String, Class<*>>()
    clazz.declaredFields.forEach { field ->
        fields[field.name] = field.type
        if (!field.type.isPrimitive && field.type != String::class.java && !field.type.isEnum) {
            fields.putAll(getRecursiveFields(field.type, visited))
        }
    }
    return fields
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

fun Long.formatFileSize(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> String.format(Locale.getDefault(), "%.2f GB", gb)
        mb >= 1 -> String.format(Locale.getDefault(), "%.2f MB", mb)
        kb >= 1 -> String.format(Locale.getDefault(), "%.2f KB", kb)
        else -> String.format(Locale.getDefault(), "%d B", this)
    }
}

fun Any?.toAINormalize(fieldsToExclude: List<String> = emptyList()): String {
    if (this == null) return ""
    if (this is String || this is Number || this is Boolean || this is Enum<*>) {
        return this.toString()
    }
    val fields = this::class.java.declaredFields
    val standardExclusions = listOf("\$stable", "companion")
    val allExclusions = fieldsToExclude + standardExclusions

    return fields
        .mapNotNull { field ->
            if (field.name in allExclusions) return@mapNotNull null
            field.isAccessible = true
            val value = field.get(this) ?: return@mapNotNull null

            val valueString =
                when (value) {
                    is List<*> -> {
                        if (value.isEmpty()) {
                            ""
                        } else {
                            value.normalizetoAIItems(
                                fieldsToExclude,
                            )
                        }
                    }

                    is Array<*> -> {
                        if (value.isEmpty()) {
                            ""
                        } else {
                            value.normalizetoAIItems(
                                fieldsToExclude,
                            )
                        }
                    }

                    is String -> {
                        if (value.length > 500) {
                            "${value.take(497)}..."
                        } else {
                            value
                        }
                    }

                    is Enum<*> -> {
                        value.toString()
                    }

                    else -> {
                        if (value::class.isData) {
                            val normalized = value.toAINormalize(fieldsToExclude)
                            if (normalized.isNotBlank()) {
                                "\n${normalized.prependIndent("  ")}"
                            } else {
                                ""
                            }
                        } else {
                            value.toString()
                        }
                    }
                }

            if (valueString.isBlank() || valueString == "[]") {
                null
            } else {
                val itemsSize =
                    when (value) {
                        is List<*> -> "[${value.size}]"
                        is Array<*> -> "[${value.size}]"
                        else -> emptyString()
                    }
                "${field.name}$itemsSize: $valueString"
            }
        }.joinToString("\n")
}
