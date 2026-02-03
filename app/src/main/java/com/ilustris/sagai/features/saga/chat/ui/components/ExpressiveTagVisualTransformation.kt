package com.ilustris.sagai.features.saga.chat.ui.components

import ai.atick.material.MaterialColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Visual transformation for expressive tags in input field
 *
 * Styles tags for better visual feedback while typing:
 * - <action>text</action> → Yellow/amber text (bold)
 * - <think>text</think> → Faded text (50% alpha)
 * - <narrator>text</narrator> → Italic text
 *
 * Tags themselves are shown but content is styled for clarity.
 */
class ExpressiveTagVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styledText = styleExpressiveTags(text.text)
        val offsetMapping = ExpressiveTagOffsetMapping(text.text)
        return TransformedText(styledText, offsetMapping)
    }

    private fun styleExpressiveTags(text: String): AnnotatedString =
        buildAnnotatedString {
            if (text.isEmpty()) {
                append(text)
                return@buildAnnotatedString
            }

            val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
            val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
            val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

            // Find all matches with their positions
            val matches = mutableListOf<TagMatch>()

            actionRegex.findAll(text).forEach { match ->
                matches.add(
                    TagMatch(
                        match.range,
                        TagType.ACTION,
                        match.value,
                        match.groupValues[1],
                    ),
                )
            }

            thinkRegex.findAll(text).forEach { match ->
                matches.add(TagMatch(match.range, TagType.THINK, match.value, match.groupValues[1]))
            }

            narratorRegex.findAll(text).forEach { match ->
                matches.add(
                    TagMatch(
                        match.range,
                        TagType.NARRATOR,
                        match.value,
                        match.groupValues[1],
                    ),
                )
            }

            // Sort matches by start position and handle nesting (prefer outermost tags)
            val sortedMatches = matches.sortedBy { it.range.first }
            val filteredMatches = mutableListOf<TagMatch>()
            var lastEnd = -1

            for (match in sortedMatches) {
                if (match.range.first > lastEnd) {
                    filteredMatches.add(match)
                    lastEnd = match.range.last
                }
            }

            var currentIndex = 0

            for (match in filteredMatches) {
                // Add plain text before this match
                if (currentIndex < match.range.first) {
                    append(text.substring(currentIndex, match.range.first))
                }

                // Only render the content (hide the tags)
                val contentStartPos = length
                append(match.content)
                val contentEndPos = length

                // Apply styling to the content only
                if (contentStartPos < contentEndPos && contentEndPos <= length) {
                    try {
                        when (match.type) {
                            TagType.ACTION -> {
                                // Yellow/amber, bold, like a subtitle
                                addStyle(
                                    SpanStyle(
                                        color = MaterialColor.Amber400,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    contentStartPos,
                                    contentEndPos,
                                )
                            }

                            TagType.THINK -> {
                                // Faded text - use semi-transparent white/black depending on theme
                                // In the input field we don't have access to the text color, so use a neutral gray
                                addStyle(
                                    SpanStyle(
                                        color = Color.Gray,
                                    ),
                                    contentStartPos,
                                    contentEndPos,
                                )
                            }

                            TagType.NARRATOR -> {
                                // Italic text
                                addStyle(
                                    SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                    ),
                                    contentStartPos,
                                    contentEndPos,
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                currentIndex = match.range.last + 1
            }

            // Add remaining text
            if (currentIndex < text.length) {
                append(text.substring(currentIndex))
            }
        }

    private data class TagMatch(
        val range: IntRange,
        val type: TagType,
        val fullText: String,
        val content: String,
    )

    private enum class TagType {
        ACTION,
        THINK,
        NARRATOR,
    }
}

/**
 * Offset mapping for expressive tags that hides the tag syntax
 */
private class ExpressiveTagOffsetMapping(
    private val originalText: String,
) : OffsetMapping {
    private val tagRanges = mutableListOf<Pair<IntRange, Int>>() // (original range, content length)

    init {
        val actionRegex = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
        val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
        val narratorRegex = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

        val allMatches = mutableListOf<MatchResult>()
        allMatches.addAll(actionRegex.findAll(originalText))
        allMatches.addAll(thinkRegex.findAll(originalText))
        allMatches.addAll(narratorRegex.findAll(originalText))

        val sortedMatches = allMatches.sortedBy { it.range.first }
        var lastEnd = -1

        sortedMatches.forEach { match ->
            if (match.range.first > lastEnd) {
                val contentLength = match.groupValues[1].length
                tagRanges.add(match.range to contentLength)
                lastEnd = match.range.last
            }
        }
    }

    override fun originalToTransformed(offset: Int): Int {
        var transformed = offset
        for ((range, _) in tagRanges) {
            when {
                offset <= range.first -> {
                    break
                }

                offset <= range.last + 1 -> {
                    // Inside a tag - map to content position
                    val tagName =
                        when {
                            originalText.substring(range.first).startsWith("<action>") -> "action"

                            originalText.substring(range.first).startsWith("<think>") -> "think"

                            originalText
                                .substring(range.first)
                                .startsWith("<narrator>") -> "narrator"

                            else -> ""
                        }
                    val openingTagLength = tagName.length + 2 // "<tag>"
                    val closingTagLength = tagName.length + 3 // "</tag>"
                    val totalTagLength = openingTagLength + closingTagLength

                    // Calculate position relative to content
                    val relativePos = offset - range.first
                    val rangeSize = range.last - range.first + 1
                    transformed =
                        when {
                            relativePos <= openingTagLength -> transformed - openingTagLength
                            relativePos > rangeSize - closingTagLength -> transformed - totalTagLength
                            else -> transformed - openingTagLength
                        }
                    break
                }

                else -> {
                    // After this tag - subtract the tag syntax length
                    val tagName =
                        when {
                            originalText.substring(range.first).startsWith("<action>") -> "action"

                            originalText.substring(range.first).startsWith("<think>") -> "think"

                            originalText
                                .substring(range.first)
                                .startsWith("<narrator>") -> "narrator"

                            else -> ""
                        }
                    val totalTagLength = (tagName.length + 2) + (tagName.length + 3)
                    transformed -= totalTagLength
                }
            }
        }
        return transformed.coerceAtLeast(0)
    }

    override fun transformedToOriginal(offset: Int): Int {
        var original = offset
        for ((range, contentLength) in tagRanges) {
            val tagName =
                when {
                    originalText.substring(range.first).startsWith("<action>") -> "action"
                    originalText.substring(range.first).startsWith("<think>") -> "think"
                    originalText.substring(range.first).startsWith("<narrator>") -> "narrator"
                    else -> ""
                }
            val openingTagLength = tagName.length + 2
            val closingTagLength = tagName.length + 3
            val totalTagLength = openingTagLength + closingTagLength

            val transformedStart = originalToTransformed(range.first)
            val transformedEnd = transformedStart + contentLength

            when {
                offset <= transformedStart -> {
                    break
                }

                offset <= transformedEnd -> {
                    original = range.first + openingTagLength + (offset - transformedStart)
                    break
                }

                else -> {
                    original += totalTagLength
                }
            }
        }
        return original.coerceAtMost(originalText.length)
    }
}
