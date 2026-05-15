package com.ilustris.sagai.features.home.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterInfo
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki

data class SagaMetadata(
    @Embedded val data: Saga,
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
        entity = Act::class,
    )
    val acts: List<ActMetadata> = emptyList(),
    @Relation(
        parentColumn = "mainCharacterId",
        entityColumn = "id",
    )
    val mainCharacter: Character? = null,
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
        entity = Message::class,
    )
    val messages: List<MessageContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
        entity = CharacterRelation::class,
    )
    val relationships: List<RelationshipContent> = emptyList(),
) {
    fun isFull(rules: NarrativeRules): Boolean = acts.count { it.isComplete(rules) } >= rules.actUpdateLimit

    fun isComplete(rules: NarrativeRules): Boolean = data.isEnded || (isFull(rules) && messages.isNotEmpty())

    fun actNumber(actId: Int?): Int = if (actId == null) 0 else acts.indexOfFirst { it.data.id == actId } + 1
}

data class ActMetadata(
    @Embedded val data: Act,
    @Relation(
        parentColumn = "id",
        entityColumn = "actId",
        entity = Chapter::class,
    )
    val chapters: List<ChapterMetadata> = emptyList(),
) {
    fun currentChapter() = chapters.find { it.data.id == data.currentChapterId } ?: chapters.lastOrNull()

    fun isFull(rules: NarrativeRules): Boolean = chapters.count { it.isComplete(rules) } >= rules.chapterUpdateLimit

    fun isComplete(rules: NarrativeRules): Boolean = isFull(rules) && data.title.isNotBlank() && data.content.isNotBlank()

    fun actSummary(showEvents: Boolean = true): String =
        buildString {
            appendLine(data.toAINormalize(com.ilustris.sagai.core.ai.prompts.LorePrompts.ACT_EXCLUDED_FIELDS))
            appendLine("CHAPTERS: ")
            chapters.forEach { chapter ->
                appendLine(
                    "${chapters.indexOf(
                        chapter,
                    ) + 1} - ${chapter.data.toAINormalize(com.ilustris.sagai.core.ai.prompts.LorePrompts.CHAPTER_EXCLUDED_FIELDS)}",
                )
                if (showEvents) {
                    appendLine("EVENTS:")
                    appendLine(
                        chapter.events.map { it.data }.normalizetoAIItems(
                            com.ilustris.sagai.core.ai.prompts.LorePrompts.TIMELINE_EXCLUDED_FIELDS,
                        ),
                    )
                }
            }
        }
}

data class ChapterMetadata(
    @Embedded val data: Chapter,
    @Relation(
        parentColumn = "id",
        entityColumn = "chapterId",
        entity = Timeline::class,
    )
    val events: List<TimelineMetadata> = emptyList(),
) {
    fun isFull(rules: NarrativeRules): Boolean = events.size >= rules.chapterUpdateLimit

    fun isComplete(rules: NarrativeRules): Boolean = isFull(rules) && data.title.isNotBlank() && data.overview.isNotBlank()
}

fun ChapterMetadata.toInfo(sagaId: Int) =
    ChapterInfo(
        id = data.id,
        title = data.title,
        overview = data.overview,
        coverImage = data.coverImage,
        actId = data.actId,
        sagaId = sagaId,
        featuredCharacters = data.featuredCharacters,
        emotionalReview = data.emotionalReview,
        createdAt = data.createdAt,
    )

data class TimelineMetadata(
    @Embedded val data: Timeline,
    @Relation(
        parentColumn = "id",
        entity = CharacterEvent::class,
        entityColumn = "gameTimelineId",
    )
    val characterEventDetails: List<CharacterEventDetails> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId",
        entity = Wiki::class,
    )
    val updatedWikis: List<Wiki> = emptyList(),
    @Relation(
        parentColumn = "id",
        entity = CharacterRelation::class,
        entityColumn = "id",
        associateBy =
            Junction(
                value = RelationshipUpdateEvent::class,
                parentColumn = "timelineId",
                entityColumn = "relationId",
            ),
    )
    val updatedRelationshipDetails: List<RelationshipContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "firstSceneId",
        entity = Character::class,
    )
    val newlyAppearedCharacters: List<Character> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId",
        entity = Message::class,
    )
    val messages: List<MessageContent> = emptyList(),
) {
    fun isFull(limit: Int): Boolean = messages.size >= limit

    fun isComplete(rules: NarrativeRules): Boolean =
        isFull(rules.loreUpdateLimit) &&
            data.title.isNotBlank() &&
            data.content.isNotBlank()
}

fun SagaMetadata.flatMessages() = acts.flatMap { it.chapters.flatMap { it.events.flatMap { it.messages } } }

fun SagaMetadata.flatChapters() = acts.flatMap { it.chapters }

fun SagaMetadata.flatEvents() = acts.flatMap { it.chapters.flatMap { it.events } }

fun SagaMetadata.flatWikis() = acts.flatMap { it.chapters.flatMap { it.events.flatMap { it.updatedWikis } } }

fun SagaMetadata.getCurrentTimeLine() =
    acts
        .lastOrNull()
        ?.chapters
        ?.lastOrNull()
        ?.events
        ?.lastOrNull()

val SagaMetadata.currentActInfo
    get() =
        acts.find { it.data.id == data.currentActId } ?: acts.lastOrNull()

