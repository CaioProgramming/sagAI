package com.ilustris.sagai.features.act.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.ai.prompts.LorePrompts
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.CharacterContent

data class ActContent(
    @Embedded
    val data: Act,
    @Relation(
        parentColumn = "currentChapterId",
        entityColumn = "id",
        entity = Chapter::class,
    )
    val currentChapterInfo: ChapterContent? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "actId",
        entity = Chapter::class,
    )
    val chapters: List<ChapterContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "actId",
        entity = Book::class,
    )
    val book: Book? = null,
) {
    fun isFull(
        chapterLimit: Int,
        rules: NarrativeRules,
    ): Boolean = chapters.count { it.isComplete(rules) } >= chapterLimit

    fun isComplete(rules: NarrativeRules): Boolean =
        isFull(rules.actUpdateLimit, rules) &&
            data.title.isNotEmpty() &&
            data.content.isNotEmpty()

    fun emotionalSummary() =
        buildMap {
            put("Act", data.title)
            put(
                "ChaptersEmotionalReview",
                chapters.joinToString {
                    "${it.data.title}: ${it.data.emotionalReview}"
                },
            )
            put("ActEmotionalConclusion", data.emotionalReview)
        }.toJsonFormat()

    fun actSummary(
        showEvents: Boolean = true,
        includeChapters: Boolean = true,
    ) = buildString {
        appendLine(
            data.toAINormalize(LorePrompts.ACT_EXCLUDED_FIELDS),
        )
        if (includeChapters) {
            appendLine("CHAPTERS: ")
            chapters.forEach { chapter ->
                appendLine("Chapter ${chapters.indexOf(chapter) + 1}")
                appendLine(
                    chapter.data.toAINormalize(LorePrompts.CHAPTER_EXCLUDED_FIELDS),
                )
                appendLine()
                if (showEvents) {
                    appendLine("CHAPTER EVENTS:")
                    appendLine(
                        chapter.events.map { it.data }.normalizetoAIItems(
                            LorePrompts.TIMELINE_EXCLUDED_FIELDS,
                        ),
                    )
                }
            }
        }
    }

    fun getChapterCovers(): List<String> = chapters.map { it.data.coverImage }.filter { it.isNotEmpty() }

    fun getPresentCharacters(allCharacters: List<CharacterContent>): List<CharacterContent> {
        val characterIds =
            chapters
                .flatMap { it.events.flatMap { it.messages.map { it.message.characterId } } }
                .toSet()
        return allCharacters.filter { it.data.id in characterIds }
    }

    /** Completed chapters in reading order, as the incremental book sees them. */
    fun bookChapters(rules: NarrativeRules): List<ChapterContent> =
        chapters.filter { it.isComplete(rules) }.sortedBy { it.data.id }

    /** Written chapter pages in reading order, titled by the real chapter. */
    fun volumeChapters(): List<BookChapter> =
        chapters
            .sortedBy { it.data.id }
            .mapNotNull { chapter ->
                chapter.bookPages?.let {
                    BookChapter(title = chapter.data.title, pages = it.pages, chapterId = chapter.data.id)
                }
            }

    fun missingBookChapters(rules: NarrativeRules): List<ChapterContent> = bookChapters(rules).filter { it.bookPages == null }

    fun needsPrologue() = data.introduction.isNotBlank() && book?.prologue.isNullOrEmpty() && book?.isSealed() != true

    fun hasReadableBook() = book?.isSealed() == true

    fun isVolumeReady(rules: NarrativeRules): Boolean =
        hasReadableBook() && (book?.isLegacy() == true || missingBookChapters(rules).isEmpty())

    /** Whether the incremental writer already started this volume — used to scope silent healing. */
    fun hasBookProgress() = book != null || chapters.any { it.bookPages != null }
}
