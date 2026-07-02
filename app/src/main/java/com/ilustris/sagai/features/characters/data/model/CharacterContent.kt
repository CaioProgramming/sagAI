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
        entityColumn = "characterId",
        entity = CharacterArc::class,
    )
    val arcs: List<CharacterArc> = emptyList(),
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

private data class DisplayNameMatch(
    val character: Character,
    val matchedTokenCount: Int,
    val allQueryTokensMatch: Boolean,
)

/**
 * Resolves a spoken or written name to a saga character (exact full name / nickname first,
 * then token-based fuzzy matching for chat and narrative references).
 *
 * Ambiguous matches (e.g. shared surnames) return null instead of picking an arbitrary character.
 */
fun List<Character>.findByDisplayName(query: String?): Character? {
    if (query.isNullOrBlank()) return null

    find { it.matchesDisplayNameStrict(query) }?.let { return it }

    val normalizedInputTokens =
        query
            .lowercase()
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
    if (normalizedInputTokens.isEmpty()) return null

    val candidates =
        mapNotNull { character ->
            val characterTokens = character.displayNameTokens()
            val matchedTokenCount = normalizedInputTokens.count { it in characterTokens }
            if (matchedTokenCount == 0) return@mapNotNull null
            DisplayNameMatch(
                character = character,
                matchedTokenCount = matchedTokenCount,
                allQueryTokensMatch = normalizedInputTokens.all { it in characterTokens },
            )
        }

    val fullQueryMatches = candidates.filter { it.allQueryTokensMatch }
    if (normalizedInputTokens.size > 1) {
        if (fullQueryMatches.isEmpty()) return null
        return fullQueryMatches
            .maxWithOrNull(
                compareBy<DisplayNameMatch> { it.matchedTokenCount }
                    .thenBy { -it.character.displayNameTokens().size },
            )?.character
    }

    val singleTokenMatches = candidates.filter { it.matchedTokenCount >= 1 }
    if (singleTokenMatches.size == 1) {
        return singleTokenMatches.first().character
    }

    return null
}
