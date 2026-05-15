package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NarrativeCheckTest {
    private val rules =
        NarrativeRules(
            maxActsLimit = 3,
            actUpdateLimit = 3,
            loreUpdateLimit = 15,
            chapterUpdateLimit = 5,
        )

    @Test
    fun `empty saga requests create act`() {
        val saga = sagaWithActs(emptyList())
        assertEquals(NarrativeAction.CreateAct, NarrativeCheck.validateProgression(saga, rules))
    }

    @Test
    fun `ended saga needs no action`() {
        val saga =
            sagaWithActs(
                acts = listOf(incompleteAct(id = 1, chapters = emptyList())),
                isEnded = true,
            )
        assertNull(NarrativeCheck.validateProgression(saga, rules))
    }

    @Test
    fun `chapter without timeline requests create timeline`() {
        val chapter =
            incompleteChapter(id = 10, events = emptyList(), introduction = "Chapter intro")
        val act =
            incompleteAct(
                id = 1,
                chapters = listOf(chapter),
                currentChapterId = 10,
                introduction = "Act intro",
            )
        val saga = sagaWithActs(listOf(act), currentActId = 1)
        assertEquals(
            NarrativeAction.CreateTimeline(chapter),
            NarrativeCheck.validateProgression(saga, rules),
        )
    }

    @Test
    fun `full timeline requests evolve`() {
        val timeline =
            timelineWithMessages(
                id = 100,
                messageCount = rules.loreUpdateLimit,
                title = "",
                content = "",
            )
        val chapter =
            incompleteChapter(
                id = 10,
                events = listOf(timeline),
                currentEventId = 100,
                introduction = "Chapter intro",
            )
        val act =
            incompleteAct(
                id = 1,
                chapters = listOf(chapter),
                currentChapterId = 10,
                introduction = "Act intro",
            )
        val saga = sagaWithActs(listOf(act), currentActId = 1)
        assertEquals(
            NarrativeAction.EvolveTimeline(timeline),
            NarrativeCheck.validateProgression(saga, rules),
        )
    }

    @Test
    fun `complete timeline requests close`() {
        val timeline =
            timelineWithMessages(
                id = 100,
                messageCount = rules.loreUpdateLimit,
                title = "Scene title",
                content = "Scene content",
            )
        val chapter =
            incompleteChapter(
                id = 10,
                events = listOf(timeline),
                currentEventId = 100,
                introduction = "Chapter intro",
            )
        val act =
            incompleteAct(
                id = 1,
                chapters = listOf(chapter),
                currentChapterId = 10,
                introduction = "Act intro",
            )
        val saga = sagaWithActs(listOf(act), currentActId = 1)
        assertEquals(
            NarrativeAction.CloseTimeline(chapter),
            NarrativeCheck.validateProgression(saga, rules),
        )
    }

    private fun sagaWithActs(
        acts: List<ActContent>,
        currentActId: Int? = null,
        isEnded: Boolean = false,
    ): SagaContent =
        SagaContent(
            data =
                Saga(
                    id = 1,
                    title = "Test Saga",
                    currentActId = currentActId,
                    isEnded = isEnded,
                ),
            currentActInfo = acts.find { it.data.id == currentActId },
            acts = acts,
        )

    private fun incompleteAct(
        id: Int,
        chapters: List<ChapterContent>,
        currentChapterId: Int? = null,
        introduction: String = "",
    ): ActContent =
        ActContent(
            data =
                Act(
                    id = id,
                    sagaId = 1,
                    currentChapterId = currentChapterId,
                    introduction = introduction,
                ),
            chapters = chapters,
            currentChapterInfo = chapters.find { it.data.id == currentChapterId },
        )

    private fun incompleteChapter(
        id: Int,
        events: List<TimelineContent>,
        currentEventId: Int? = null,
        introduction: String = "",
    ): ChapterContent =
        ChapterContent(
            data =
                Chapter(
                    id = id,
                    actId = 1,
                    currentEventId = currentEventId,
                    introduction = introduction,
                ),
            events = events,
            currentEventInfo = events.find { it.data.id == currentEventId },
        )

    private fun timelineWithMessages(
        id: Int,
        messageCount: Int,
        title: String,
        content: String,
    ): TimelineContent =
        TimelineContent(
            data = Timeline(id = id, chapterId = 10, title = title, content = content),
            messages =
                List(messageCount) {
                    MessageContent(
                        message =
                            Message(
                                id = it,
                                text = "msg",
                                senderType = SenderType.USER,
                                timelineId = id,
                            ),
                        reactions = emptyList(),
                    )
                },
        )
}
