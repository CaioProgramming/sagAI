package com.ilustris.sagai.features.timeline.data.model

/** One line, copied verbatim from the real chat — never paraphrased. */
data class SimpleMessage(
    val speaker: String,
    val line: String,
)

/**
 * A real back-and-forth captured at event-synthesis time, when the chat is still on hand — so
 * later writers (the book, mainly) can quote what actually happened instead of inventing dialogue.
 * [respondent] is null when the line landed without a reply worth keeping.
 */
data class NotableExchange(
    val sender: SimpleMessage,
    val respondent: SimpleMessage? = null,
)
