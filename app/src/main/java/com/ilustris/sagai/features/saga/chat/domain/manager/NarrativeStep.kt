package com.ilustris.sagai.features.saga.chat.domain.manager

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

    data class StartChapter(
        val act: ActContent,
    ) : NarrativeStep()

    data class GenerateChapter(
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
    fun validateProgression(saga: SagaContent): NarrativeStep {
        val currentAct = saga.currentActInfo
        val currentChapter = currentAct?.currentChapterInfo
        val currentTimeline = currentChapter?.currentEventInfo

        return when {
            saga.isComplete() -> NarrativeStep.NoActionNeeded
            saga.isFull() -> NarrativeStep.GenerateSagaEnding(saga)
            currentAct == null -> NarrativeStep.StartAct
            currentAct.isFull() -> NarrativeStep.GenerateAct(currentAct)
            currentChapter == null -> NarrativeStep.StartChapter(currentAct)
            currentChapter.isFull() -> NarrativeStep.GenerateChapter(currentChapter)
            currentTimeline == null -> NarrativeStep.StartTimeline(currentChapter)
            currentTimeline.isComplete() -> NarrativeStep.EndTimeLine(currentChapter)
            currentTimeline.isFull() -> NarrativeStep.GenerateTimeLine(currentTimeline)
            else -> NarrativeStep.NoActionNeeded
        }
    }
}
