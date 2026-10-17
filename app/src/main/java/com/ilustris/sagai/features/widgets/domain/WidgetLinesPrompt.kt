package com.ilustris.sagai.features.widgets.domain

/** Everything the model is told about the character. All of it is already summarized in the database. */
data class LinesFacts(
    val type: LinesType,
    val characterName: String,
    val occupation: String,
    val personality: String,
    val backstory: String,
    val sagaTitle: String,
    val genreAesthetic: String,
    val arcs: List<String>,
    val events: List<String>,
    val relationships: List<String>,
    /** The scene's current objective. Only used by hooks, which call the player back to it. */
    val objective: String?,
    val count: Int,
    /** Lines the player already has, so a "generate more" does not repeat them. */
    val avoid: List<String>,
)

/**
 * Builds the prompt for a batch of widget lines.
 *
 * The chat is never sent: a finished saga can hold hundreds of messages per character, and the
 * profile, arcs, events and relationships already carry what the lines need, at a size that barely
 * changes from one saga to the next.
 */
object WidgetLinesPrompt {
    const val BLUEPRINT_KEY = "widget_lines"
    private const val BACKSTORY_LIMIT = 700
    private const val ITEM_LIMIT = 260
    private const val MAX_ARCS = 3
    private const val MAX_EVENTS = 5
    private const val MAX_RELATIONSHIPS = 4

    const val QUOTE_MAX_CHARS = 160
    const val HOOK_MAX_CHARS = 140

    fun maxChars(type: LinesType) = if (type == LinesType.QUOTE) QUOTE_MAX_CHARS else HOOK_MAX_CHARS

    fun build(facts: LinesFacts): String =
        buildString {
            appendLine("# CHARACTER")
            appendLine("Name: ${facts.characterName}")
            if (facts.occupation.isNotBlank()) appendLine("Occupation: ${facts.occupation}")
            if (facts.personality.isNotBlank()) appendLine("Personality: ${facts.personality}")
            if (facts.backstory.isNotBlank()) appendLine("Backstory: ${facts.backstory.take(BACKSTORY_LIMIT)}")
            appendLine()
            appendLine("# SAGA")
            appendLine("Title: ${facts.sagaTitle}")
            if (facts.genreAesthetic.isNotBlank()) appendLine("World: ${facts.genreAesthetic.take(BACKSTORY_LIMIT)}")
            section("HOW THE CHARACTER CHANGED", facts.arcs.takeLast(MAX_ARCS))
            section("WHAT HAPPENED TO THE CHARACTER", facts.events.takeLast(MAX_EVENTS))
            section("BONDS", facts.relationships.take(MAX_RELATIONSHIPS))
            if (facts.type == LinesType.HOOK && !facts.objective.isNullOrBlank()) {
                appendLine()
                appendLine("# WHERE THE STORY STANDS")
                appendLine("The player's current objective: ${facts.objective}")
            }
            if (facts.avoid.isNotEmpty()) {
                appendLine()
                appendLine("# ALREADY WRITTEN (do not repeat or paraphrase)")
                facts.avoid.forEach { appendLine("- $it") }
            }
            appendLine()
            appendLine("# TASK")
            appendLine(task(facts))
        }.trim()

    private fun StringBuilder.section(
        title: String,
        items: List<String>,
    ) {
        if (items.isEmpty()) return
        appendLine()
        appendLine("# $title")
        items.forEach { appendLine("- ${it.take(ITEM_LIMIT)}") }
    }

    private fun task(facts: LinesFacts): String =
        when (facts.type) {
            LinesType.QUOTE ->
                "Write ${facts.count} different short, poetic lines that ${facts.characterName} could say. " +
                    "Each one stands alone on a phone home screen, at most ${QUOTE_MAX_CHARS} characters."

            LinesType.HOOK ->
                "Write ${facts.count} different short messages in ${facts.characterName}'s voice, calling the player " +
                    "back to the story, like a message left for a friend. Each one is at most ${HOOK_MAX_CHARS} characters."
        }

    /** The system-side instructions, as the nested buckets the prompt service renders. */
    fun instructions(type: LinesType): Map<String, Any> =
        mapOf(
            "role" to
                "You write the short lines that appear on a player's phone home screen, " +
                "in the voice of a character from the story they played.",
            "directives" to
                mapOf(
                    "VOICE" to
                        "Speak as the character, in first person, with the register their personality and world imply. " +
                        "Poetic, never generic.",
                    "SPECIFIC" to
                        "Draw on the character's own history, wounds and bonds. A line that fits any character is a failed line.",
                    "NO_SPOILERS_OF_THE_CHAT" to
                        "Do not quote or retell specific conversations. Evoke what the character lived through instead.",
                    "FORMAT" to
                        "Plain text only. No quotation marks around the line, no numbering, no emojis, no hashtags, " +
                        "no stage directions. One sentence or two short ones.",
                    "SAFETY" to
                        "Nothing graphic, sexual or hateful: these lines sit on a home screen anyone may glance at.",
                    "KIND" to
                        if (type == LinesType.QUOTE) {
                            "Quotes: reflective, quotable, self-contained."
                        } else {
                            "Hooks: warm, a little mysterious, an invitation. Never guilt the player for being away."
                        },
                ),
        )
}

/** Cleans what the model returned: no blanks, no repeats, nothing too long for the widget. */
object WidgetLinesSanitizer {
    private val SPACES = Regex("\\s+")
    private val QUOTE_MARKS = charArrayOf('"', '“', '”', '\'', '‘', '’')
    private val LIST_PREFIX = Regex("^\\s*(?:[-*•]|\\d+[.)])\\s+")

    fun clean(
        raw: List<String>,
        type: LinesType,
        avoid: List<String> = emptyList(),
        limit: Int = WidgetLimits.BATCH_SIZE,
    ): List<String> {
        val maxChars = WidgetLinesPrompt.maxChars(type)
        val seen = avoid.map { it.normalizeForCompare() }.toMutableSet()
        val result = mutableListOf<String>()
        for (line in raw) {
            val cleaned = line.replace(LIST_PREFIX, "").replace(SPACES, " ").trim().trim(*QUOTE_MARKS).trim()
            if (cleaned.isBlank() || cleaned.length > maxChars) continue
            if (!seen.add(cleaned.normalizeForCompare())) continue
            result += cleaned
            if (result.size >= limit) break
        }
        return result
    }

    /** A character with almost nothing recorded yet gives thin lines; the creator warns about it. */
    fun hasLittleHistory(
        eventCount: Int,
        arcCount: Int,
    ): Boolean = eventCount + arcCount < 2

    private fun String.normalizeForCompare() = lowercase().filter { it.isLetterOrDigit() }
}
