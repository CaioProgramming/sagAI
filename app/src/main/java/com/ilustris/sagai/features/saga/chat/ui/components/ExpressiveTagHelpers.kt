package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.ilustris.sagai.R

/**
 * Supported expressive tags for inline formatting
 */
enum class ExpressiveTag(
    val tag: String,
    val displayName: Int,
    val hint: String,
) {
    ACTION("action", R.string.sender_type_action_title, "Physical movements"),
    THINK("redacted_thinking", R.string.sender_type_thought_title, "Internal thoughts"),
    NARRATOR("narrator", R.string.sender_type_narrator_title, "Narrator voice"),
    ;

    fun openingTag() = "<$tag>"

    fun closingTag() = "</$tag>"

    fun wrapText(text: String) = "${openingTag()}$text${closingTag()}"
}

/**
 * Insert an expressive tag at the current cursor position
 *
 * Behavior:
 * - If text is selected: wraps selection with tags
 * - If no selection: inserts opening and closing tags with cursor between them
 *
 * @param currentValue Current text field value
 * @param tag The tag to insert
 * @return Updated text field value with cursor positioned correctly
 */
fun insertExpressiveTag(
    currentValue: TextFieldValue,
    tag: ExpressiveTag,
): TextFieldValue {
    val currentText = currentValue.text
    val selection = currentValue.selection

    return if (selection.start != selection.end) {
        // Text is selected - wrap it
        val selectedText = currentText.substring(selection.start, selection.end)
        val wrappedText = tag.wrapText(selectedText)
        val newText = currentText.replaceRange(selection.start, selection.end, wrappedText)
        val newCursorPosition = selection.start + wrappedText.length

        TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition),
        )
    } else {
        // No selection - insert tags with cursor between them with a breathing space
        // The space tricks the OS keyboard into allowing smart-space/predictions
        val opening = tag.openingTag()
        val closing = tag.closingTag()
        val insertPosition = selection.start
        val newText =
            currentText.substring(0, insertPosition) +
                opening + " " + closing +
                currentText.substring(insertPosition)
        val newCursorPosition = insertPosition + opening.length

        TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition),
        )
    }
}

/**
 * Detect if user typed '<' to trigger tag autocomplete
 *
 * @param text Current text
 * @param cursorPosition Current cursor position
 * @return True if autocomplete should be shown
 */
fun shouldShowTagAutocomplete(
    text: String,
    cursorPosition: Int,
): Boolean {
    // Don't show if text is empty or cursor is at position 0
    if (text.isEmpty() || cursorPosition == 0) return false

    // Check if last character before cursor is '<'
    val lastChar = text.getOrNull(cursorPosition - 1)
    if (lastChar != '<') return false

    // Check if we're not already inside a tag
    val textBeforeCursor = text.substring(0, cursorPosition)
    val openBrackets = textBeforeCursor.count { it == '<' }
    val closeBrackets = textBeforeCursor.count { it == '>' }

    // Only show if we have more open brackets than close brackets
    return openBrackets > closeBrackets
}

/**
 * Complete a tag when user selects from autocomplete
 *
 * @param currentValue Current text field value
 * @param tag The selected tag
 * @return Updated text field value with completed tag
 */
fun completeTag(
    currentValue: TextFieldValue,
    tag: ExpressiveTag,
): TextFieldValue {
    val text = currentValue.text
    val cursorPosition = currentValue.selection.start

    // Find the '<' before cursor
    val textBeforeCursor = text.substring(0, cursorPosition)
    val lastOpenBracket = textBeforeCursor.lastIndexOf('<')

    if (lastOpenBracket == -1) return currentValue

    // Remove the '<' and insert the complete tag pair
    val beforeBracket = text.substring(0, lastOpenBracket)
    val afterCursor = text.substring(cursorPosition)
    val opening = tag.openingTag()
    val closing = tag.closingTag()
    val newText = beforeBracket + opening + closing + afterCursor
    val newCursorPosition = lastOpenBracket + opening.length

    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursorPosition),
    )
}

