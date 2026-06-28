package com.ilustris.sagai.features.narrative.domain

import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import com.ilustris.sagai.features.narrative.data.model.mergeSceneSummaries
import com.ilustris.sagai.features.narrative.data.model.mergeWith
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary

/** Deterministic rollup from all timeline scene summaries in a chapter. */
fun ChapterContent.rollupContinuity(): ContinuitySummary =
    events
        .mapNotNull { it.data.sceneSummary }
        .mergeSceneSummaries()

fun ChapterContent.effectiveContinuity(): ContinuitySummary? {
    val stored = data.continuitySummary
    val rollup = rollupContinuity()
    return when {
        stored != null && !stored.isBlank() -> stored.mergeWith(rollup)
        rollup.isBlank() -> null
        else -> rollup
    }
}
