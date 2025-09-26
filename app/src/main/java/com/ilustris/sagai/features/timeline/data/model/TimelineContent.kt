package com.ilustris.sagai.features.timeline.data.model

import androidx.room.Embedded
import androidx.room.Junction // Import Junction
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.features.characters.data.model.Character // Already imported
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
// Import RelationshipContent and RelationshipUpdateEvent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.wiki.data.model.Wiki

data class TimelineContent(
    @Embedded
    val data: Timeline,
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId",
        entity = Message::class,
    )
    val messages: List<MessageContent> = emptyList(),
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
        entity = com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation::class,
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
) {
    fun isFull(): Boolean = messages.size >= UpdateRules.LORE_UPDATE_LIMIT

    fun isComplete(): Boolean =
        isFull() &&
            data.title.isNotEmpty() &&
            data.content.isNotEmpty()

    fun numberOfRelationshipUpdates(): Int = updatedRelationshipDetails.size

    fun emotionalRanking(mainCharacter: Character?) =
        messages
            .filter {
                it.message.senderType == SenderType.USER || it.message.characterId == mainCharacter?.id
            }.groupBy { it.message.emotionalTone.toString() }
            .mapValues { entry -> entry.value.size }
}
