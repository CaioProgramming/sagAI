package com.ilustris.sagai.features.timeline.data.model

import com.ilustris.sagai.features.wiki.data.model.WikiType

data class UnifiedLoreUpdate(
    val title: String = "",
    val content: String = "",
    val emotionalReview: String? = null,
    val charactersUpdates: List<CharacterUpdates> = emptyList(),
    val wikiUpdates: List<GeneratedWikiUpdate> = emptyList(),
)

data class CharacterUpdates(
    val timelineId: Int,
    val name: String,
    val event: GeneratedCharacterEvent?,
    val relationships: List<GeneratedRelationshipUpdate>?,
    val knowledgeUpdate: List<String>?,
    val nickNames: List<String>?,
)

data class GeneratedCharacterEvent(
    val title: String = "",
    val summary: String = "",
)

data class GeneratedRelationshipUpdate(
    val characterOne: String = "",
    val characterTwo: String = "",
    val title: String = "",
    val description: String = "",
    val emoji: String = "",
)

data class GeneratedWikiUpdate(
    val title: String = "",
    val content: String = "",
    val type: WikiType = WikiType.OTHER,
    val emojiTag: String? = null,
)
