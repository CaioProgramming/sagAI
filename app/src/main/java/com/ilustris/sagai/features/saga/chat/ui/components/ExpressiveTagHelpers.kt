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
        // No selection - insert tags with cursor between them
        val opening = tag.openingTag()
        val closing = tag.closingTag()
        val insertPosition = selection.start
        val newText =
            currentText.substring(0, insertPosition) +
                opening + closing +
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
    for (tag in ExpressiveTag.entries) {
        val openTag = tag.openingTag()
        val closeTag = tag.closingTag()

        // Find the last opening tag before cursor
        val textBeforeCursor = text.substring(0, cursorPosition)
        val lastOpenIndex = textBeforeCursor.lastIndexOf(openTag)

        if (lastOpenIndex == -1) continue

        // Check if there's a closing tag after the opening tag but before or at cursor
        val afterOpenTag = text.substring(lastOpenIndex + openTag.length)
        val closeIndex = afterOpenTag.indexOf(closeTag)

        if (closeIndex == -1) continue

        // Calculate the absolute positions
        val contentStart = lastOpenIndex + openTag.length
        val contentEnd = lastOpenIndex + openTag.length + closeIndex

        // Check if cursor is between open and close tags (inside the content area)
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
fun cleanEmptyTags(text: String): String {
    val emptyTagPattern = Regex("<(think|action|narrator)>\\s*</(\\1)>")
    return emptyTagPattern.replace(text, "").trim()
}

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
    val tagPattern = Regex("<(think|action|narrator)>|</(think|action|narrator)>")
    return tagPattern.replace(text, "").length
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
 * Handles smart backspace for empty tags only.
 * When cursor is right after an empty closing tag, deletes the entire tag pair.
 * When cursor is inside an empty tag, also deletes the entire tag pair.
 * If the tag has content, returns null to allow normal backspace behavior.
 *
 * @param currentValue Current text field value
 * @return Updated text field value with empty tag deleted, or null if normal backspace should occur
 */
fun handleSmartBackspace(currentValue: TextFieldValue): TextFieldValue? {
    val text = currentValue.text
    val cursorPosition = currentValue.selection.start

    // Only handle if no text is selected
    if (currentValue.selection.start != currentValue.selection.end) return null

    // Case 1: Cursor is right after a closing tag
    val tagAfterClose = getCursorAfterClosingTag(text, cursorPosition)
    if (tagAfterClose != null) {
        return handleEmptyTagDeletion(text, cursorPosition, tagAfterClose)
    }

    // Case 2: Cursor is inside an empty tag (user just deleted all content)
    val tagInside = getCursorInsideTag(text, cursorPosition)
    if (tagInside != null) {
        val openTag = tagInside.openingTag()
        val closeTag = tagInside.closingTag()

        // Find the opening tag position
        val textBeforeCursor = text.substring(0, cursorPosition)
        val openTagIndex = textBeforeCursor.lastIndexOf(openTag)

        if (openTagIndex == -1) return null

        // Find closing tag position
        val afterOpenTag = text.substring(openTagIndex + openTag.length)
        val closeIndexRelative = afterOpenTag.indexOf(closeTag)

        if (closeIndexRelative == -1) return null

        // Get content between tags
        val contentStart = openTagIndex + openTag.length
        val contentEnd = openTagIndex + openTag.length + closeIndexRelative
        val content = text.substring(contentStart, contentEnd).trim()

        // Only delete if empty - let normal backspace work for tags with content
        if (content.isNotEmpty()) return null

        // Empty tag - delete the entire tag pair
        val beforeTag = text.substring(0, openTagIndex)
        val afterTag = text.substring(contentEnd + closeTag.length)
        val newText = beforeTag + afterTag

        return TextFieldValue(
            text = newText,
            selection = TextRange(openTagIndex),
        )
    }

    return null
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
