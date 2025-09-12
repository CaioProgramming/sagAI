package com.ilustris.sagai.features.home.data.model

import android.icu.util.Calendar
import android.util.Log
import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.ActDirectives
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki
import kotlin.jvm.javaClass

data class SagaContent(
    @Embedded
    val data: Saga,
    @Relation(
        parentColumn = "mainCharacterId",
        entityColumn = "id",
        entity = Character::class,
    )
    val mainCharacter: CharacterContent? = null,
    @Relation(
        entity = Act::class,
        parentColumn = "currentActId",
        entityColumn = "id",
    )
    val currentActInfo: ActContent? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
        entity = Character::class,
    )
    val characters: List<CharacterContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
        entity = Wiki::class,
    )
    val wikis: List<Wiki> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
        entity = Act::class,
    )
    val acts: List<ActContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
        entity = CharacterRelation::class,
    )
    val relationships: List<RelationshipContent> = emptyList(),
) {
    fun isFull(): Boolean = acts.count { it.isComplete() } == UpdateRules.MAX_ACTS_LIMIT

    fun isComplete(): Boolean = isFull() && data.endMessage.isNotEmpty()

    fun chaptersSize() = acts.sumOf { it.chapters.size }

    fun eventsSize() = acts.sumOf { it.chapters.sumOf { it.events.size } }

    fun messagesSize() = acts.sumOf { it.chapters.sumOf { it.events.sumOf { it.messages.size } } }
}

fun SagaContent.getCharacters(filterMainCharacter: Boolean = false) =
    if (filterMainCharacter) {
        characters.filter { it.data.id != mainCharacter?.data?.id }.map { it.data }
    } else {
        characters.map { it.data }
    }

fun SagaContent.isFirstAct() = currentActInfo?.data?.id == acts.first().data.id

fun SagaContent.isFirstChapter() = flatChapters().first().data.id == currentActInfo?.currentChapterInfo?.data?.id

fun SagaContent.isFirstTimeline() =
    flatEvents().first().data.id ==
        currentActInfo
            ?.currentChapterInfo
            ?.currentEventInfo
            ?.data
            ?.id

fun SagaContent.findTimelineChapter(timeline: Timeline) = flatChapters().find { it.data.id == timeline.chapterId }

fun SagaContent.findChapterAct(chapter: Chapter?) = acts.find { it.data.id == chapter?.actId }

fun SagaContent.isTimelineOnFirstChapter(timeline: Timeline) = findTimelineChapter(timeline) == flatChapters().first()

fun SagaContent.isChapterOnFirstAct(chapter: Chapter) = findChapterAct(chapter) == acts.first()

fun SagaContent.flatMessages() = acts.flatMap { it.chapters.flatMap { it.events.flatMap { it.messages } } }

fun SagaContent.flatEvents() = acts.flatMap { it.chapters.flatMap { it.events } }

fun SagaContent.flatChapters() = acts.flatMap { it.chapters }

fun SagaContent.getCurrentTimeLine() = currentActInfo?.currentChapterInfo?.currentEventInfo

fun SagaContent.getCurrentMessages() = getCurrentTimeLine()?.messages

fun SagaContent.getCurrentChapter() = currentActInfo?.currentChapterInfo

fun SagaContent.chapterNumber(chapter: Chapter) = flatChapters().indexOfFirst { it.data.id == chapter.id } + 1

fun SagaContent.actNumber(act: Act) = acts.indexOfFirst { it.data.id == act.id } + 1

fun SagaContent.getDirective(): String {
    val actsCount = acts.size
    Log.d(
        javaClass.simpleName,
        "Getting directive. Total acts count: $actsCount for saga(${this.data.id}) -> ${this.data.title}",
    )
    return when (actsCount) {
        0, 1 -> ActDirectives.FIRST_ACT_DIRECTIVES
        2 -> ActDirectives.SECOND_ACT_DIRECTIVES
        3 -> ActDirectives.THIRD_ACT_DIRECTIVES
        else -> ActDirectives.FIRST_ACT_DIRECTIVES
    }
}

fun SagaContent.rankByHour() =
    flatMessages()
        .groupBy {
            val date =
                Calendar.getInstance().apply {
                    timeInMillis = it.message.timestamp
                }
            date.get(Calendar.HOUR_OF_DAY)
        }.toSortedMap()

fun SagaContent.emotionalSummary() =

    acts
        .map {
            """
            Act ${it.data.title}: ${it.data.emotionalReview}
            ${it.chapters.joinToString(
                ";",
            ) { chapter -> "${chapterNumber(chapter.data)} Chapter ${chapter.data.title}: ${chapter.data.emotionalReview}" } }
            """.trimIndent()
        }
