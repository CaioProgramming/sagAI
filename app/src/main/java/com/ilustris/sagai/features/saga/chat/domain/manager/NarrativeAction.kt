package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

/** Thrown by [NarrativeAction.CreateTimeline]'s executor when a timeline is already active for
 * the chapter — the reactive progression triggers (milestone dismissal, loading state, explicit
 * continue) can race each other and resolve the same automatic action more than once before
 * [SagaContentManager]'s cached saga snapshot catches up with the first one's write. Matched by
 * name (not type) since [NarrativeExecutionResult.Failure] only carries a message string. */
const val TIMELINE_ALREADY_ACTIVE_MESSAGE = "Timeline already set at this chapter"

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
