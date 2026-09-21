package com.ilustris.sagai.features.act.data.model

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActContentBookTest {
    private val rules = NarrativeRules(loreUpdateLimit = 1, chapterUpdateLimit = 1, actUpdateLimit = 3)

    @Test
    fun `missing chapters are completed chapters without pages in reading order`() {
        val act =
            act(
                chapters =
                    listOf(
                        chapter(id = 3),
                        chapter(id = 1, pages = pages(1)),
                        chapter(id = 2),
                        chapter(id = 4, complete = false),
                    ),
            )

        assertEquals(listOf(2, 3), act.missingBookChapters(rules).map { it.data.id })
    }

    @Test
    fun `volume chapters are titled by the real chapter and ordered`() {
        val act =
            act(
                chapters =
                    listOf(
                        chapter(id = 2, title = "Second", pages = pages(2)),
                        chapter(id = 1, title = "First", pages = pages(1)),
                    ),
            )

        val volume = act.volumeChapters()

        assertEquals(listOf("First", "Second"), volume.map { it.title })
        assertEquals(listOf(1, 2), volume.map { it.chapterId })
    }

    @Test
    fun `prologue is needed only once the act has an introduction and no prologue yet`() {
        assertFalse(act(introduction = "").needsPrologue())
        assertTrue(act(introduction = "Once upon a time").needsPrologue())
        assertFalse(
            act(
                introduction = "Once upon a time",
                book = book(prologue = listOf(BookPage("The beginning"))),
            ).needsPrologue(),
        )
    }

    @Test
    fun `an unsealed book is not readable`() {
        val act = act(book = book(prologue = listOf(BookPage("The beginning"))))

        assertFalse(act.hasReadableBook())
        assertTrue(act.hasBookProgress())
    }

    @Test
    fun `sealed incremental volume is ready only without missing chapters`() {
        val sealed = book(coverQuote = "Quote", authorNote = "Note")

        assertTrue(act(book = sealed, chapters = listOf(chapter(id = 1, pages = pages(1)))).isVolumeReady(rules))
        assertFalse(act(book = sealed, chapters = listOf(chapter(id = 1))).isVolumeReady(rules))
    }

    @Test
    fun `legacy volume stays ready without chapter pages`() {
        val legacy =
            book(
                coverQuote = "Quote",
                chapters = listOf(BookChapter("Old chapter", listOf(BookPage("Old prose")))),
            )

        assertTrue(act(book = legacy, chapters = listOf(chapter(id = 1))).isVolumeReady(rules))
    }

    private fun act(
        introduction: String = "",
        chapters: List<ChapterContent> = emptyList(),
        book: Book? = null,
    ) = ActContent(
        data = Act(id = 7, title = "Act", content = "Done", introduction = introduction),
        chapters = chapters,
        book = book,
    )

    private fun chapter(
        id: Int,
        title: String = "Chapter $id",
        complete: Boolean = true,
        pages: BookChapterPages? = null,
    ) = ChapterContent(
        data = Chapter(id = id, title = title, content = if (complete) "Content" else "", actId = 7),
        events = listOf(completeEvent(chapterId = id)),
        bookPages = pages?.copy(chapterId = id),
    )

    private fun completeEvent(chapterId: Int) =
        TimelineContent(
            data = Timeline(id = chapterId * 10, title = "Event", content = "Happened", chapterId = chapterId),
            messages =
                listOf(
                    MessageContent(
                        message = Message(text = "Hi", senderType = SenderType.CHARACTER, timelineId = chapterId * 10),
                        reactions = emptyList(),
                    ),
                ),
        )

    private fun pages(chapterId: Int) =
        BookChapterPages(
            chapterId = chapterId,
            pages = listOf(BookPage("Prose")),
            writerNotes = WriterNotes(),
        )

    private fun book(
        coverQuote: String = "",
        authorNote: String? = null,
        chapters: List<BookChapter> = emptyList(),
        prologue: List<BookPage>? = null,
    ) = Book(
        actId = 7,
        actTitle = "Act",
        sagaTitle = "Saga",
        coverQuote = coverQuote,
        chapters = chapters,
        authorNote = authorNote,
        prologue = prologue,
    )
}
