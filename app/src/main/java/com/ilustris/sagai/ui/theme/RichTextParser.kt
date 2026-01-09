package com.ilustris.sagai.ui.theme

/**
 * Parsed message structure containing text segments with different formatting styles
 */
data class ParsedMessage(val segments: List<TextSegment>)

/**
 * Represents a segment of text with a specific formatting style
 */
sealed class TextSegment {
    /**
     * Plain text without any special formatting
     */
    data class Plain(val text: String) : TextSegment()

    /**
     * Action text - physical movements, environmental changes
     * Renders with levitation animation and amber color
     */
    data class Action(val text: String) : TextSegment()

    /**
     * Thought text - internal monologue, hidden behind interactive stars
     * Renders with starry placeholder, revealed on tap
     */
    data class Think(val text: String) : TextSegment()

    /**
     * Narrator text - omniscient narration or dramatic context
     * Renders in an inline bordered box
     */
    data class Narrator(val text: String) : TextSegment()
}

/**
 * Parser for rich text messages with embedded formatting tags
 *
 * Supported tags:
 * - <action>...</action> for physical movements
 * - <think>...</think> for internal thoughts
 * - <narrator>...</narrator> for narrator voice
 *
 * Example: "Hello <action>waves</action> <think>I'm nervous</think>"
 */
object RichTextParser {

    private val ACTION_REGEX = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL)
    private val THINK_REGEX = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
    private val NARRATOR_REGEX = Regex("<narrator>(.*?)</narrator>", RegexOption.DOT_MATCHES_ALL)

    /**
     * Parses a text string into structured segments
     *
     * @param text Raw text with embedded tags
     * @return ParsedMessage with ordered list of text segments
     */
    fun parse(text: String): ParsedMessage {
        if (text.isBlank()) {
            return ParsedMessage(emptyList())
        }

        // Find all tag matches with their positions
        val matches = mutableListOf<TagMatch>()

        ACTION_REGEX.findAll(text).forEach { match ->
            matches.add(TagMatch(match.range, TagType.ACTION, match.groupValues[1]))
        }

        THINK_REGEX.findAll(text).forEach { match ->
            matches.add(TagMatch(match.range, TagType.THINK, match.groupValues[1]))
        }

        NARRATOR_REGEX.findAll(text).forEach { match ->
            matches.add(TagMatch(match.range, TagType.NARRATOR, match.groupValues[1]))
        }

        // Sort matches by start position
        matches.sortBy { it.range.first }

        // Build segments list
        val segments = mutableListOf<TextSegment>()
        var currentIndex = 0

        for (match in matches) {
            // Add plain text before this match
            if (currentIndex < match.range.first) {
                val plainText = text.substring(currentIndex, match.range.first)
                if (plainText.isNotBlank()) {
                    segments.add(TextSegment.Plain(plainText))
                }
            }

            // Add tagged segment
            val content = match.content.trim()
            if (content.isNotEmpty()) {
                segments.add(when (match.type) {
                    TagType.ACTION -> TextSegment.Action(content)
                    TagType.THINK -> TextSegment.Think(content)
                    TagType.NARRATOR -> TextSegment.Narrator(content)
                })
            }

            currentIndex = match.range.last + 1
        }

        // Add remaining plain text
        if (currentIndex < text.length) {
            val plainText = text.substring(currentIndex)
            if (plainText.isNotBlank()) {
                segments.add(TextSegment.Plain(plainText))
            }
        }

        // If no segments were created, return the whole text as plain
        if (segments.isEmpty() && text.isNotBlank()) {
            return ParsedMessage(listOf(TextSegment.Plain(text)))
        }

        return ParsedMessage(segments)
    }

    private data class TagMatch(
        val range: IntRange,
        val type: TagType,
        val content: String
    )

    private enum class TagType {
        ACTION, THINK, NARRATOR
    }
}

