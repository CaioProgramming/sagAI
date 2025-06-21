package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
import com.ilustris.sagai.features.characters.data.model.CharacterPose
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData

object CharacterPrompts {
    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    fun generateImage(
        character: Character,
        saga: SagaData,
    ) = """
        ${GenrePrompts.artStyle(saga.genre)}
        ${GenrePrompts.portraitStyle(saga.genre)}
        ${CharacterFraming.PORTRAIT.description}  
        Race: ${character.details.race}
        Gender: ${character.details.gender}
        Style: ${character.details.style}
        Occupation: ${character.details.occupation}
        Ethnicity: ${character.details.ethnicity}
        Height: ${character.details.height}
        Weight: ${character.details.weight}
        Appearance: ${character.details.appearance}
        Expression: ${CharacterExpression.random().description}
        Pose: ${CharacterPose.random().description}}
        """

    fun charactersOverview(characters: List<Character>): String =
        """
            CURRENT SAGA CAST (If a character is not listed here, they are considered new):
            This list is authoritative.
            If a character is NOT listed here, they are considered new and MUST be introduced with 'senderType': "NEW_CHARACTER" upon their first significant appearance or mention.
            ${characters.formatToJsonArray()}
            """

    fun characterGeneration(
        saga: SagaContent,
        description: String,
    ) = """
        Write a character description for a new character in the story.
        ${SagaPrompts.details(saga.data)}
        
        CURRENT Characters in story:
        // DO NOT CREATE A CHARACTER THAT ALREADY EXIST IN THIS LIST, DO NOT DUPLICATE NAMES NOR DESCRIPTIONS.
        // NEW CHARACTERS MUST BE UNIQUE AND RELATING TO THE STORY.
        ${charactersOverview(saga.characters)}

        use this message as a reference to get character details:
        // You MUST use the character's name, core appearance details, and personality hints from this message.
        // Invent reasonable and consistent details for any missing fields
        (like backstory, specific occupation, height, weight, race, hexColor, image).
        $description
        """
}
