package com.ilustris.sagai.features.characters.data.model

import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent

/**
 * A dedicated data model for the character details page.
 * Uses [CharacterSagaInfo] instead of the full [com.ilustris.sagai.features.home.data.model.Saga]
 * to avoid loading unnecessary columns (review, emotionalProfile, worldState, etc.).
 *
 * Assembled manually in the repository from [CharacterWithRelations] + [CharacterSagaInfo].
 */
data class CharacterDetailData(
    val character: Character,
    val sagaInfo: CharacterSagaInfo,
    val events: List<CharacterEventDetails> = emptyList(),
    val relationshipsAsFirst: List<RelationshipContent> = emptyList(),
    val relationshipsAsSecond: List<RelationshipContent> = emptyList(),
    val messageCount: Int = 0,
) {
    /**
     * Combines all relationships where this character is either the first or second participant.
     */
    val relationships: List<RelationshipContent>
        get() = (relationshipsAsFirst + relationshipsAsSecond).distinctBy { it.data.id }
}
