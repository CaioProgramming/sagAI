package com.ilustris.sagai.features.characters.data.model

import androidx.room.Embedded
import androidx.room.Relation
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

    fun summarizeRelationships(threshold: Int = 3) =
        relationships.sortedBy { it.relationshipEvents.size }.joinToString(";\n") {
            it.summarizeRelation(threshold)
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

fun Character.fullName() = "$name ${lastName ?: ""}".trim()

private fun Character.displayNameTokens(): List<String> {
    val tokens = mutableListOf<String>()
    tokens.addAll(name.lowercase().split(" "))
    lastName
        ?.lowercase()
        ?.split(" ")
        ?.let { tokens.addAll(it) }
    nicknames?.forEach { nickname -> tokens.addAll(nickname.lowercase().split(" ")) }
    return tokens.filter { it.isNotBlank() }
}

fun Character.matchesDisplayNameStrict(query: String): Boolean {
    val normalizedQuery = query.trim().lowercase()
    return fullName().lowercase() == normalizedQuery ||
        nicknames.orEmpty().any { it.trim().lowercase() == normalizedQuery }
}

/**
 * Resolves a spoken or written name to a saga character (exact full name / nickname first,
 * then token-based fuzzy matching for chat and narrative references).
 */
fun List<Character>.findByDisplayName(query: String?): Character? {
    if (query.isNullOrBlank()) return null

    find { it.matchesDisplayNameStrict(query) }?.let { return it }

    val normalizedInput = query.lowercase().trim()
    val normalizedInputTokens = normalizedInput.split(" ").filter { it.isNotBlank() }

    find { character ->
        val allTokens = character.displayNameTokens()
        normalizedInputTokens.all { inputToken -> allTokens.contains(inputToken) }
    }?.let { return it }

    return find { character ->
        val allTokens = character.displayNameTokens()
        normalizedInputTokens.any { inputToken -> allTokens.contains(inputToken) }
    }
}