/**
 * Check if the cursor is currently inside an expressive tag
 *
 * @param text Current text
 * @param cursorPosition Current cursor position
 * @return The tag the cursor is inside, or null if not inside any tag
 */
fun getCursorInsideTag(
    text: String,
    cursorPosition: Int,
): ExpressiveTag? {
    val normalized = normalizeLegacyThinkTags(text)
    for (tag in ExpressiveTag.entries) {
        val openTag = tag.openingTag()
        val closeTag = tag.closingTag()

        val textBeforeCursor = normalized.substring(0, cursorPosition.coerceIn(0, normalized.length))
        val lastOpenIndex = textBeforeCursor.lastIndexOf(openTag)

        if (lastOpenIndex == -1) continue

        val afterOpenTag = normalized.substring(lastOpenIndex + openTag.length)
        val closeIndex = afterOpenTag.indexOf(closeTag)

        if (closeIndex == -1) continue

        val contentStart = lastOpenIndex + openTag.length
        val contentEnd = lastOpenIndex + openTag.length + closeIndex

        if (cursorPosition >= contentStart && cursorPosition <= contentEnd) {
            return tag
        }
    }
    return null
}

/**
 * Move cursor outside the current tag (after the closing tag)
 *
 * @param currentValue Current text field value
 * @return Updated text field value with cursor after the closing tag, or same value if not in tag
 */
fun escapeCursorFromTag(currentValue: TextFieldValue): TextFieldValue {
    val text = currentValue.text
    val cursorPosition = currentValue.selection.start

    val tag = getCursorInsideTag(text, cursorPosition) ?: return currentValue

    tag.openingTag()
    val closeTag = tag.closingTag()

    // Find the closing tag position after cursor
    val afterCursor = text.substring(cursorPosition)
    val closeIndexRelative = afterCursor.indexOf(closeTag)

    if (closeIndexRelative == -1) return currentValue

    // Move cursor after the closing tag
    val newCursorPosition = cursorPosition + closeIndexRelative + closeTag.length

    return TextFieldValue(
        text = text,
        selection = TextRange(newCursorPosition),
    )
}

/**
 * Removes empty expressive tags from text.
 * E.g., "<think></think>" or "<action>   </action>" becomes ""
 *
 * @param text The text to clean
 * @return Text with empty tags removed
 */
private val EXPRESSIVE_TAG_NAMES = listOf("action", "narrator", "redacted_thinking", "think")

private val COMPLETE_TAG_REGEX =
    Regex(
        "<(action|narrator|redacted_thinking|think)>(.*?)</\\1>",
        RegexOption.DOT_MATCHES_ALL,
    )

private val TAG_MARKUP_REGEX =
    Regex("</?(?:action|narrator|redacted_thinking|think)>")

fun cleanEmptyTags(text: String): String {
    val normalized = normalizeLegacyThinkTags(text)
    val emptyTagPattern =
        Regex("<(action|narrator|redacted_thinking|think)>\\s*</(\\1)>")
    return emptyTagPattern.replace(normalized, "").trim()
}

private fun normalizeLegacyThinkTags(text: String): String =
    text
        .replace("<think>", "<redacted_thinking>")
        .replace("</think>", "</redacted_thinking>")

/**
 * Escapes cursor from tag and cleans up empty tags.
 * Use this when user presses Next/Enter to exit a tag.
 *
 * @param currentValue Current text field value
 * @return Updated text field value with cursor escaped and empty tags removed
 */
fun escapeCursorFromTagAndClean(currentValue: TextFieldValue): TextFieldValue {
    val escaped = escapeCursorFromTag(currentValue)
    val cleaned = cleanEmptyTags(escaped.text)

    // Adjust cursor position if text was cleaned
    val cursorPosition = minOf(escaped.selection.start, cleaned.length)

    return TextFieldValue(
        text = cleaned,
        selection = TextRange(cursorPosition),
    )
}

