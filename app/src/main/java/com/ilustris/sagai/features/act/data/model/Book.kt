package com.ilustris.sagai.features.act.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.chapter.data.model.Chapter

data class BookPage(
    val content: String,
    val pageNumber: Int? = null,
)

data class BookChapter(
    val title: String,
    val pages: List<BookPage>,
    /** Real [Chapter] these pages were written from; null for legacy single-shot volumes. */
    val chapterId: Int? = null,
)

/**
 * The writer's private working notes, never shown to the reader. Each chapter's pages hand these
 * to the next chapter's writing call so voice, open threads and motifs carry across the volume
 * without resending previous prose. Replaced (not accumulated) on every chapter.
 */
data class WriterNotes(
    val voice: String = "",
    val motifs: List<String> = emptyList(),
    val plantedThreads: List<String> = emptyList(),
    val payoffsDelivered: List<String> = emptyList(),
    val lastSceneState: String = "",
    val readerQuestions: List<String> = emptyList(),
)

/**
 * Volume (= Act) header and conclusion. Created when the prologue is written and sealed by the
 * act closure, which fills [coverQuote] and [authorNote]. Titles always come from the Act/Saga.
 */
@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = Act::class,
            parentColumns = ["id"],
            childColumns = ["actId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["actId"], unique = true)],
)
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actId: Int,
    val actTitle: String,
    val sagaTitle: String,
    val coverQuote: String = "",
    /** Legacy single-shot volumes only. Incremental volumes keep their pages in [BookChapterPages]. */
    val chapters: List<BookChapter> = emptyList(),
    val authorNote: String? = null,
    val prologue: List<BookPage>? = null,
    val prologueNotes: WriterNotes? = null,
    val epilogue: List<BookPage>? = null,
    /** Audiobook narrator, drawn once per volume so the voice never changes between chapters. */
    val narrationVoice: String? = null,
) {
    fun isSealed() = coverQuote.isNotBlank()

    fun isLegacy() = chapters.isNotEmpty()
}

/** Pages of a single [Chapter], written in the background once the chapter is synthesized. */
@Entity(
    tableName = "book_chapter_pages",
    foreignKeys = [
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["chapterId"], unique = true)],
)
data class BookChapterPages(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Int,
    val pages: List<BookPage>,
    val writerNotes: WriterNotes,
    val createdAt: Long = System.currentTimeMillis(),
)

data class BookChapterWriting(
    val pages: List<BookPage>,
    val writerNotes: WriterNotes,
)

data class BookPrologueWriting(
    val pages: List<BookPage>,
    val writerNotes: WriterNotes,
)

data class BookVolumeClosure(
    val coverQuote: String,
    val authorNote: String,
    val epilogue: List<BookPage> = emptyList(),
)
