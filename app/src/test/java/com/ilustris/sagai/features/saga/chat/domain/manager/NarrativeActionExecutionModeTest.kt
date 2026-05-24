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

class NarrativeActionExecutionModeTest {
    private val act = ActContent(data = Act(id = 1, sagaId = 1))
    private val chapter = ChapterContent(data = Chapter(id = 1, actId = 1))
    private val timeline = TimelineContent(data = Timeline(id = 1, chapterId = 1))
    private val saga = SagaContent(data = Saga(id = 1))

    @Test
    fun `all narrative actions require user advance on the trigger`() {
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.CreateAct.executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.GenerateActIntro(act).executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.CreateChapter(act).executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.GenerateChapterIntro(chapter).executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.CreateTimeline(chapter).executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.EnsureTimelineSceneSummary(timeline).executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.CloseTimeline(chapter).executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.EvolveTimeline(timeline).executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.GenerateChapter(chapter).executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.GenerateAct(act).executionMode())
        assertEquals(NarrativeExecutionMode.UserTriggered, NarrativeAction.GenerateEnding(saga).executionMode())
    }
}
