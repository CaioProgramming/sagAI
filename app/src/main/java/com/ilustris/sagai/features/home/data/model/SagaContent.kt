package com.ilustris.sagai.features.home.data.model

import android.util.Log
import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.ActDirectives
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.wiki.data.model.Wiki
import kotlin.jvm.javaClass

data class SagaContent(
    @Embedded
    val data: Saga,
    @Relation(
        parentColumn = "mainCharacterId",
        entityColumn = "id",
    )
    val mainCharacter: Character? = null,
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
    val characters: List<Character> = emptyList(),
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
) {
    fun isFull(): Boolean = acts.count { it.isComplete() } == UpdateRules.MAX_ACTS_LIMIT

    fun isComplete(): Boolean = isFull() && data.endMessage.isNotEmpty()

    fun chaptersSize() = acts.sumOf { it.chapters.size }

    fun eventsSize() = acts.sumOf { it.chapters.sumOf { it.events.size } }

    fun messagesSize() = acts.sumOf { it.chapters.sumOf { it.events.sumOf { it.messages.size } } }
}

fun SagaContent.isFirstAct() = currentActInfo == acts.first()

fun SagaContent.isFirstChapter() =
    isFirstAct() &&
        (
            currentActInfo?.chapters?.isEmpty() == true ||
                currentActInfo?.currentChapterInfo == currentActInfo?.chapters?.first()
        )

fun SagaContent.isFirstEvent() =
    isFirstAct() &&
        isFirstChapter() &&
        currentActInfo?.currentChapterInfo?.events?.isEmpty() == true ||
        currentActInfo?.currentChapterInfo?.currentEventInfo ==
        currentActInfo
            ?.chapters
            ?.first()
            ?.events
            ?.first()

fun SagaContent.flatMessages() = acts.flatMap { it.chapters.flatMap { it.events.flatMap { it.messages } } }

fun SagaContent.flatEvents() = acts.flatMap { it.chapters.flatMap { it.events } }

fun SagaContent.flatChapters() = acts.flatMap { it.chapters }

fun SagaContent.getCurrentTimeLine() = currentActInfo?.currentChapterInfo?.currentEventInfo

fun SagaContent.getCurrentMessages() = getCurrentTimeLine()?.messages

fun SagaContent.getCurrentChapter() = currentActInfo?.currentChapterInfo

fun SagaContent.chapterNumber(chapter: Chapter) = flatChapters().indexOfFirst { it.data.id == chapter.id } + 1

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