val SagaMetadata.currentChapterInfo
    get() =
        currentActInfo?.let { act ->
            act.chapters.find { it.data.id == act.data.currentChapterId }
                ?: act.chapters.lastOrNull()
        }

val SagaMetadata.currentEventInfo
    get() =
        currentChapterInfo?.let { chapter ->
            chapter.events.find { it.data.id == chapter.data.currentEventId }
                ?: chapter.events.lastOrNull()
        }

fun SagaMetadata.findTimeline(timelineId: Int) = flatEvents().find { it.data.id == timelineId }

/**
 * Act and chapter ordinals (1-based) where the latest chat message lives. Matches what the user
 * actually sees when [Saga.currentActId] / [Act.currentChapterId] are stale after refactors or
 * partial updates.
 */
fun SagaMetadata.actAndChapterOrdinalsFromLatestMessage(): Pair<Int, Int>? {
    val msgs = flatMessages()
    if (msgs.isEmpty()) return null
    val timeline =
        findTimeline(msgs.maxBy { it.message.timestamp }.message.timelineId) ?: return null
    val chapterId = timeline.data.chapterId
    val actId = flatChapters().find { it.data.id == chapterId }?.data?.actId ?: return null
    val actOrd = actNumber(actId).takeIf { it > 0 } ?: return null
    val chapterOrd = chapterNumber(chapterId).takeIf { it > 0 } ?: return null
    return actOrd to chapterOrd
}

fun SagaMetadata.actAndChapterOrdinalsFromProgressPointers(): Pair<Int, Int>? {
    val actId =
        data.currentActId
            ?: currentActInfo?.data?.id
            ?: return null
    val actOrd = actNumber(actId).takeIf { it > 0 } ?: return null
    val chapterId = currentChapterInfo?.data?.id ?: return null
    val chapterOrd = chapterNumber(chapterId).takeIf { it > 0 } ?: return null
    return actOrd to chapterOrd
}

fun SagaMetadata.subtitleActAndChapterOrdinals(): Pair<Int, Int> =
    actAndChapterOrdinalsFromLatestMessage()
        ?: actAndChapterOrdinalsFromProgressPointers()
        ?: (1 to 1)

fun SagaMetadata.toSagaInfo() =
    SagaInfo(
        id = data.id,
        title = data.title,
        genre = data.genre,
        variationId = data.variationId,
        icon = data.icon,
        playTimeMs = data.playTimeMs,
        description = data.description,
    )

fun SagaMetadata.getCharacters(filterMainCharacter: Boolean = false) =
    if (filterMainCharacter) {
        characters.filter { it.id != mainCharacter?.id }
    } else {
        characters
    }

fun SagaMetadata.actNumber(actId: Int): Int = acts.indexOfFirst { it.data.id == actId } + 1

fun SagaMetadata.chapterNumber(chapterId: Int?): Int =
    if (chapterId ==
        null
    ) {
        0
    } else {
        flatChapters().indexOfFirst { it.data.id == chapterId } + 1
    }

fun SagaMetadata.findCharacter(characterId: Int) = characters.find { it.id == characterId }

fun SagaMetadata.findCharacter(name: String?) =
    characters.find {
        it.name.equals(name, true) || it.lastName?.equals(name, true) == true ||
            it.nicknames?.any { nick -> nick.equals(name, true) } == true
    }

fun SagaMetadata.historySummary() =
    acts.joinToString(";\n---\n") {
        "${acts.indexOf(it) + 1} - ${it.actSummary(it == acts.last())}"
    }

fun SagaMetadata.summarizeRelationships(
    characterId: Int,
    threshold: Int = 3,
): String {
    val charRelationships =
        relationships.filter { it.characterOne.id == characterId || it.characterTwo.id == characterId }
    return charRelationships.sortedBy { it.relationshipEvents.size }.joinToString(";\n") {
        it.summarizeRelation(threshold)
    }
}

fun SagaMetadata.emotionalSummary() =
    buildString {
        acts.forEach { act ->
            appendLine(
                act.data.toAINormalize(
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
            appendLine("Chapters emotional review: ${act.chapters.map { it.data.emotionalReview }}")
        }
    }

fun SagaMetadata.generateCharacterRelationsSummary(filterNames: List<String>? = null): String {
    if (relationships.isEmpty()) return "No significant relationships tracked."
    val filteredRelations =
        if (filterNames == null) {
            relationships
        } else {
            relationships.filter { relation ->
                val char1 = characters.find { it.id == relation.data.characterOneId }?.name?.lowercase()
                val char2 = characters.find { it.id == relation.data.characterTwoId }?.name?.lowercase()
                val filterLower = filterNames.map { it.lowercase() }
                filterLower.contains(char1) || filterLower.contains(char2)
            }
        }
    if (filteredRelations.isEmpty()) return "No relevant relationships for this scene."
    return filteredRelations.joinToString("\n---\n") { it.summarizeRelation() }
}

fun SagaMetadata.getDirectiveKey(targetActId: Int? = null): String {
    val actId = targetActId ?: data.currentActId
    val actIndex = acts.indexOfFirst { it.data.id == actId }
    return when (actIndex) {
        0 -> "act_1_hook_blueprint"
        1 -> "act_2_rising_action_blueprint"
        2 -> "act_3_resolution_blueprint"
        else -> "act_1_hook_blueprint"
    }
}
