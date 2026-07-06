package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

sealed class NarrativeAction {
    data object CreateAct : NarrativeAction()

    data class GenerateActIntro(
        val act: ActContent,
    ) : NarrativeAction()

    data class CreateChapter(
        val act: ActContent,
    ) : NarrativeAction()

    data class GenerateChapter(
        val chapter: ChapterContent,
    ) : NarrativeAction()

    data class GenerateChapterIntro(
        val chapter: ChapterContent,
    ) : NarrativeAction()

    data class CreateTimeline(
        val chapter: ChapterContent,
    ) : NarrativeAction()

    data class EvolveTimeline(
        val timeline: TimelineContent,
    ) : NarrativeAction()

    data class CloseTimeline(
        val chapter: ChapterContent,
    ) : NarrativeAction()

    data class GenerateAct(
        val act: ActContent,
    ) : NarrativeAction()

    data class GenerateEnding(
        val saga: SagaContent,
    ) : NarrativeAction()
}

enum class NarrativeExecutionMode {
    UserTriggered,
    Automatic,
}
