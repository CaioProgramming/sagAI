package com.ilustris.sagai.features.audiobook.data.usecase

/** A contiguous slice of one page's content. Offsets are relative to that page. */
data class TextSpan(
    val pageIndex: Int,
    val start: Int,
    val end: Int,
)

/** What a single TTS call narrates: whole paragraphs packed up to the configured size. */
data class SegmentPlan(
    val index: Int,
    val spans: List<TextSpan>,
    val text: String,
) {
    val startPageIndex get() = spans.first().pageIndex
    val startChar get() = spans.first().start
    val endPageIndex get() = spans.last().pageIndex
    val endChar get() = spans.last().end

    /** Whitespace-insensitive, so a plan rebuilt from a stored range hashes like the original. */
    val textHash get() = text.split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ").hashCode()
}

/**
 * Splits a section into narration segments on our side, before any API call, so every clip covers
 * text known in advance: nothing is truncated by the model and each clip stays far from the TTS
 * output limit. Paragraphs are packed greedily (across pages too) up to [maxChars]; a paragraph
 * longer than that alone falls back to sentences, and a sentence longer than that to words.
 */
object BookAudioSegmenter {
    private const val SEPARATOR = "\n\n"
    private val paragraphBreak = Regex("\\n\\s*\\n|\\n")
    private val sentenceEnd = Regex("(?<=[.!?…])\\s+")
    private val whitespace = Regex("\\s+")

    /** Rebuilds the plan of an already narrated range; words only live inside spans, so gaps don't matter. */
    fun between(
        pages: List<String>,
        index: Int,
        startPageIndex: Int,
        startChar: Int,
        endPageIndex: Int,
        endChar: Int,
    ): SegmentPlan {
        val spans =
            (startPageIndex..endPageIndex).map { pageIndex ->
                val page = pages[pageIndex]
                TextSpan(
                    pageIndex = pageIndex,
                    start = if (pageIndex == startPageIndex) startChar.coerceIn(0, page.length) else 0,
                    end = if (pageIndex == endPageIndex) endChar.coerceIn(0, page.length) else page.length,
                )
            }
        return SegmentPlan(index, spans, spans.joinToString(SEPARATOR) { pages[it.pageIndex].substring(it.start, it.end) })
    }

    fun plan(
        pages: List<String>,
        maxChars: Int,
    ): List<SegmentPlan> {
        require(maxChars > 0) { "maxChars must be positive" }
        val units = pages.flatMapIndexed { pageIndex, page -> paragraphs(pageIndex, page, maxChars) }

        val segments = mutableListOf<SegmentPlan>()
        var current = mutableListOf<TextSpan>()
        var currentLength = 0

        fun flush() {
            if (current.isEmpty()) return
            segments +=
                SegmentPlan(
                    index = segments.size,
                    spans = current,
                    text = current.joinToString(SEPARATOR) { pages[it.pageIndex].substring(it.start, it.end) },
                )
            current = mutableListOf()
            currentLength = 0
        }

        units.forEach { unit ->
            val length = unit.end - unit.start
            val added = if (current.isEmpty()) length else length + SEPARATOR.length
            if (current.isNotEmpty() && currentLength + added > maxChars) flush()
            currentLength += if (current.isEmpty()) length else length + SEPARATOR.length
            current += unit
        }
        flush()
        return segments
    }

    private fun paragraphs(
        pageIndex: Int,
        page: String,
        maxChars: Int,
    ): List<TextSpan> =
        split(page, 0, page.length, paragraphBreak).flatMap { (start, end) ->
            if (end - start <= maxChars) {
                listOf(start to end)
            } else {
                split(page, start, end, sentenceEnd).flatMap { (sStart, sEnd) ->
                    if (sEnd - sStart <= maxChars) listOf(sStart to sEnd) else packWords(page, sStart, sEnd, maxChars)
                }.let { pack(it, maxChars) }
            }
        }.map { (start, end) -> TextSpan(pageIndex, start, end) }

    /** Trimmed, non-blank ranges of [text] between [delimiter] matches inside [start]..[end]. */
    private fun split(
        text: String,
        start: Int,
        end: Int,
        delimiter: Regex,
    ): List<Pair<Int, Int>> {
        val ranges = mutableListOf<Pair<Int, Int>>()
        var cursor = start
        delimiter.findAll(text.substring(start, end)).forEach { match ->
            ranges += trimmed(text, cursor, start + match.range.first)
            cursor = start + match.range.last + 1
        }
        ranges += trimmed(text, cursor, end)
        return ranges.filter { it.second > it.first }
    }

    private fun trimmed(
        text: String,
        start: Int,
        end: Int,
    ): Pair<Int, Int> {
        var s = start
        var e = end
        while (s < e && text[s].isWhitespace()) s++
        while (e > s && text[e - 1].isWhitespace()) e--
        return s to e
    }

    /** Re-joins adjacent sentence ranges that still fit, so long paragraphs don't become one clip per sentence. */
    private fun pack(
        ranges: List<Pair<Int, Int>>,
        maxChars: Int,
    ): List<Pair<Int, Int>> {
        val packed = mutableListOf<Pair<Int, Int>>()
        ranges.forEach { range ->
            val last = packed.lastOrNull()
            if (last != null && range.second - last.first <= maxChars) {
                packed[packed.lastIndex] = last.first to range.second
            } else {
                packed += range
            }
        }
        return packed
    }

    private fun packWords(
        text: String,
        start: Int,
        end: Int,
        maxChars: Int,
    ): List<Pair<Int, Int>> = pack(split(text, start, end, whitespace), maxChars)
}
