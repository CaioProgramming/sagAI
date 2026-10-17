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

    /** Renders as `<think>…</think>` in message text. */
    THINK("think", R.string.sender_type_thought_title, "Internal thoughts"),
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
    // A selection dragged right to left has start > end, so read it as a range.
    val selectionStart = currentValue.selection.min.coerceIn(0, currentText.length)
    val selectionEnd = currentValue.selection.max.coerceIn(0, currentText.length)

    return if (selectionStart != selectionEnd) {
        // Text is selected - wrap it
        val selectedText = currentText.substring(selectionStart, selectionEnd)
        val wrappedText = tag.wrapText(selectedText)
        val newText = currentText.replaceRange(selectionStart, selectionEnd, wrappedText)
        val newCursorPosition = selectionStart + wrappedText.length

        TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition),
        )
    } else {
        // No selection - insert opening tag only; closing is added on exit/send
        val opening = tag.openingTag()
        val insertPosition = selectionStart
        val newText =
            currentText.substring(0, insertPosition) +
                opening +
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
 * Bounds of editable content when the cursor is inside an expressive tag.
 */
data class TagContentBounds(
    val contentStart: Int,
    val contentEnd: Int,
)

fun getTagContentBounds(
    text: String,
    cursorPosition: Int,
): TagContentBounds? {
    val normalized = normalizeThinkTags(text)
    val tag = getCursorInsideTag(normalized, cursorPosition) ?: return null
    val openTag = tag.openingTag()
    val closeTag = tag.closingTag()
    val textBeforeCursor = normalized.substring(0, cursorPosition.coerceIn(0, normalized.length))
    val openIndex = textBeforeCursor.lastIndexOf(openTag)
    if (openIndex == -1) return null

    val contentStart = openIndex + openTag.length
    val afterOpen = normalized.substring(openIndex + openTag.length)
    val closeRelative = afterOpen.indexOf(closeTag)
    val contentEnd =
        if (closeRelative == -1) {
            normalized.length
        } else {
            openIndex + openTag.length + closeRelative
        }

    return TagContentBounds(contentStart, contentEnd)
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
    val normalized = normalizeThinkTags(text)
    for (tag in ExpressiveTag.entries) {
        val openTag = tag.openingTag()
        val closeTag = tag.closingTag()

        val textBeforeCursor = normalized.substring(0, cursorPosition.coerceIn(0, normalized.length))
        val lastOpenIndex = textBeforeCursor.lastIndexOf(openTag)

        if (lastOpenIndex == -1) continue

        val afterOpenTag = normalized.substring(lastOpenIndex + openTag.length)
        val closeIndex = afterOpenTag.indexOf(closeTag)

        val contentStart = lastOpenIndex + openTag.length

        if (closeIndex == -1) {
            if (cursorPosition >= contentStart) {
                return tag
            }
            continue
        }

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
    val text = normalizeThinkTags(currentValue.text)
    val cursorPosition = currentValue.selection.start

    val tag = getCursorInsideTag(text, cursorPosition) ?: return currentValue

    val openTag = tag.openingTag()
    val closeTag = tag.closingTag()

    val textBeforeCursor = text.substring(0, cursorPosition.coerceIn(0, text.length))
    val openIndex = textBeforeCursor.lastIndexOf(openTag)
    if (openIndex == -1) return currentValue

    val afterOpen = text.substring(openIndex + openTag.length)
    val closeRelative = afterOpen.indexOf(closeTag)

    if (closeRelative == -1) {
        val contentStart = openIndex + openTag.length
        val contentEnd = findNextExpressiveTagOpen(text, contentStart) ?: text.length
        val newText = text.substring(0, contentEnd) + closeTag + text.substring(contentEnd)
        val newCursorPosition = contentEnd + closeTag.length
        return TextFieldValue(newText, TextRange(newCursorPosition))
    }

    val closeStart = openIndex + openTag.length + closeRelative
    val newCursorPosition = closeStart + closeTag.length

    return TextFieldValue(
        text = text,
        selection = TextRange(newCursorPosition),
    )
}

/**
 * Closes any expressive tags that were left open while editing.
 * Inserts closing tags at the end of text for any unclosed opening tags.
 */
fun ensureExpressiveTagsClosed(text: String): String {
    var result = normalizeThinkTags(text)
    for (tag in ExpressiveTag.entries) {
        val openTag = tag.openingTag()
        val closeTag = tag.closingTag()
        var searchFrom = 0
        var unclosedCount = 0

        while (true) {
            val openIndex = result.indexOf(openTag, searchFrom)
            if (openIndex == -1) break
            val contentStart = openIndex + openTag.length
            val closeIndex = result.indexOf(closeTag, contentStart)
            if (closeIndex == -1) {
                unclosedCount++
                searchFrom = result.length  // Skip to end
            } else {
                searchFrom = closeIndex + closeTag.length
            }
        }

        // Append closing tags for all unclosed opening tags of this type
        repeat(unclosedCount) {
            result += closeTag
        }
    }
    return result
}

fun finalizeInputForSend(currentValue: TextFieldValue): TextFieldValue {
    val escaped = escapeCursorFromTagAndClean(currentValue)
    val closed = ensureExpressiveTagsClosed(escaped.text)
    val cursor = escaped.selection.start.coerceIn(0, closed.length)
    return TextFieldValue(closed, TextRange(cursor))
}

/**
 * Removes empty expressive tags from text.
 * E.g., "<think></think>" or "<action>   </action>" becomes ""
 *
 * @param text The text to clean
 * @return Text with empty tags removed
 */
private val EXPRESSIVE_TAG_NAMES = listOf("action", "narrator", "think")

private val LEGACY_THINK_OPEN = "<" + "redacted_thinking" + ">"
private val LEGACY_THINK_CLOSE = "</" + "redacted_thinking" + ">"

private val COMPLETE_TAG_REGEX =
    Regex(
        "<(action|narrator|think)>(.*?)</\\1>",
        RegexOption.DOT_MATCHES_ALL,
    )

private val TAG_MARKUP_REGEX =
    Regex("</?(?:action|narrator|think)>")

fun cleanEmptyTags(text: String): String {
    val normalized = normalizeThinkTags(text)
    val emptyTagPattern = Regex("<(action|narrator|think)>\\s*</(\\1)>")
    var result = emptyTagPattern.replace(normalized, "").trim()
    for (tag in ExpressiveTag.entries) {
        val openOnlyPattern = Regex("${Regex.escape(tag.openingTag())}\\s*$")
        result = openOnlyPattern.replace(result, "")
    }
    return result
}

private fun normalizeThinkTags(text: String): String =
    text
        .replace(LEGACY_THINK_OPEN, ExpressiveTag.THINK.openingTag())
        .replace(LEGACY_THINK_CLOSE, ExpressiveTag.THINK.closingTag())

private fun findNextExpressiveTagOpen(
    text: String,
    fromIndex: Int,
): Int? {
    val openings = ExpressiveTag.entries.map { it.openingTag() }
    return openings
        .mapNotNull { tag -> text.indexOf(tag, fromIndex).takeIf { it != -1 } }
        .minOrNull()
}

private fun isTagContentEmpty(
    text: String,
    contentStart: Int,
    contentEnd: Int,
): Boolean = text.substring(contentStart, contentEnd).trim().isEmpty()

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

private val expressiveTagMarkers = Regex("<(action|narrator|think)>|</(action|narrator|think)>")

/**
 * The message as the reader sees it: the tag markers go, the words inside them stay. Distinct from
 * `RichTextParser.stripTags`, which drops the tagged content entirely for TTS.
 *
 * @param text The text to clean
 * @return The same text without the tag markup
 */
fun stripExpressiveTags(text: String): String {
    if (!text.contains('<')) return text
    return expressiveTagMarkers.replace(normalizeThinkTags(text), "")
}

/**
 * Calculates the length of user's actual content, excluding tag markup.
 * Used for character limit validation.
 *
 * @param text The text to measure
 * @return The length of content without tag overhead
 */
fun getCleanTextLength(text: String): Int = stripExpressiveTags(text).length

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
 * Handles tag-aware backspace: only removes the whole tag when it is empty and the user
 * would delete tag markup next. Otherwise defers to normal character deletion.
 */
fun handleTagAwareBackspace(currentValue: TextFieldValue): TextFieldValue? {
    if (currentValue.selection.start != currentValue.selection.end) return null

    val text = normalizeThinkTags(currentValue.text)
    val cursorPosition = currentValue.selection.start

    getCursorAfterClosingTag(text, cursorPosition)?.let { tag ->
        handleEmptyTagDeletion(text, cursorPosition, tag)?.let { return it }
        // The closing markup is invisible, so backspace here removes the last visible character.
        deleteLastContentCharacter(text, cursorPosition, tag)?.let { return it }
    }

    val tagInside = getCursorInsideTag(text, cursorPosition)
    if (tagInside != null) {
        val openTag = tagInside.openingTag()
        val closeTag = tagInside.closingTag()
        val textBeforeCursor = text.substring(0, cursorPosition)
        val openTagIndex = textBeforeCursor.lastIndexOf(openTag)
        if (openTagIndex == -1) return null

        val contentStart = openTagIndex + openTag.length
        val afterOpenTag = text.substring(contentStart)
        val closeIndexRelative = afterOpenTag.indexOf(closeTag)

        if (closeIndexRelative == -1) {
            if (cursorPosition == contentStart &&
                isTagContentEmpty(text, contentStart, text.length)
            ) {
                return deleteTagPair(text, openTagIndex, text.length, openTagIndex)
            }
            // Backspace at the start of the content takes the tag off and keeps what was written.
            if (cursorPosition == contentStart) {
                return stripTag(TextFieldValue(text, TextRange(cursorPosition)))
            }
            return null
        }

        val contentEnd = contentStart + closeIndexRelative
        if (isTagContentEmpty(text, contentStart, contentEnd) && cursorPosition == contentStart) {
            return deleteTagPair(text, openTagIndex, contentEnd + closeTag.length, openTagIndex)
        }
        if (cursorPosition == contentStart) {
            return stripTag(TextFieldValue(text, TextRange(cursorPosition)))
        }
        return null
    }

    // Only real tag markup is protected; a stray '<' or '>' the player typed is plain text.
    if (cursorPosition > 0 && isInsidePartialTagMarkup(text, cursorPosition - 1)) {
        return repairAtCursor(text, cursorPosition)?.let {
            val newCursor = (cursorPosition - 1).coerceIn(0, it.length)
            TextFieldValue(it, TextRange(newCursor))
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

    val text = normalizeThinkTags(currentValue.text)
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

        val contentStart = openTagIndex + openTag.length
        val contentEnd = contentStart + closeIndexRelative
        val closeTagEnd = contentEnd + closeTag.length

        // The closing markup is invisible, so delete here removes what is visibly next: the first
        // character after the tag. An empty tag has nothing else to give, so it goes away whole.
        if (cursorPosition in contentEnd until closeTagEnd) {
            if (isTagContentEmpty(text, contentStart, contentEnd)) {
                return deleteTagPair(text, openTagIndex, closeTagEnd, openTagIndex)
            }
            val next = text.getOrNull(closeTagEnd)
            if (next == null || next == '<') return currentValue
            return TextFieldValue(text.removeRange(closeTagEnd, closeTagEnd + 1), TextRange(cursorPosition))
        }
    }

    if (isInsidePartialTagMarkup(text, cursorPosition)) {
        return repairAtCursor(text, cursorPosition)?.let {
            TextFieldValue(it, TextRange(cursorPosition.coerceAtMost(it.length)))
        }
    }

    return null
}

private fun deleteLastContentCharacter(
    text: String,
    cursorAfterClosingTag: Int,
    tag: ExpressiveTag,
): TextFieldValue? {
    val closeTag = tag.closingTag()
    val contentEnd = cursorAfterClosingTag - closeTag.length
    val openIndex = text.substring(0, contentEnd).lastIndexOf(tag.openingTag())
    if (openIndex == -1) return null
    val contentStart = openIndex + tag.openingTag().length
    if (contentEnd <= contentStart) return null
    return TextFieldValue(text.removeRange(contentEnd - 1, contentEnd), TextRange(contentEnd - 1))
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

private val WHOLE_TAG_MARKUP_REGEX = Regex("</?(?:action|narrator|think)>")

/** True when [index] falls inside a real tag marker such as `<action>` or `</think>`. */
private fun isInsidePartialTagMarkup(
    text: String,
    index: Int,
): Boolean {
    if (index !in text.indices) return false
    val start = text.lastIndexOf('<', index).takeIf { it >= 0 } ?: return false
    val end = text.indexOf('>', start)
    if (end == -1 || index > end) return false
    return WHOLE_TAG_MARKUP_REGEX.matches(text.substring(start, end + 1))
}

private fun repairAtCursor(
    text: String,
    cursorPosition: Int,
): String? {
    val bounds = getTagContentBounds(text, cursorPosition)
    if (bounds != null &&
        text
            .substring(bounds.contentStart, bounds.contentEnd)
            .trim()
            .isNotEmpty()
    ) {
        return null
    }
    return repairBrokenTags(text)
}

/**
 * Removes orphan/partial tag markup while preserving readable content.
 */
fun repairBrokenTags(text: String): String {
    var result = normalizeThinkTags(text)
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
    val normalized = normalizeThinkTags(text)
    if (hasTrailingOpenExpressiveTag(normalized)) return false
    val withoutComplete = COMPLETE_TAG_REGEX.replace(normalized, "$2")
    return TAG_MARKUP_REGEX.containsMatchIn(withoutComplete)
}

private fun hasTrailingOpenExpressiveTag(text: String): Boolean {
    for (tag in ExpressiveTag.entries) {
        val openTag = tag.openingTag()
        val closeTag = tag.closingTag()
        var searchFrom = 0
        while (true) {
            val openIndex = text.indexOf(openTag, searchFrom)
            if (openIndex == -1) break
            val contentStart = openIndex + openTag.length
            val closeIndex = text.indexOf(closeTag, contentStart)
            if (closeIndex == -1) return true
            searchFrom = closeIndex + closeTag.length
        }
    }
    return false
}

private fun textNeedsTagSanitization(text: String): Boolean =
    text.contains('<') &&
        (
            TAG_MARKUP_REGEX.containsMatchIn(text) ||
                text.contains(LEGACY_THINK_OPEN) ||
                hasBrokenTagMarkup(text)
        )

/**
 * Sanitizes text after edits so partial tag markup is never left exposed.
 * Preserves IME composition so keyboard suggestions keep working.
 */
fun sanitizeExpressiveTagsOnChange(
    @Suppress("UNUSED_PARAMETER") oldValue: TextFieldValue,
    newValue: TextFieldValue,
): TextFieldValue {
    if (!textNeedsTagSanitization(newValue.text)) {
        return newValue
    }

    var text = normalizeThinkTags(newValue.text)
    if (text == newValue.text && !hasBrokenTagMarkup(text)) {
        return newValue
    }

    var selection = newValue.selection
    var composition = newValue.composition

    if (hasBrokenTagMarkup(text)) {
        val beforeLength = text.length
        text = repairBrokenTags(text)
        val delta = text.length - beforeLength
        if (delta != 0) {
            selection =
                TextRange(
                    (selection.start + delta).coerceIn(0, text.length),
                    (selection.end + delta).coerceIn(0, text.length),
                )
            composition =
                composition?.let {
                    TextRange(
                        (it.start + delta).coerceIn(0, text.length),
                        (it.end + delta).coerceIn(0, text.length),
                    )
                }
        }
    }

    selection =
        TextRange(
            selection.start.coerceIn(0, text.length),
            selection.end.coerceIn(0, text.length),
        )

    return TextFieldValue(
        text = text,
        selection = selection,
        composition = composition,
    )
}

fun processInputChange(
    previousValue: TextFieldValue,
    newValue: TextFieldValue,
    maxContentLength: Int,
): TextFieldValue? {
    if (getCleanTextLength(newValue.text) > maxContentLength) return null

    if (!newValue.text.contains('<')) {
        return newValue
    }

    if (newValue.selection.start != newValue.selection.end) {
        return sanitizeExpressiveTagsOnChange(previousValue, newValue)
    }

    if (newValue.text.length == previousValue.text.length - 1) {
        // The cursor tells which key it was: backspace pulls it one back, delete leaves it put.
        val handled =
            when (newValue.selection.start) {
                previousValue.selection.start - 1 -> handleTagAwareBackspace(previousValue)
                previousValue.selection.start -> handleTagAwareDelete(previousValue)
                else -> null
            }
        if (handled != null) {
            return if (getCleanTextLength(handled.text) <= maxContentLength) {
                sanitizeExpressiveTagsOnChange(previousValue, handled)
            } else {
                null
            }
        }
    }

    return sanitizeExpressiveTagsOnChange(previousValue, newValue)
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

    val contentStart = openTagIndex + openTag.length

    if (closeIndexRelative == -1) {
        val beforeTag = text.substring(0, openTagIndex)
        val content = text.substring(contentStart)
        val newText = beforeTag + content
        val newCursorPosition = maxOf(0, cursorPosition - openTag.length)
        return TextFieldValue(newText, TextRange(newCursorPosition))
    }

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
