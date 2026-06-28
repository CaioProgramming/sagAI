package com.ilustris.sagai.features.narrative.domain

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuityContextBuilderTest {
    private val rules =
        NarrativeRules(
            continuityRecentChapters = 3,
            continuityDistantFactsLimit = 20,
        )

    @Test
    fun `chapter rollup aggregates timeline scene summaries`() {
        val chapter =
            chapterContent(
                id = 1,
                events =
                    listOf(
                        timelineWithSummary(
                            id = 10,
                            chapterId = 1,
                            establishedFacts = listOf("Secret door discovered"),
                        ),
                        timelineWithSummary(
                            id = 11,
                            chapterId = 1,
                            establishedFacts = listOf("Secret door discovered", "Key was taken"),
                        ),
                    ),
            )

        val rollup = chapter.rollupContinuity()

        assertEquals(2, rollup.establishedFacts.size)
        assertTrue(rollup.establishedFacts.contains("Secret door discovered"))
        assertTrue(rollup.establishedFacts.contains("Key was taken"))
    }

    @Test
    fun `distant canon includes chapter one persistent setup when on chapter eight`() {
        val chapters =
            (1..8).map { index ->
                val continuity =
                    if (index == 1) {
                        ContinuitySummary(
                            persistentSetups = listOf("A cursed coin was left in the cellar"),
                            establishedFacts = listOf("Hero arrived in town"),
                        )
                    } else {
                        ContinuitySummary(
                            establishedFacts = listOf("Chapter $index completed"),
                        )
                    }
                chapterContent(
                    id = index,
                    title = "Chapter $index",
                    continuitySummary = continuity,
                )
            }

        val saga =
            sagaWithChapters(
                currentChapterId = 8,
                chapters = chapters,
            )

        val context = saga.buildChatContinuityContext(rules)
        val distantFacts =
            context.distantCanon?.persistentSetups.orEmpty() +
                context.distantCanon?.establishedFacts.orEmpty()

        assertTrue(
            "Expected chapter 1 persistent setup in distant canon",
            distantFacts.any { it.contains("cursed coin") },
        )
    }

    @Test
    fun `recent chapter canon includes last three completed chapters`() {
        val chapters =
            (1..6).map { index ->
                chapterContent(
                    id = index,
                    title = "Chapter $index",
                    continuitySummary =
                        ContinuitySummary(
                            establishedFacts = listOf("Milestone from chapter $index"),
                        ),
                )
            }

        val saga =
            sagaWithChapters(
                currentChapterId = 6,
                chapters = chapters,
            )

        val context = saga.buildChatContinuityContext(rules)

        assertEquals(3, context.recentChapterCanon.size)
        assertTrue(
            context.recentChapterCanon.any { it.summary.establishedFacts.contains("Milestone from chapter 3") },
        )
    }

    private fun sagaWithChapters(
        currentChapterId: Int,
        chapters: List<ChapterContent>,
    ): SagaContent {
        val act =
            ActContent(
                data = Act(id = 1, sagaId = 1, currentChapterId = currentChapterId),
                chapters = chapters,
                currentChapterInfo = chapters.find { it.data.id == currentChapterId },
            )
        return SagaContent(
            data = Saga(id = 1, title = "Test Saga", currentActId = 1),
            currentActInfo = act,
            acts = listOf(act),
        )
    }

    private fun chapterContent(
        id: Int,
        title: String = "Chapter $id",
        continuitySummary: ContinuitySummary? = null,
        events: List<TimelineContent> = emptyList(),
    ) = ChapterContent(
        data =
            Chapter(
                id = id,
                title = title,
                actId = 1,
                continuitySummary = continuitySummary,
            ),
        events = events,
    )

    private fun timelineWithSummary(
        id: Int,
        chapterId: Int,
        establishedFacts: List<String>,
    ) = TimelineContent(
        data =
            Timeline(
                id = id,
                chapterId = chapterId,
                sceneSummary =
                    SceneSummary(
                        currentLocation = "Somewhere",
                        charactersPresent = emptyList(),
                        immediateObjective = null,
                        currentConflict = null,
                        mood = null,
                        currentTimeOfDay = null,
                        establishedFacts = establishedFacts,
                    ),
            ),
    )
}