/**
 * Calculates the length of user's actual content, excluding tag markup.
 * Used for character limit validation.
 *
 * @param text The text to measure
 * @return The length of content without tag overhead
 */
fun getCleanTextLength(text: String): Int {
    val tagPattern =
        Regex(
            "<(action|narrator|redacted_thinking|think)>|</(action|narrator|redacted_thinking|think)>",
        )
    return tagPattern.replace(normalizeLegacyThinkTags(text), "").length
}

/**
 * Checks if cursor is positioned right after a closing tag.
 *
 * @param text Current text
 * @param cursorPosition Current cursor position
 * @return The tag if cursor is after its closing tag, null otherwise
 */
fun getCursorAfterClosingTag(
    text: String,
    cursorPosition: Int,
): ExpressiveTag? {
    for (tag in ExpressiveTag.entries) {
        val closeTag = tag.closingTag()
        // Check if the text before cursor ends with this closing tag
        if (cursorPosition >= closeTag.length) {
            val textBeforeCursor = text.substring(0, cursorPosition)
            if (textBeforeCursor.endsWith(closeTag)) {
                return tag
            }
        }
    }
    return null
}

/**
 * Handles tag-aware backspace: empty tags, first content char, and markup boundaries.
 */
fun handleTagAwareBackspace(currentValue: TextFieldValue): TextFieldValue? {
    if (currentValue.selection.start != currentValue.selection.end) return null

    val text = normalizeLegacyThinkTags(currentValue.text)
    val cursorPosition = currentValue.selection.start

    getCursorAfterClosingTag(text, cursorPosition)?.let { tag ->
        handleEmptyTagDeletion(text, cursorPosition, tag)?.let { return it }
    }

    val tagInside = getCursorInsideTag(text, cursorPosition)
    if (tagInside != null) {
        val openTag = tagInside.openingTag()
        val closeTag = tagInside.closingTag()
        val textBeforeCursor = text.substring(0, cursorPosition)
        val openTagIndex = textBeforeCursor.lastIndexOf(openTag)
        if (openTagIndex == -1) return null

        val afterOpenTag = text.substring(openTagIndex + openTag.length)
        val closeIndexRelative = afterOpenTag.indexOf(closeTag)
        if (closeIndexRelative == -1) return null

        val contentStart = openTagIndex + openTag.length
        val contentEnd = openTagIndex + openTag.length + closeIndexRelative
        val content = text.substring(contentStart, contentEnd).trim()

        if (content.isEmpty()) {
            return deleteTagPair(text, openTagIndex, contentEnd + closeTag.length, openTagIndex)
        }

        if (cursorPosition == contentStart || cursorPosition == contentStart + 1 && content.startsWith(" ")) {
            return deleteTagPair(text, openTagIndex, contentEnd + closeTag.length, openTagIndex)
        }
    }

    if (cursorPosition > 0) {
        val charBefore = text[cursorPosition - 1]
        if (charBefore == '<' || charBefore == '>' || isInsidePartialTagMarkup(text, cursorPosition - 1)) {
            return repairAtCursor(text, cursorPosition)?.let {
                TextFieldValue(it, TextRange(it.length.coerceAtMost(cursorPosition)))
            }
        }
    }

    return null
}

/** @deprecated Use [handleTagAwareBackspace] */
fun handleSmartBackspace(currentValue: TextFieldValue): TextFieldValue? = handleTagAwareBackspace(currentValue)

/**
 * Handles forward-delete when it would break tag markup.
 */
