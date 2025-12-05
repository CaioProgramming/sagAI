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
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki

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

fun SagaContent.findTimeline(timelineId: Int) = flatEvents().find { it.data.id == timelineId }

fun SagaContent.findCharacter(characterId: Int) = characters.find { it.data.id == characterId }

fun SagaContent.findCharacter(name: String?): CharacterContent? {
    if (name == null) return null
    val normalizedInputNameTokens = name.lowercase().split(" ")
    return characters.find { characterContent ->
        val characterNameTokens =
            characterContent.data.name
                .lowercase()
                .split(" ")
        normalizedInputNameTokens.any { inputToken -> characterNameTokens.contains(inputToken) }
    }
}

fun SagaContent.findTimelineChapter(timeline: Timeline) = flatChapters().find { it.data.id == timeline.chapterId }

fun SagaContent.findChapterAct(chapter: Chapter?) = acts.find { it.data.id == chapter?.actId }

fun SagaContent.isTimelineOnFirstChapter(timeline: Timeline) = findTimelineChapter(timeline) == flatChapters().first()

fun SagaContent.isChapterOnFirstAct(chapter: Chapter) = findChapterAct(chapter) == acts.first()

fun SagaContent.flatMessages() = acts.flatMap { it.chapters.flatMap { it.events.flatMap { it.messages } } }

fun SagaContent.flatEvents() = acts.flatMap { it.chapters.flatMap { it.events } }

fun SagaContent.flatChapters() = acts.flatMap { it.chapters }

fun SagaContent.getCurrentTimeLine() = currentActInfo?.currentChapterInfo?.currentEventInfo

fun SagaContent.getCurrentMessages(): List<Message>? = getCurrentTimeLine()?.messages?.map { it.message }

fun SagaContent.relationshipsSortedByEvents() =
    relationships
        .sortedByDescending {
            val sortedEvents = it.sortedByEvents(flatEvents().map { it.data })
            sortedEvents.firstOrNull()?.timelineId ?: sortedEvents.lastOrNull()?.timelineId
        }.map {
            it.copy(relationshipEvents = it.sortedByEvents(flatEvents().map { it.data }))
        }.filter { it.relationshipEvents.isNotEmpty() }

fun SagaContent.chapterNumber(chapter: Chapter?) = if (chapter == null) 0 else flatChapters().indexOfFirst { it.data.id == chapter?.id } + 1

fun SagaContent.actNumber(act: Act?): Int =
    acts.find { it.data.id == act?.id }?.let { requestedAct ->
        acts.indexOf(requestedAct) + 1
    } ?: run {
        1
    }

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
    buildString {
        acts.forEach {
            it.emotionalSummary(this@emotionalSummary)
            appendLine("Emotional profile on ${it.data.title}: ${it.data.emotionalReview}")
        }
    }

@Suppress("ktlint:standard:max-line-length")
fun SagaContent.generateCharacterRelationsSummary(): String {
    if (relationships.isEmpty()) return "No specific character relationships were formally established or tracked."
    return relationships.joinToString("; ") { relationContent ->
        val char1Name =
            characters.find { it.data.id == relationContent.data.characterOneId }?.data?.name
                ?: "Character ${relationContent.data.characterOneId}"
        val char2Name =
            characters.find { it.data.id == relationContent.data.characterTwoId }?.data?.name
                ?: "Character ${relationContent.data.characterTwoId}"
        val relationDesc = relationContent.data.title.takeIf { it.isNotBlank() } ?: "Unnamed relationship"
        "$char1Name and $char2Name: $relationDesc"
    }
}

fun List<Message>.filterCharacterMessages(characterId: Int?): List<Message> {
    if (characterId == null) return emptyList()
    return this.filter { it.characterId == characterId }
}

fun SagaContent.generateActLevelEmotionalFlowText(): String {
    if (acts.isEmpty()) return "The emotional flow through the acts and chapters was not explicitly tracked for the player."

    val playerCharacterId = this.mainCharacter?.data?.id

    return acts.joinToString("") { actContent ->
        val actTitle = actContent.data.title
        if (actContent.chapters.isEmpty()) {
            "Act '$actTitle': No chapter data to analyze player's emotional flow."
        } else {
            val chapterFeelings =
                actContent.chapters.joinToString(", ") { chapterContent ->
                    val chapterTitle = chapterContent.data.title
                    val playerMessagesInChapter =
                        chapterContent.events
                            .flatMap { event -> event.messages.map { it.message } }
                            .filterCharacterMessages(playerCharacterId)

                    val emotionalTonesInChapter = playerMessagesInChapter.mapNotNull { it.emotionalTone }

                    if (emotionalTonesInChapter.isEmpty()) {
                        "Chapter '$chapterTitle': No player emotional tones recorded in messages."
                    } else {
                        val toneCounts = emotionalTonesInChapter.groupingBy { it.name }.eachCount()
                        val dominantTone = toneCounts.maxByOrNull { it.value }?.key ?: "varied"
                        "Chapter '$chapterTitle': player predominantly felt $dominantTone"
                    }
                }
            "Act '$actTitle': $chapterFeelings"
        }
    }
}

fun SagaContent.hasMoreThanOneChapter() = flatChapters().size > 1
