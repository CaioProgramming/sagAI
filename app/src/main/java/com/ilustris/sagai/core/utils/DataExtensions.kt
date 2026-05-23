package com.ilustris.sagai.core.utils

import com.google.firebase.ai.type.Schema
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import timber.log.Timber
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

fun Long.formatDuration(): String {
    val minutes = this / 60000
    val hours = minutes / 60
    return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m"
}

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

        Int::class.java, Integer::class.java, Integer::class.java -> {
            Schema.integer(
                nullable = nullable,
            )
        }

        Long::class.java, java.lang.Long::class.java -> {
            Schema.long(
                nullable = nullable,
            )
        }

        Boolean::class.java, java.lang.Boolean::class.java -> {
            Schema.boolean(
                nullable = nullable,
            )
        }

        Double::class.java, java.lang.Double::class.java -> {
            Schema.double(
                nullable = nullable,
            )
        }

        Float::class.java, java.lang.Float::class.java -> {
            Schema.float(
                nullable = nullable,
            )
        }

        Byte::class.java, java.lang.Byte::class.java, Short::class.java, java.lang.Short::class.java -> {
            Schema.integer(
                nullable = nullable,
            )
        }

        Char::class.java, Character::class.java -> {
            Schema.string(
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
            Timber.tag("SchemaMapper").d("Mapping field ${it.name} nullable: $memberIsNullable type: ${it.type.name}")
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
    val obj = toJsonMapObject(clazz, filteredFields, fieldCustomDescriptions, mutableSetOf())
    val gson = GsonBuilder().setPrettyPrinting().create()
    return gson.toJson(obj)
}

private fun toJsonMapObject(
    clazz: Class<*>,
    filteredFields: List<String>,
    fieldCustomDescriptions: List<Pair<String, String>>,
    visited: MutableSet<Class<*>>,
): Any {
    when {
        clazz.isEnum -> return clazz.enumConstants?.joinToString(" | ") ?: "PLACEHOLDER_ENUM"

        clazz == String::class.java -> return "PLACEHOLDER_STRING"

        clazz == Int::class.java || clazz == Integer::class.java || clazz == Integer::class.java || clazz == Long::class.java ||
            clazz == java.lang.Long::class.java -> return 999

        clazz == Boolean::class.java || clazz == java.lang.Boolean::class.java -> return false

        clazz == Double::class.java || clazz == java.lang.Double::class.java -> return 0.0

        clazz == Float::class.java || clazz == java.lang.Float::class.java -> return 0.0

        clazz == Byte::class.java || clazz == java.lang.Byte::class.java -> return 0

        clazz == Short::class.java || clazz == java.lang.Short::class.java -> return 0

        clazz == Char::class.java || clazz == Character::class.java -> return "PLACEHOLDER_CHAR"
    }

    if (visited.contains(clazz)) return "circular_reference_detected"
    visited.add(clazz)

    val map = linkedMapOf<String, Any>()
    val deniedFields = filteredFields + "\$stable" + "companion"

    clazz.declaredFields
        .filter { !deniedFields.contains(it.name) }
        .sortedBy { it.name }
        .forEach { field ->
            val customDesc = fieldCustomDescriptions.find { it.first == field.name }
            if (customDesc != null) {
                try {
                    map[field.name] =
                        com.google.gson.JsonParser
                            .parseString(customDesc.second)
                } catch (e: Exception) {
                    map[field.name] = customDesc.second
                }
            } else {
                val fieldType = field.type
                if (List::class.java.isAssignableFrom(fieldType) ||
                    Array::class.java.isAssignableFrom(
                        fieldType,
                    )
                ) {
                    val genericType = field.genericType as? ParameterizedType
                    val itemType = genericType?.actualTypeArguments?.firstOrNull() as? Class<*>
                    map[field.name] =
                        if (itemType != null) {
                            listOf(toJsonMapObject(itemType, filteredFields, emptyList(), visited))
                        } else {
                            emptyList<Any>()
                        }
                } else {
                    map[field.name] =
                        toJsonMapObject(fieldType, filteredFields, emptyList(), visited)
                }
            }
        }
    visited.remove(clazz)
    return map
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

private fun Class<*>.isBoxedPrimitive(): Boolean =
    this == Integer::class.java ||
        this == java.lang.Long::class.java ||
        this == java.lang.Double::class.java ||
        this == java.lang.Float::class.java ||
        this == java.lang.Boolean::class.java ||
        this == java.lang.Byte::class.java ||
        this == java.lang.Short::class.java ||
        this == Character::class.java

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

val jsonPattern = Regex("```json\\s*\\n?(.*?)\\n?\\s*```", RegexOption.DOT_MATCHES_ALL)

fun String?.sanitizeAndExtractJsonString(expectedClass: Class<*>? = null): String {
    val logTag = "StringSanitization"
    if (this.isNullOrBlank()) {
        Timber.tag(logTag).w("Input string is null or blank, cannot sanitize.")
        throw IllegalArgumentException("Input string is null or blank.")
    }

    var cleanedJsonString = this!!
    Timber.tag(logTag).i("Sanitizing raw string: $cleanedJsonString")

    // 1. Try to extract JSON from ```json ... ``` fenced blocks first (most reliable signal)
    val fencedJsonPattern = jsonPattern
    val fencedMatch = fencedJsonPattern.findAll(cleanedJsonString).lastOrNull()

    if (fencedMatch != null) {
        cleanedJsonString = fencedMatch.groupValues[1].trim()
        Timber.tag(logTag).i("Extracted JSON from fenced block")
        // No need for heuristic if we have a fenced block; just ensure it's closed and return
        return finalizeSanitization(cleanedJsonString, expectedClass, logTag)
    }

    // Fallback: Remove any stray markdown delimiters and proceed with heuristic extraction
    cleanedJsonString =
        cleanedJsonString.replace(Regex("```[a-zA-Z]*"), "").replace("```", "").trim()

    // 2. Find all potential JSON start positions and find the last one that forms a complete block
    val allStarts =
        cleanedJsonString.indices.filter { cleanedJsonString[it] == '{' || cleanedJsonString[it] == '[' }
    var bestJson: String? = null

    for (startIndex in allStarts.reversed()) {
        val potentialSubstring = cleanedJsonString.substring(startIndex)
        val endIndex = findMatchingClosingIndex(potentialSubstring, 0)
        if (endIndex != -1) {
            val extracted = potentialSubstring.substring(0, endIndex + 1)
            val hasPlaceholder =
                extracted.contains(": \"PLACEHOLDER_") || extracted.contains(": 999")

            if (bestJson == null) {
                bestJson = extracted
            } else {
                val currentBestHasPlaceholder =
                    bestJson!!.contains(": \"PLACEHOLDER_") || bestJson!!.contains(": 999")

                if (currentBestHasPlaceholder && !hasPlaceholder) {
                    bestJson = extracted
                } else if (!currentBestHasPlaceholder && !hasPlaceholder) {
                    bestJson = extracted
                } else if (currentBestHasPlaceholder && hasPlaceholder) {
                    if (extracted.length > bestJson!!.length) {
                        bestJson = extracted
                    }
                }
            }
        }
    }

    if (bestJson != null) {
        cleanedJsonString = bestJson!!
    }

    return finalizeSanitization(cleanedJsonString, expectedClass, logTag)
}

private fun finalizeSanitization(
    json: String,
    expectedClass: Class<*>?,
    logTag: String,
): String {
    var cleanedJsonString = json

    // Ensure balanced braces for incomplete streams
    cleanedJsonString = autoCloseJson(cleanedJsonString)

    // 4. Precise end index is already handled by the new extraction logic above.
    // We just ensure we have a valid start.
    if (cleanedJsonString.isNotEmpty()) {
        val startChar = cleanedJsonString.first()
        if (startChar != '{' && startChar != '[') {
            Timber.tag(logTag).e("Cleaned string does not start with { or [: $cleanedJsonString")
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

    Timber.tag(logTag).i("Sanitization complete, cleaned JSON: $cleanedJsonString")
    if (cleanedJsonString.isBlank()) {
        Timber.tag(logTag).e("Cleaned JSON string is blank after sanitization.")
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
                            Timber.tag("JsonRepair").w(
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
                                    Timber.tag("JsonRepair").d(
                                        "Field '$name' has existing closing quote at index $trailingQuotePos after content at $lastContentPos, cleaning up",
                                    )
                                    currentJson = currentJson.substring(0, lastContentPos) +
                                        "\"" +
                                        currentJson.substring(endPosition)
                                    startIndex = lastContentPos + 2
                                } else {
                                    // No existing quote found, just add the closing quote at the right position
                                    Timber.tag("JsonRepair").w(
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
        Timber.tag("JsonRepair").e("Failed to repair JSON structure: ${e.message}")
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
    if (clazz in visited || clazz.isPrimitive || clazz == String::class.java || clazz.isEnum || clazz.isBoxedPrimitive()) return emptyMap()
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

private fun findMatchingClosingIndex(
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

/** Default cap for long descriptive strings (backstory, summaries, etc.). */
private const val AI_STRING_TRUNCATE_DEFAULT = 500

/** Chat message bodies — must not clip below UI `chat_input_limit` (default 2000). */
private const val AI_MESSAGE_TEXT_MAX = 8_192

private fun truncateForAi(
    fieldName: String,
    value: String,
): String {
    val limit =
        when (fieldName) {
            "text" -> AI_MESSAGE_TEXT_MAX
            else -> AI_STRING_TRUNCATE_DEFAULT
        }
    return if (value.length > limit) {
        "${value.take(limit - 3)}..."
    } else {
        value
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

                    is String -> truncateForAi(field.name, value)

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

private fun autoCloseJson(json: String): String {
    val stack = mutableListOf<Char>()
    var inString = false
    var escaped = false

    for (c in json) {
        if (escaped) {
            escaped = false
            continue
        }
        if (c == '\\') {
            escaped = true
            continue
        }
        if (c == '"') {
            inString = !inString
            continue
        }
        if (!inString) {
            if (c == '{' || c == '[') {
                stack.add(if (c == '{') '}' else ']')
            } else if (c == '}' || c == ']') {
                if (stack.isNotEmpty() && stack.last() == c) {
                    stack.removeAt(stack.size - 1)
                }
            }
        }
    }

    val sb = StringBuilder(json)
    if (inString) {
        sb.append('"')
    }

    for (i in stack.size - 1 downTo 0) {
        sb.append(stack[i])
    }

    return sb.toString()
}
