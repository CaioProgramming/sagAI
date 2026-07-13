package com.ilustris.sagai.features.home.data.model

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InheritSceneSummaryTest {
    @Test
    fun `inherits from last event in current chapter`() {
        val chapterOneSummary =
            sceneSummary(
                location = "Tavern",
                objective = "Find the map",
            )
        val chapter =
            chapterContent(
                id = 10,
                events =
                    listOf(
                        timelineWithSummary(id = 100, chapterId = 10, summary = chapterOneSummary),
                    ),
            )
        val saga = sagaWithChapters(listOf(chapter))

        assertEquals(chapterOneSummary, saga.inheritSceneSummaryForChapter(chapter))
    }

    @Test
    fun `inherits from previous chapter when current chapter has no events`() {
        val previousSummary =
            sceneSummary(
                location = "Forest edge",
                objective = "Cross the river",
            )
        val previousChapter =
            chapterContent(
                id = 10,
                events =
                    listOf(
                        timelineWithSummary(id = 100, chapterId = 10, summary = previousSummary),
                    ),
            )
        val newChapter = chapterContent(id = 11, events = emptyList())
        val saga = sagaWithChapters(listOf(previousChapter, newChapter))

        assertEquals(previousSummary, saga.inheritSceneSummaryForChapter(newChapter))
    }

    @Test
    fun `returns null when no active summary exists`() {
        val chapter = chapterContent(id = 10, events = emptyList())
        val saga = sagaWithChapters(listOf(chapter))

        assertNull(saga.inheritSceneSummaryForChapter(chapter))
    }

    @Test
    fun `skips inactive summaries`() {
        val inactiveSummary =
            SceneSummary(
                currentLocation = "",
                charactersPresent = emptyList(),
                immediateObjective = null,
                currentConflict = null,
                mood = null,
                currentTimeOfDay = null,
            )
        val previousChapter =
            chapterContent(
                id = 10,
                events =
                    listOf(
                        timelineWithSummary(id = 100, chapterId = 10, summary = inactiveSummary),
                    ),
            )
        val newChapter = chapterContent(id = 11, events = emptyList())
        val saga = sagaWithChapters(listOf(previousChapter, newChapter))

        assertNull(saga.inheritSceneSummaryForChapter(newChapter))
    }

    private fun sagaWithChapters(chapters: List<ChapterContent>): SagaContent {
        val act =
            ActContent(
                data = Act(id = 1, sagaId = 1, currentChapterId = chapters.last().data.id),
                chapters = chapters,
                currentChapterInfo = chapters.last(),
            )
        return SagaContent(
            data = Saga(id = 1, title = "Test Saga", currentActId = 1),
            currentActInfo = act,
            acts = listOf(act),
        )
    }

    private fun chapterContent(
        id: Int,
        events: List<TimelineContent>,
    ) = ChapterContent(
        data = Chapter(id = id, actId = 1),
        events = events,
    )

    private fun timelineWithSummary(
        id: Int,
        chapterId: Int,
        summary: SceneSummary,
    ) = TimelineContent(
        data =
            Timeline(
                id = id,
                chapterId = chapterId,
                sceneSummary = summary,
            ),
    )

    private fun sceneSummary(
        location: String,
        objective: String,
    ) = SceneSummary(
        currentLocation = location,
        charactersPresent = emptyList(),
        immediateObjective = objective,
        currentConflict = null,
        mood = null,
        currentTimeOfDay = null,
    )
}