fun handleTagAwareDelete(currentValue: TextFieldValue): TextFieldValue? {
    if (currentValue.selection.start != currentValue.selection.end) return null

    val text = normalizeLegacyThinkTags(currentValue.text)
    val cursorPosition = currentValue.selection.start
    if (cursorPosition >= text.length) return null

    val tagInside = getCursorInsideTag(text, cursorPosition)
    if (tagInside != null) {
        val openTag = tagInside.openingTag()
        val closeTag = tagInside.closingTag()
        val textBeforeCursor = text.substring(0, cursorPosition)
        val openTagIndex = textBeforeCursor.lastIndexOf(openTag)
        if (openTagIndex == -1) return null

        val afterOpenTag = text.substring(openTagIndex + openTag.length)
        val closeIndexRelative = afterOpenTag.indexOf(closeTag)
        if (closeIndexRelative == -1) return null

        val contentEnd = openTagIndex + openTag.length + closeIndexRelative
        val closeTagStart = contentEnd
        val closeTagEnd = closeTagStart + closeTag.length

        if (cursorPosition in closeTagStart until closeTagEnd) {
            return deleteTagPair(text, openTagIndex, closeTagEnd, openTagIndex)
        }
    }

    if (text[cursorPosition] == '<' || text[cursorPosition] == '>' || isInsidePartialTagMarkup(text, cursorPosition)) {
        return repairAtCursor(text, cursorPosition)?.let {
            TextFieldValue(it, TextRange(cursorPosition.coerceAtMost(it.length)))
        }
    }

    return null
}

private fun deleteTagPair(
    text: String,
    tagStart: Int,
    tagEnd: Int,
    newCursor: Int,
): TextFieldValue {
    val newText = text.substring(0, tagStart) + text.substring(tagEnd)
    return TextFieldValue(newText, TextRange(newCursor.coerceIn(0, newText.length)))
}

private fun isInsidePartialTagMarkup(
    text: String,
    index: Int,
): Boolean {
    val start = text.lastIndexOf('<', index).takeIf { it >= 0 } ?: return false
    val end = text.indexOf('>', start)
    if (end == -1 || index > end) return false
    val fragment = text.substring(start, end + 1)
    return EXPRESSIVE_TAG_NAMES.any { name -> fragment.contains(name) }
}

private fun repairAtCursor(
    text: String,
    cursorPosition: Int,
): String? {
    val tagInside = getCursorInsideTag(text, cursorPosition)
    if (tagInside != null) {
        return stripTag(TextFieldValue(text, TextRange(cursorPosition))).text
    }
    return repairBrokenTags(text)
}

/**
 * Removes orphan/partial tag markup while preserving readable content.
 */
fun repairBrokenTags(text: String): String {
    var result = normalizeLegacyThinkTags(text)
    if (!TAG_MARKUP_REGEX.containsMatchIn(result)) return result

    val completeRanges = mutableListOf<IntRange>()
    COMPLETE_TAG_REGEX.findAll(result).forEach { completeRanges.add(it.range) }

    fun isInsideCompleteRange(index: Int): Boolean = completeRanges.any { index in it }

    TAG_MARKUP_REGEX.findAll(result).toList().reversed().forEach { match ->
        if (match.range.any { isInsideCompleteRange(it) }) return@forEach
        result = result.removeRange(match.range)
    }

    return result
}

fun hasBrokenTagMarkup(text: String): Boolean {
    val normalized = normalizeLegacyThinkTags(text)
    val withoutComplete = COMPLETE_TAG_REGEX.replace(normalized, "$2")
    return TAG_MARKUP_REGEX.containsMatchIn(withoutComplete)
}

/**
 * Sanitizes text after edits so partial tag markup is never left exposed.
 */
fun sanitizeExpressiveTagsOnChange(
    @Suppress("UNUSED_PARAMETER") oldValue: TextFieldValue,
    newValue: TextFieldValue,
): TextFieldValue {
    var text = normalizeLegacyThinkTags(newValue.text)
    var cursor = newValue.selection.start.coerceIn(0, text.length)

    if (hasBrokenTagMarkup(text)) {
        text = repairBrokenTags(text)
        cursor = cursor.coerceIn(0, text.length)
    }

    return TextFieldValue(text, TextRange(cursor))
}

