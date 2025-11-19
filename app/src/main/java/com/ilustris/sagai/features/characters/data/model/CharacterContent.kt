package com.ilustris.sagai.features.characters.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.utils.listToAINormalize
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.timeline.data.model.Timeline

data class CharacterContent(
    @Embedded
    val data: Character,
    @Relation(
        parentColumn = "id",
        entityColumn = "characterId",
        entity = CharacterEvent::class,
    )
    val events: List<CharacterEventDetails> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "characterOneId",
        entity = CharacterRelation::class,
    )
    private val relationshipsAsFirst: List<RelationshipContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "characterTwoId",
        entity = CharacterRelation::class,
    )
    private val relationshipsAsSecond: List<RelationshipContent> = emptyList(),
) {
    val relationships: List<RelationshipContent>
        get() {
            if (relationshipsAsFirst.isEmpty() && relationshipsAsSecond.isEmpty()) return emptyList()
            val byId = LinkedHashMap<Int, RelationshipContent>()
            relationshipsAsFirst.forEach { byId[it.data.id] = it }
            relationshipsAsSecond.forEach { byId[it.data.id] = it }
            return byId.values.toList()
        }

    fun findRelationship(characterId: Int) =
        relationships.find {
            it.characterOne.id == characterId ||
                it.characterTwo.id == characterId
        }

    fun summarizeRelationships() =
        relationships.sortedBy { it.relationshipEvents.size }.joinToString(";\n") {
            "${it.characterOne.name} ${it.data.emoji} ${it.characterTwo.name}:\n${
                it.relationshipEvents.takeLast(5).listToAINormalize(
                    listOf("timestamp", "relationId", "timelineId", "id"),
                )
            }"
        }

    fun rankRelationships() = relationships.sortedByDescending { it.relationshipEvents.size }

    fun sortEventsByTimeline(timeLineEvents: List<Timeline>) =
        events.sortedByDescending {
            timeLineEvents.find { event -> event.id == it.timeline?.id }?.createdAt
        }

    fun sortRelationsByTimeline(timeLineEvents: List<Timeline>) =
        relationships
            .sortedByDescending {
                val sortedEvents = it.sortedByEvents(timeLineEvents)
                sortedEvents.firstOrNull()?.timelineId ?: sortedEvents.lastOrNull()?.timelineId
            }.map {
                it.copy(relationshipEvents = it.sortedByEvents(timeLineEvents))
            }.filter { it.relationshipEvents.isNotEmpty() }
}
