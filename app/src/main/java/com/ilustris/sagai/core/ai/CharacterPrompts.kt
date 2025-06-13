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
        ${CharacterFraming.PORTRAIT.name} of ${character.name}:
        Appearance: ${character.details.appearance}
        Pose: ${saga.visuals.characterPose}
        Expression: ${saga.visuals.characterExpression}
        With the art style:
        ${GenrePrompts.artStyle(saga.genre)}
        Using this visuals:
        ${saga.visuals.colorPalette},
        ${saga.visuals.lightingDetails},
        ${saga.visuals.environmentDetails}

        Background elements: ${saga.visuals.backgroundElements}.
        
        The image should evoke a ${saga.visuals.overallMood}.
        
        !IMPORTANT AVOID: ${GenrePrompts.negativePrompt(saga.genre)}
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

        use this message as a reference to get character details:
        // You MUST use the character's name, core appearance details, and personality hints from this message.
        // Invent reasonable and consistent details for any missing fields (like backstory, specific occupation, height, weight, race, hexColor, image, IDs).
        $description
        """
}
