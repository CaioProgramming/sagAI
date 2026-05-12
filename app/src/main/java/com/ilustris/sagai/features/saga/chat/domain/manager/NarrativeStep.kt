package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.home.data.model.currentActInfo
import com.ilustris.sagai.features.home.data.model.currentChapterInfo
import com.ilustris.sagai.features.home.data.model.currentEventInfo

sealed class NarrativeStep {
    data class GenerateSagaEnding(
        val saga: SagaMetadata,
    ) : NarrativeStep()

    data object StartAct : NarrativeStep()

    data class GenerateAct(
        val act: ActMetadata,
    ) : NarrativeStep()

    data class GenerateActIntroduction(
        val act: ActMetadata,
    ) : NarrativeStep()

    data class StartChapter(
        val act: ActMetadata,
    ) : NarrativeStep()

    data class GenerateChapter(
        val chapter: ChapterMetadata,
    ) : NarrativeStep()

    data class GenerateChapterIntroduction(
        val chapter: ChapterMetadata,
    ) : NarrativeStep()

    data class StartTimeline(
        val chapter: ChapterMetadata,
    ) : NarrativeStep()

    data class GenerateTimeLine(
        val timeline: TimelineMetadata,
    ) : NarrativeStep()

    data class EndTimeLine(
        val currentChapter: ChapterMetadata,
    ) : NarrativeStep()

    data object NoActionNeeded : NarrativeStep()
}

object NarrativeCheck {
    fun validateProgression(
        saga: SagaMetadata,
        rules: NarrativeRules,
    ): NarrativeStep {
        val currentAct = saga.currentActInfo
        val currentChapter = saga.currentChapterInfo
        val currentTimeline = saga.currentEventInfo

        return when {
            saga.isComplete(rules) -> {
                NarrativeStep.NoActionNeeded
            }

            saga.isFull(rules) -> {
                NarrativeStep.GenerateSagaEnding(saga)
            }

            currentAct == null || currentAct.isComplete(rules) -> {
                NarrativeStep.StartAct
            }

            currentAct.isFull(rules) -> {
                NarrativeStep.GenerateAct(currentAct)
            }

            currentAct.data.introduction.isBlank() -> {
                NarrativeStep.GenerateActIntroduction(currentAct)
            }

            currentChapter == null || currentChapter.isComplete(rules) -> {
                NarrativeStep.StartChapter(
                    currentAct,
                )
            }

            currentChapter.isFull(rules) -> {
                NarrativeStep.GenerateChapter(currentChapter)
            }

            currentChapter.data.introduction.isBlank() -> {
                NarrativeStep.GenerateChapterIntroduction(currentChapter)
            }

            currentTimeline == null -> {
                NarrativeStep.StartTimeline(currentChapter)
            }

            currentTimeline.isComplete(rules) -> {
                NarrativeStep.EndTimeLine(currentChapter)
            }

            currentTimeline.isFull(rules.loreUpdateLimit) -> {
                NarrativeStep.GenerateTimeLine(currentTimeline)
            }

            else -> {
                NarrativeStep.NoActionNeeded
            }
        }
    }
}
