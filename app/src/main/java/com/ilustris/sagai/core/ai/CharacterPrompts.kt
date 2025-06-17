package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData

object CharacterPrompts {
    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    fun generateImage(
        character: Character,
        saga: SagaData,
    ) = """
        ${GenrePrompts.artStyle(saga.genre)}
        ${CharacterFraming.PORTRAIT.description}  
        Race: ${character.details.race}
        Gender: ${character.details.gender}
        Style: ${character.details.style}
        Occupation: ${character.details.occupation}
        Ethnicity: ${character.details.ethnicity}
        Height: ${character.details.height}
        Weight: ${character.details.weight}
        Appearance: ${character.details.appearance}
        Use a expression that matches character personality: ${character.details.personality}
        """

    fun charactersOverview(characters: List<Character>): String =
        """
            CURRENT SAGA CAST (If a character is not listed here, they are considered new):
             This list is authoritative.
             If a character is NOT listed here, they are considered new and MUST be introduced with 'senderType': "NEW_CHARACTER" upon their first significant appearance or mention.
            ${characters.map { it.toJsonFormat() }}
            """

    fun characterGeneration(
        saga: SagaData,
        description: String,
    ) = """
        Write a character description for a new character in the story.
        ${SagaPrompts.details(saga)}

        use this messages as a reference to get character details:
        $description
        use this message as a reference to get character details:
        // **CRITICAL INSTRUCTION:** You MUST use the character's name provided in the conversation context. DO NOT invent a new name.
        // **You MUST also** use the core appearance details and any personality hints from this message.
        // Invent reasonable and consistent details for any missing fields (like backstory, specific occupation, height, weight, race, hexColor, image, IDs).
        """
}
