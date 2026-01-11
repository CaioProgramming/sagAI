package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.ilustris.sagai.R

/**
 * Supported expressive tags for inline formatting
 */
enum class ExpressiveTag(
    val tag: String,
    val displayName: String,
    val hint: String,
    @DrawableRes val iconRes: Int,
) {
    ACTION("action", "Action", "Physical movements", R.drawable.ic_feather),
    THINK("think", "Think", "Internal thoughts", R.drawable.ic_spark),
    NARRATOR("narrator", "Narrator", "Narrator voice", R.drawable.ic_note),
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
