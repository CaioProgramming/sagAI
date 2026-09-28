package com.ilustris.sagai.features.live.presentation

import com.ilustris.sagai.features.saga.chat.data.voicing.PerformanceLine
import com.ilustris.sagai.features.saga.chat.data.voicing.PerformanceScript

/**
 * When each script line plays, estimated from its share of the clip's spoken characters (cues in
 * square brackets don't count). No transcription call in live mode: close enough to highlight the
 * block being spoken and to switch the portrait to the narrator on narration.
 */
class CaptionTimeline private constructor(
    private val spans: List<Span>,
) {
    private class Span(
        val start: Long,
        val end: Long,
        val line: PerformanceLine,
    )

    fun lineAt(positionMs: Long): PerformanceLine? = spanAt(positionMs)?.line

    /** How far into [lineAt]'s line [positionMs] is, 0..1 — drives the word-by-word caption fill. */
    fun progressAt(positionMs: Long): Float {
        val span = spanAt(positionMs) ?: return 0f
        val length = (span.end - span.start).coerceAtLeast(1)
        return ((positionMs - span.start).toFloat() / length).coerceIn(0f, 1f)
    }

    private fun spanAt(positionMs: Long): Span? = spans.lastOrNull { it.start <= positionMs }

    companion object {
        private val CUE_REGEX = Regex("\\[[^\\]]*]")

        fun from(
            script: PerformanceScript,
            durationMs: Long,
        ): CaptionTimeline {
            val weights =
                script.lines.map {
                    it.text
                        .replace(CUE_REGEX, "")
                        .trim()
                        .length
                        .coerceAtLeast(1)
                }
            val total = weights.sum().coerceAtLeast(1)
            var elapsed = 0L
            val spans =
                script.lines.mapIndexed { index, line ->
                    val start = elapsed
                    elapsed += durationMs * weights[index] / total
                    Span(start, elapsed, line)
                }
            return CaptionTimeline(spans)
        }
    }
}
