package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The split between what runs on its own and what waits for the reader.
 *
 * This asserted that everything waits, which stopped being true when CreateTimeline was made
 * automatic: it is a pure local write that inherits the chapter's scene summary and calls no
 * model, so pausing the story to ask permission for it was interrupting nothing. The test kept
 * asserting the older world and had been failing unnoticed, because a sibling file that did not
 * compile was keeping the whole suite from running.
 *
 * Written as "only this one is automatic" rather than as a list of expectations, so a new action
 * that silently runs itself fails here instead of surprising a reader mid-scene.
 */
class NarrativeActionExecutionModeTest {
    private val act = ActContent(data = Act(id = 1, sagaId = 1))
    private val chapter = ChapterContent(data = Chapter(id = 1, actId = 1))
    private val timeline = TimelineContent(data = Timeline(id = 1, chapterId = 1))
    private val saga = SagaContent(data = Saga(id = 1))

    private val everyAction =
        listOf(
            NarrativeAction.CreateAct,
            NarrativeAction.GenerateActIntro(act),
            NarrativeAction.CreateChapter(act),
            NarrativeAction.GenerateChapterIntro(chapter),
            NarrativeAction.CreateTimeline(chapter),
            NarrativeAction.CloseTimeline(chapter),
            NarrativeAction.EvolveTimeline(timeline),
            NarrativeAction.GenerateChapter(chapter),
            NarrativeAction.GenerateAct(act),
            NarrativeAction.GenerateEnding(saga),
        )

    @Test
    fun `only creating the next timeline runs without asking`() {
        val automatic =
            everyAction
                .filter { it.executionMode() == NarrativeExecutionMode.Automatic }
                .map { it::class.simpleName }

        assertEquals(listOf("CreateTimeline"), automatic)
    }

    @Test
    fun `everything that calls a model waits for the reader`() {
        everyAction
            .filterNot { it is NarrativeAction.CreateTimeline }
            .forEach { action ->
                assertEquals(
                    "${action::class.simpleName} should wait for the reader",
                    NarrativeExecutionMode.UserTriggered,
                    action.executionMode(),
                )
            }
    }
}
