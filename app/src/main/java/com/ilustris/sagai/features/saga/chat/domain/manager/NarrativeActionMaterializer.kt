package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.home.data.model.findChapter
import com.ilustris.sagai.features.home.data.model.findTimeline

/**
 * Hydrates payloads for execution/UI from identifiers chosen using [SagaMetadata] semantics.
 */
object NarrativeActionMaterializer {
    fun materialize(
        intent: NarrativeProgressIntent,
        saga: SagaContent,
    ): NarrativeAction? =
        when (intent) {
            NarrativeProgressIntent.CreateAct -> {
                NarrativeAction.CreateAct
            }

            NarrativeProgressIntent.GenerateEnding -> {
                NarrativeAction.GenerateEnding(saga)
            }

            is NarrativeProgressIntent.GenerateActIntro -> {
                saga.findAct(intent.actId)?.let { NarrativeAction.GenerateActIntro(it) }
            }

            is NarrativeProgressIntent.CreateChapter -> {
                saga.findAct(intent.actId)?.let { NarrativeAction.CreateChapter(it) }
            }

            is NarrativeProgressIntent.GenerateChapter -> {
                saga.findChapter(intent.chapterId)?.let { NarrativeAction.GenerateChapter(it) }
            }

            is NarrativeProgressIntent.GenerateChapterIntro -> {
                saga.findChapter(intent.chapterId)?.let { NarrativeAction.GenerateChapterIntro(it) }
            }

            is NarrativeProgressIntent.CreateTimeline -> {
                saga.findChapter(intent.chapterId)?.let { NarrativeAction.CreateTimeline(it) }
            }

            is NarrativeProgressIntent.CloseTimeline -> {
                saga.findChapter(intent.chapterId)?.let { NarrativeAction.CloseTimeline(it) }
            }

            is NarrativeProgressIntent.EvolveTimeline -> {
                saga.findTimeline(intent.timelineId)?.let { NarrativeAction.EvolveTimeline(it) }
            }

            is NarrativeProgressIntent.GenerateAct -> {
                saga.findAct(intent.actId)?.let { NarrativeAction.GenerateAct(it) }
            }
        }
}
