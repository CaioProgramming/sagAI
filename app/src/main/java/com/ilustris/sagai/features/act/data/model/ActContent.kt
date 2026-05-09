package com.ilustris.sagai.features.act.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.ai.prompts.LorePrompts
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
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
        actLimit: Int,
        rules: NarrativeRules,
    ): Boolean = chapters.count { it.isComplete(rules) } >= actLimit

    fun isComplete(rules: NarrativeRules): Boolean =
        isFull(rules.actUpdateLimit, rules) &&
            data.title.isNotEmpty() &&
            data.content.isNotEmpty()

    fun emotionalSummary() =
        buildString {
            appendLine(
                this.toAINormalize(
                    listOf(
                        "id",
                        "currentChapterId",
                        "sagaId",
                        "actId",
                        "featuredCharacters",
                        "coverImage",
                        "createdAt",
                        "content",
                    ),
                ),
            )
            chapters.normalizetoAIItems(
                listOf(
                    "id",
                    "currentEventId",
                    "sagaId",
                    "actId",
                    "featuredCharacters",
                    "coverImage",
                    "createdAt",
                    "overview",
                ),
            )
        }

    fun actSummary(showEvents: Boolean = true) =
        buildString {
            appendLine(
                data.toAINormalize(LorePrompts.ACT_EXCLUDED_FIELDS),
            )
            appendLine("CHAPTERS: ")
            chapters.forEach { chapter ->
                chapter == chapters.last()
                appendLine(chapters.indexOf(chapter) + 1)
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

    fun getChapterCovers(): List<String> = chapters.map { it.data.coverImage }.filter { it.isNotEmpty() }

    fun getPresentCharacters(allCharacters: List<CharacterContent>): List<CharacterContent> {
        val characterIds =
            chapters
                .flatMap { it.events.flatMap { it.messages.map { it.message.characterId } } }
                .toSet()
        return allCharacters.filter { it.data.id in characterIds }
    }
}
