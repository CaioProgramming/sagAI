package com.ilustris.sagai.features.live.presentation

import com.ilustris.sagai.features.saga.chat.data.voicing.PerformanceLine
import com.ilustris.sagai.features.saga.chat.data.voicing.PerformanceScript

/**
 * When each script line plays, estimated from its share of the clip's spoken characters (cues in
 * square brackets don't count). No transcription call in live mode: close enough to highlight the
 * block being spoken and to switch the portrait to the narrator on narration.
 */
class CaptionTimeline private constructor(
    private val starts: List<Pair<Long, PerformanceLine>>,
) {
    fun lineAt(positionMs: Long): PerformanceLine? = starts.lastOrNull { it.first <= positionMs }?.second

    companion object {
        private val CUE_REGEX = Regex("\\[[^\\]]*]")

        fun from(
            script: PerformanceScript,
            durationMs: Long,
        ): CaptionTimeline {
            val weights = script.lines.map { it.text.replace(CUE_REGEX, "").trim().length.coerceAtLeast(1) }
            val total = weights.sum().coerceAtLeast(1)
            var elapsed = 0L
            val starts =
                script.lines.mapIndexed { index, line ->
                    val start = elapsed
                    elapsed += durationMs * weights[index] / total
                    start to line
                }
            return CaptionTimeline(starts)
        }
    }
}