fun processInputChange(
    previousValue: TextFieldValue,
    newValue: TextFieldValue,
    maxContentLength: Int,
): TextFieldValue? {
    if (newValue.selection.start != newValue.selection.end) {
        return if (getCleanTextLength(newValue.text) <= maxContentLength) {
            sanitizeExpressiveTagsOnChange(previousValue, newValue)
        } else {
            null
        }
    }

    if (newValue.text.length == previousValue.text.length - 1) {
        handleTagAwareBackspace(previousValue)?.let { handled ->
            return if (getCleanTextLength(handled.text) <= maxContentLength) {
                sanitizeExpressiveTagsOnChange(previousValue, handled)
            } else {
                null
            }
        }
        handleTagAwareDelete(previousValue)?.let { handled ->
            return if (getCleanTextLength(handled.text) <= maxContentLength) {
                sanitizeExpressiveTagsOnChange(previousValue, handled)
            } else {
                null
            }
        }
    }

    return if (getCleanTextLength(newValue.text) <= maxContentLength) {
        sanitizeExpressiveTagsOnChange(previousValue, newValue)
    } else {
        null
    }
}

/**
 * Helper function to handle deletion when cursor is after a closing tag.
 */
private fun handleEmptyTagDeletion(
    text: String,
    cursorPosition: Int,
    tag: ExpressiveTag,
): TextFieldValue? {
    val openTag = tag.openingTag()
    val closeTag = tag.closingTag()

    // Find the matching opening tag
    val textBeforeCursor = text.substring(0, cursorPosition - closeTag.length)
    val openTagIndex = textBeforeCursor.lastIndexOf(openTag)

    if (openTagIndex == -1) return null

    // Get the content between tags
    val contentStart = openTagIndex + openTag.length
    val contentEnd = cursorPosition - closeTag.length
    val content = text.substring(contentStart, contentEnd).trim()

    // Only handle empty tags - let normal backspace work for tags with content
    if (content.isNotEmpty()) return null

    // Empty tag - delete the entire tag pair
    val beforeTag = text.substring(0, openTagIndex)
    val afterTag = text.substring(cursorPosition)
    val newText = beforeTag + afterTag

    return TextFieldValue(
        text = newText,
        selection = TextRange(openTagIndex),
    )
}

/**
 * Strips the current tag entirely from the text, retaining its inner content.
 * Useful when the user wants to cancel the tag mode without losing typed text.
 *
 * @param currentValue Current text field value
 * @return Updated text field value with the tag removed
 */
fun stripTag(currentValue: TextFieldValue): TextFieldValue {
    val text = currentValue.text
    val cursorPosition = currentValue.selection.start
    val tag = getCursorInsideTag(text, cursorPosition) ?: return currentValue

    val openTag = tag.openingTag()
    val closeTag = tag.closingTag()

    // Find the opening tag
    val textBeforeCursor = text.substring(0, cursorPosition)
    val openTagIndex = textBeforeCursor.lastIndexOf(openTag)

    if (openTagIndex == -1) return currentValue

    // Find the closing tag
    val textAfterOpenTag = text.substring(openTagIndex + openTag.length)
    val closeIndexRelative = textAfterOpenTag.indexOf(closeTag)

    if (closeIndexRelative == -1) return currentValue

    val contentStart = openTagIndex + openTag.length
    val contentEnd = openTagIndex + openTag.length + closeIndexRelative

    val beforeTag = text.substring(0, openTagIndex)
    val content = text.substring(contentStart, contentEnd)
    val afterTag = text.substring(contentEnd + closeTag.length)

    val newText = beforeTag + content + afterTag

    // Adjust cursor position: shift by openTag.length backwards
    val newCursorPosition = maxOf(0, cursorPosition - openTag.length)

    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursorPosition),
    )
}
