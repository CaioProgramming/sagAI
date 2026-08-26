package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import com.ilustris.sagai.ui.theme.RichTextParser
import com.ilustris.sagai.ui.theme.TextSegment

/**
 * Breaks a long message into a handful of consecutive bubbles, the way messaging apps do, so a wall
 * of AI text arrives as something readable instead of one enormous block.
 *
 * The split is purely visual — one `Message` row is still one item in the chat list, keyed by its
 * id, so selection, reactions, long-press and the timestamp all keep working off a single message.
 *
 * Splitting happens on segment boundaries first ([RichTextParser] segments are atomic: cutting
 * inside an `<action>`/`<think>`/`<narrator>` tag would destroy its meaning), and only falls back to
 * cutting inside a plain segment when that segment alone is too tall. Even then the cut lands on a
 * sentence end when one is nearby, and on a word boundary otherwise — never mid-word.
 */
object MessageBlockSplitter {
    /** Lines a single bubble may hold before the message gets broken up. */
    const val DEFAULT_MAX_LINES = 5

    /**
     * Upper bound on bubbles per message. Without it a very long reply would reveal as a dozen
     * bubbles typing one after another, which reads as slow rather than natural — past this point
     * the remainder stays in the final bubble.
     */
    private const val MAX_BLOCKS = 6

    /**
     * A sentence break is only worth taking if it doesn't leave the bubble mostly empty. Below this
     * fraction of the available height we cut on a word boundary instead.
     */
    private const val MIN_SENTENCE_BREAK_RATIO = 0.4f

    private val SENTENCE_END_CHARS = charArrayOf('.', '!', '?', '…')

    fun split(
        text: String,
        measurer: TextMeasurer,
        style: TextStyle,
        maxWidthPx: Int,
        maxLines: Int = DEFAULT_MAX_LINES,
    ): List<String> {
        if (text.isBlank() || maxWidthPx <= 0 || maxLines <= 0) return listOf(text)

        val lineCount = { value: String -> measure(value, measurer, style, maxWidthPx).lineCount }

        // Fast path: short messages — the overwhelming majority — never touch the parser.
        if (lineCount(text) <= maxLines) return listOf(text)

        val segments = RichTextParser.parse(text).segments
        if (segments.isEmpty()) return listOf(text)

        val blocks = mutableListOf<String>()
        val current = mutableListOf<TextSegment>()

        fun flush() {
            if (current.isEmpty()) return
            blocks += current.joinToString(" ") { it.serialize() }.trim()
            current.clear()
        }

        for (segment in segments) {
            val candidate = (current + segment).joinToString(" ") { it.visibleText() }
            if (current.isNotEmpty() && lineCount(candidate) > maxLines) {
                flush()
            }

            if (current.isEmpty() && segment is TextSegment.Plain) {
                val chunks = splitPlainText(segment.text, measurer, style, maxWidthPx, maxLines)
                // Everything but the tail becomes its own bubble; the tail stays open so a short
                // following segment can still join it.
                chunks.dropLast(1).forEach { blocks += it.trim() }
                chunks.lastOrNull()?.let { current += TextSegment.Plain(it) }
            } else {
                current += segment
            }
        }
        flush()

        val nonEmpty = blocks.filter { it.isNotBlank() }
        if (nonEmpty.isEmpty()) return listOf(text)
        if (nonEmpty.size <= MAX_BLOCKS) return nonEmpty

        return nonEmpty.take(MAX_BLOCKS - 1) +
            nonEmpty.drop(MAX_BLOCKS - 1).joinToString(" ")
    }

    private fun splitPlainText(
        text: String,
        measurer: TextMeasurer,
        style: TextStyle,
        maxWidthPx: Int,
        maxLines: Int,
    ): List<String> {
        val chunks = mutableListOf<String>()
        var rest = text.trim()

        while (rest.isNotEmpty()) {
            val layout = measure(rest, measurer, style, maxWidthPx)
            if (layout.lineCount <= maxLines) {
                chunks += rest
                break
            }

            val hardEnd = layout.getLineEnd(maxLines - 1, visibleEnd = true)
            if (hardEnd <= 0 || hardEnd >= rest.length) {
                chunks += rest
                break
            }

            val cut =
                sentenceBreakBefore(rest, hardEnd)
                    ?: wordBreakBefore(rest, hardEnd)
                    ?: hardEnd

            chunks += rest.substring(0, cut).trim()
            rest = rest.substring(cut).trimStart()
        }

        return chunks.filter { it.isNotBlank() }
    }

    /** Index just past the last sentence-ending punctuation before [limit], if it isn't too early. */
    private fun sentenceBreakBefore(
        text: String,
        limit: Int,
    ): Int? {
        val floor = (limit * MIN_SENTENCE_BREAK_RATIO).toInt()
        for (i in limit - 1 downTo floor) {
            if (text[i] !in SENTENCE_END_CHARS) continue
            // Must actually end a sentence — "3.5" or an ellipsis mid-word shouldn't split.
            val next = text.getOrNull(i + 1)
            if (next == null || next.isWhitespace()) return i + 1
        }
        return null
    }

    private fun wordBreakBefore(
        text: String,
        limit: Int,
    ): Int? {
        for (i in limit - 1 downTo 1) {
            if (text[i].isWhitespace()) return i
        }
        return null
    }

    private fun measure(
        text: String,
        measurer: TextMeasurer,
        style: TextStyle,
        maxWidthPx: Int,
    ) = measurer.measure(
        text = text,
        style = style,
        constraints = Constraints(maxWidth = maxWidthPx),
    )

    /** What the reader actually sees — tags themselves take up no space. */
    private fun TextSegment.visibleText() =
        when (this) {
            is TextSegment.Plain -> text
            is TextSegment.Action -> text
            is TextSegment.Think -> text
            is TextSegment.Narrator -> text
        }

    /** Round-trips back into tagged text so each block still renders through [ExpressiveText]. */
    private fun TextSegment.serialize() =
        when (this) {
            is TextSegment.Plain -> text
            is TextSegment.Action -> "<action>$text</action>"
            is TextSegment.Think -> "<think>$text</think>"
            is TextSegment.Narrator -> "<narrator>$text</narrator>"
        }
}

/**
 * Memoized [MessageBlockSplitter.split]. Measuring is not free, so it only re-runs when the text,
 * the style or the available width actually change.
 *
 * @param enabled pass false to force a single block (audio messages, for instance, can't be split).
 */
@Composable
fun rememberMessageBlocks(
    text: String,
    style: TextStyle,
    maxWidthPx: Int,
    maxLines: Int = MessageBlockSplitter.DEFAULT_MAX_LINES,
    enabled: Boolean = true,
): List<String> {
    val measurer = rememberTextMeasurer()
    return remember(text, style, maxWidthPx, maxLines, enabled) {
        if (!enabled) listOf(text) else MessageBlockSplitter.split(text, measurer, style, maxWidthPx, maxLines)
    }
}
