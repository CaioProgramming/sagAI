package com.ilustris.sagai.features.characters.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import kotlin.collections.LinkedHashMap

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

    fun rankRelationships() = relationships.sortedByDescending { it.relationshipEvents.size }
}
