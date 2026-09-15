package com.ilustris.sagai.features.audiobook.data.usecase

import com.ilustris.sagai.features.audiobook.data.model.WordTiming
import java.text.Normalizer

/** A word as the transcription model heard it, with times relative to the clip start. */
data class SpokenWord(
    val text: String,
    val startMs: Long,
    val endMs: Long,
)

data class Alignment(
    val timings: List<WordTiming>,
    /** Share of written words matched to a spoken word, 0..1. */
    val score: Float,
)

/**
 * Turns clip timings into per-word highlights over the written text.
 *
 * The narration reads known text, so this aligns rather than transcribes: written and spoken words
 * are matched as sequences (tolerating small recognition differences), matched words take the
 * spoken times and unmatched runs are spread between their matched neighbours by character weight.
 * The score doubles as a divergence detector for narration that added or skipped words.
 */
object TimingAligner {
    private data class WrittenWord(
        val pageIndex: Int,
        val start: Int,
        val end: Int,
        val norm: String,
        val weight: Int,
    )

    private val token = Regex("\\S+")
    private val diacritics = Regex("\\p{Mn}+")
    private val nonWord = Regex("[^\\p{L}\\p{N}]+")

    fun align(
        pages: List<String>,
        segment: SegmentPlan,
        spoken: List<SpokenWord>,
        durationMs: Long,
    ): Alignment {
        val written = writtenWords(pages, segment)
        if (written.isEmpty()) return Alignment(emptyList(), 0f)
        val heard = spoken.map { it to normalize(it.text) }.filter { it.second.isNotEmpty() }
        if (heard.isEmpty()) return Alignment(estimate(written, 0, written.size, 0, durationMs), 0f)

        val matches = matchSequences(written.map { it.norm }, heard.map { it.second })
        val timings = arrayOfNulls<WordTiming>(written.size)
        matches.forEach { (w, h) ->
            val word = written[w]
            val (spokenWord, _) = heard[h]
            timings[w] = word.timing(spokenWord.startMs.coerceIn(0, durationMs), spokenWord.endMs.coerceIn(0, durationMs))
        }

        // Fill unmatched runs between matched neighbours, keeping times monotonic.
        var index = 0
        while (index < written.size) {
            if (timings[index] != null) {
                index++
                continue
            }
            val runStart = index
            while (index < written.size && timings[index] == null) index++
            val from = timings.getOrNull(runStart - 1)?.endMs ?: 0L
            val to = timings.getOrNull(index)?.startMs ?: durationMs
            estimate(written, runStart, index, from, maxOf(from, to)).forEachIndexed { offset, timing ->
                timings[runStart + offset] = timing
            }
        }

        return Alignment(
            timings = monotonic(timings.filterNotNull()),
            score = matches.size.toFloat() / written.size,
        )
    }

    /** Character-weighted timings with extra weight on punctuation pauses; the fallback when no transcription exists. */
    fun estimate(
        pages: List<String>,
        segment: SegmentPlan,
        durationMs: Long,
    ): List<WordTiming> {
        val written = writtenWords(pages, segment)
        return estimate(written, 0, written.size, 0, durationMs)
    }

    private fun estimate(
        written: List<WrittenWord>,
        from: Int,
        until: Int,
        startMs: Long,
        endMs: Long,
    ): List<WordTiming> {
        val run = written.subList(from, until)
        val total = run.sumOf { it.weight }.coerceAtLeast(1)
        val span = (endMs - startMs).coerceAtLeast(0)
        var consumed = 0
        return run.map { word ->
            val wordStart = startMs + span * consumed / total
            consumed += word.weight
            val wordEnd = startMs + span * consumed / total
            word.timing(wordStart, wordEnd)
        }
    }

    private fun writtenWords(
        pages: List<String>,
        segment: SegmentPlan,
    ): List<WrittenWord> =
        segment.spans.flatMap { span ->
            val page = pages[span.pageIndex]
            token.findAll(page.substring(span.start, span.end)).mapNotNull { match ->
                val raw = match.value
                val norm = normalize(raw)
                if (norm.isEmpty()) return@mapNotNull null
                WrittenWord(
                    pageIndex = span.pageIndex,
                    start = span.start + match.range.first,
                    end = span.start + match.range.last + 1,
                    norm = norm,
                    weight = norm.length + pauseWeight(raw.last()),
                )
            }.toList()
        }

    private fun pauseWeight(trailing: Char) =
        when (trailing) {
            '.', '!', '?', '…' -> 6
            ';', ':', '—', '–' -> 4
            ',' -> 3
            else -> 1
        }

    private fun WrittenWord.timing(
        startMs: Long,
        endMs: Long,
    ) = WordTiming(pageIndex, start, end, startMs, maxOf(startMs, endMs))

    private fun normalize(text: String): String =
        Normalizer
            .normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(diacritics, "")
            .replace(nonWord, "")

    private fun similar(
        a: String,
        b: String,
    ): Boolean {
        if (a == b) return true
        if (minOf(a.length, b.length) < 4) return false
        return levenshteinAtMostOne(a, b)
    }

    private fun levenshteinAtMostOne(
        a: String,
        b: String,
    ): Boolean {
        if (kotlin.math.abs(a.length - b.length) > 1) return false
        var i = 0
        var j = 0
        var edits = 0
        while (i < a.length && j < b.length) {
            if (a[i] == b[j]) {
                i++
                j++
                continue
            }
            if (++edits > 1) return false
            when {
                a.length > b.length -> i++
                a.length < b.length -> j++
                else -> {
                    i++
                    j++
                }
            }
        }
        return edits + (a.length - i) + (b.length - j) <= 1
    }

    /** Global sequence alignment (unit gap and substitution costs); returns matched index pairs in order. */
    private fun matchSequences(
        written: List<String>,
        heard: List<String>,
    ): List<Pair<Int, Int>> {
        val n = written.size
        val m = heard.size
        val cost = Array(n + 1) { IntArray(m + 1) }
        for (i in 0..n) cost[i][0] = i
        for (j in 0..m) cost[0][j] = j
        for (i in 1..n) {
            for (j in 1..m) {
                val diagonal = cost[i - 1][j - 1] + if (similar(written[i - 1], heard[j - 1])) 0 else 1
                cost[i][j] = minOf(diagonal, cost[i - 1][j] + 1, cost[i][j - 1] + 1)
            }
        }

        val pairs = ArrayDeque<Pair<Int, Int>>()
        var i = n
        var j = m
        while (i > 0 && j > 0) {
            val same = similar(written[i - 1], heard[j - 1])
            when {
                same && cost[i][j] == cost[i - 1][j - 1] -> {
                    pairs.addFirst(i - 1 to j - 1)
                    i--
                    j--
                }

                cost[i][j] == cost[i - 1][j - 1] + 1 -> {
                    i--
                    j--
                }

                cost[i][j] == cost[i - 1][j] + 1 -> {
                    i--
                }

                else -> {
                    j--
                }
            }
        }
        return pairs.toList()
    }

    private fun monotonic(timings: List<WordTiming>): List<WordTiming> {
        var floor = 0L
        return timings.map { timing ->
            val start = maxOf(timing.startMs, floor)
            val end = maxOf(timing.endMs, start)
            floor = start
            timing.copy(startMs = start, endMs = end)
        }
    }
}
