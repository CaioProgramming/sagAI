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

/** Thrown by [NarrativeAction.CreateChapter]'s executor when the act's last chapter is already
 * active and mid-play — same self-heal shape as [TIMELINE_ALREADY_ACTIVE_MESSAGE], just one level
 * up: `CreateChapter` gets (re)proposed while a chapter already exists for this act, the executor
 * repoints `currentChapterId` at it, and this reports that as a no-op rather than a real failure. */
const val CHAPTER_ALREADY_SET_MESSAGE = "Chapter is already set at this act"

/** Same shape one level further up, for [NarrativeAction.CreateAct]. */
const val ACT_ALREADY_SET_MESSAGE = "Act is already set at this saga"

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

/**
 * What an action acts on, independent of the content snapshot embedded in it — two actions with the
 * same key are the same decision, even if one carries a staler copy of its chapter/act/timeline.
 */
fun NarrativeAction.targetKey(): String =
    when (this) {
        NarrativeAction.CreateAct -> "CreateAct"
        is NarrativeAction.GenerateActIntro -> "GenerateActIntro:${act.data.id}"
        is NarrativeAction.CreateChapter -> "CreateChapter:${act.data.id}"
        is NarrativeAction.GenerateChapter -> "GenerateChapter:${chapter.data.id}"
        is NarrativeAction.GenerateChapterIntro -> "GenerateChapterIntro:${chapter.data.id}"
        is NarrativeAction.CreateTimeline -> "CreateTimeline:${chapter.data.id}"
        is NarrativeAction.EvolveTimeline -> "EvolveTimeline:${timeline.data.id}"
        is NarrativeAction.CloseTimeline -> "CloseTimeline:${chapter.data.id}"
        is NarrativeAction.GenerateAct -> "GenerateAct:${act.data.id}"
        is NarrativeAction.GenerateEnding -> "GenerateEnding:${saga.data.id}"
    }

enum class NarrativeExecutionMode {
    UserTriggered,
    Automatic,
}
