package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

sealed class NarrativeStep {
    data class GenerateSagaEnding(
        val saga: SagaContent,
    ) : NarrativeStep()

    data object StartAct : NarrativeStep()

    data class GenerateAct(
        val act: ActContent,
    ) : NarrativeStep()

    data class GenerateActIntroduction(
        val act: ActContent,
    ) : NarrativeStep()

    data class StartChapter(
        val act: ActContent,
    ) : NarrativeStep()

    data class GenerateChapter(
        val chapter: ChapterContent,
    ) : NarrativeStep()

    data class GenerateChapterIntroduction(
        val chapter: ChapterContent,
    ) : NarrativeStep()

    data class StartTimeline(
        val chapter: ChapterContent,
    ) : NarrativeStep()

    data class GenerateTimeLine(
        val timeline: TimelineContent,
    ) : NarrativeStep()

    data class EndTimeLine(
        val currentChapterContent: ChapterContent,
    ) : NarrativeStep()

    data object NoActionNeeded : NarrativeStep()
}

object NarrativeCheck {
    fun validateProgression(
        saga: SagaContent,
        rules: NarrativeRules,
    ): NarrativeStep {
        val currentAct = saga.currentActInfo
        val currentChapter = currentAct?.currentChapterInfo
        val currentTimeline = currentChapter?.currentEventInfo

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

            currentAct.isFull(rules.actUpdateLimit, rules) -> {
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

            currentChapter.isFull(rules.chapterUpdateLimit, rules) -> {
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
