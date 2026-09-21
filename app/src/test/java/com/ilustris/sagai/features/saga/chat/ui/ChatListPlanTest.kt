package com.ilustris.sagai.features.saga.chat.ui

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.presentation.ActDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.ChapterDisplayData
import com.ilustris.sagai.features.saga.chat.presentation.TimelineDisplayData
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.domain.TimelineCardContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatListPlanTest {
    @Test
    fun `plan emits the chat order newest first`() {
        val plan =
            buildChatPlan(
                actList = listOf(act(id = 1, chapters = listOf(chapter(id = 10, messageIds = listOf(101, 100))))),
                sagaId = 7,
                isEnded = false,
                endMessage = "",
                hasReasoning = false,
            )

        assertEquals(
            listOf(
                "bottom-spacer",
                "chapter-10",
                "timeline-1000",
                "message-101",
                "message-100",
                "timeline-1000-spark",
                "chapter-10-intro",
                "chapter-10-title",
                "act-1-title",
                "saga-7-header",
            ),
            plan.map { it.key },
        )
    }

    @Test
    fun `reasoning and end of saga entries are conditional`() {
        val actList = listOf(act(id = 1, chapters = listOf(chapter(id = 10, messageIds = listOf(100)))))

        val bare = buildChatPlan(actList, sagaId = 7, isEnded = false, endMessage = "", hasReasoning = false)
        val full = buildChatPlan(actList, sagaId = 7, isEnded = true, endMessage = "The end.", hasReasoning = true)

        assertTrue(bare.none { it == ChatEntry.Reasoning || it == ChatEntry.RecapHero })
        assertEquals(
            listOf("bottom-spacer", "reasoning", "recap-hero", "end-message", "ended-at"),
            full.take(5).map { it.key },
        )
        // An ended saga with no closing message keeps the recap cards out.
        val endedWithoutMessage = buildChatPlan(actList, sagaId = 7, isEnded = true, endMessage = "", hasReasoning = false)
        assertTrue(endedWithoutMessage.none { it == ChatEntry.RecapHero })
    }

    @Test
    fun `incomplete chapter has no cover and act with conclusion has no title`() {
        val plan =
            buildChatPlan(
                actList =
                    listOf(
                        act(
                            id = 1,
                            content = "Act synthesis body.",
                            chapters = listOf(chapter(id = 10, isComplete = false, messageIds = listOf(100))),
                        ),
                    ),
                sagaId = 7,
                isEnded = false,
                endMessage = "",
                hasReasoning = false,
            )

        assertTrue(plan.none { it is ChatEntry.ChapterCover })
        assertTrue(plan.none { it is ChatEntry.ActTitle })
        assertTrue(plan.any { it is ChatEntry.ActConclusion })
    }

    @Test
    fun `chapter anchor resolves to the oldest message of that chapter`() {
        val plan =
            buildChatPlan(
                actList =
                    listOf(
                        act(
                            id = 1,
                            chapters =
                                listOf(
                                    chapter(id = 20, timelineId = 2000, messageIds = listOf(201, 200)),
                                    chapter(id = 10, timelineId = 1000, messageIds = listOf(101, 100)),
                                ),
                        ),
                    ),
                sagaId = 7,
                isEnded = false,
                endMessage = "",
                hasReasoning = false,
            )

        // Messages run newest first, so a chapter's oldest message is its last entry — that is
        // where the rail lands so the chapter reads forward from its start.
        assertEquals("message-200", plan[plan.chapterAnchorIndex(20)].key)
        assertEquals("message-100", plan[plan.chapterAnchorIndex(10)].key)
    }

    @Test
    fun `chapter with no messages falls back to its title`() {
        val plan =
            buildChatPlan(
                actList = listOf(act(id = 1, chapters = listOf(chapter(id = 10, messageIds = emptyList())))),
                sagaId = 7,
                isEnded = false,
                endMessage = "",
                hasReasoning = false,
            )

        assertEquals("chapter-10-title", plan[plan.chapterAnchorIndex(10)].key)
        assertEquals(-1, plan.chapterAnchorIndex(chapterId = 999))
    }

    @Test
    fun `every entry below an act is stamped with its chapter and act`() {
        val plan =
            buildChatPlan(
                actList = listOf(act(id = 1, chapters = listOf(chapter(id = 10, messageIds = listOf(100))))),
                sagaId = 7,
                isEnded = false,
                endMessage = "",
                hasReasoning = false,
            )

        val message = plan.first { it is ChatEntry.Message }
        assertEquals(10, message.chapterId)
        assertEquals(1, message.actId)
    }

    private fun act(
        id: Int,
        content: String = "",
        introduction: String = "",
        chapters: List<ChapterDisplayData>,
    ) = ActDisplayData(
        content = ActMetadata(data = Act(id = id, content = content, introduction = introduction)),
        isComplete = false,
        chapters = chapters,
    )

    private fun chapter(
        id: Int,
        timelineId: Int = 1000,
        isComplete: Boolean = true,
        messageIds: List<Int>,
    ): ChapterDisplayData {
        val timeline = Timeline(id = timelineId, chapterId = id)
        val messages =
            messageIds.map {
                MessageContent(
                    message =
                        Message(
                            id = it,
                            text = "message $it",
                            senderType = SenderType.CHARACTER,
                            timelineId = timelineId,
                        ),
                    reactions = emptyList(),
                )
            }
        return ChapterDisplayData(
            chapter = ChapterMetadata(data = Chapter(id = id, actId = 1)),
            isComplete = isComplete,
            timelineSummaries =
                listOf(
                    TimelineDisplayData(
                        isComplete = true,
                        timeline =
                            TimelineCardContent(
                                timelineContent = TimelineMetadata(data = timeline, messages = messages),
                                overallEmotion = null,
                                chapterNumber = null,
                                canShowData = true,
                            ),
                    ),
                ),
        )
    }
}
