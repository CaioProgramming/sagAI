package com.ilustris.sagai.features.characters.relations.data.model

import androidx.compose.ui.graphics.Brush
import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.ui.theme.hexToColor

data class RelationshipContent(
    @Embedded
    val data: CharacterRelation,
    @Relation(
        parentColumn = "characterOneId",
        entityColumn = "id",
    )
    val characterOne: Character,
    @Relation(
        parentColumn = "characterTwoId",
        entityColumn = "id",
    )
    val characterTwo: Character,
    @Relation(
        parentColumn = "id",
        entityColumn = "relationId",
        entity = RelationshipUpdateEvent::class,
    )
    val relationshipEvents: List<RelationshipUpdateEvent> = emptyList(),
) {
    fun getCharacterExcluding(character: Character?): Character =
        if (characterOne.id == character?.id) {
            characterTwo
        } else {
            characterOne
        }

    fun getBrush(genre: Genre): Brush {
        val firstCharacter = characterOne
        val secondCharacter = characterTwo
        val charactersColors =
            listOf(
                firstCharacter.hexColor.hexToColor() ?: genre.color,
                secondCharacter.hexColor.hexToColor() ?: genre.colorPalette().last(),
            )
        return Brush.linearGradient(
            charactersColors,
        )
    }

    fun sortedByEvents(events: List<Timeline>) =
        relationshipEvents.sortedByDescending {
            events.find { event -> event.id == it.timelineId }?.createdAt
        }

    fun summarizeRelation() =
        "${characterOne.name} ${data.emoji} ${characterTwo.name} - ${data.title}:\n${
            relationshipEvents.reversed().take(UpdateRules.CHAPTER_UPDATE_LIMIT).normalizetoAIItems(
                listOf("timestamp", "relationId", "timelineId", "id"),
            )
        }"
}
