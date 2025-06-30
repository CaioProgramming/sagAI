package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
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
        ${character.details.race}, ${character.details.gender}, ${character.details.ethnicity}
        Appearance: ${character.details.appearance}
        ${GenrePrompts.thematicVisuals(saga.genre)}
        Expression: ${CharacterExpression.random().description}
        """

    fun appearance(character: Character) =
        """
        ${character.details.race},${character.details.gender},${character.details.ethnicity}
        ${character.details.appearance}."
        """

    fun charactersOverview(characters: List<Character>): String =
        """
            CURRENT SAGA CAST (If a character is not listed here, they are considered new):
            This list is authoritative.
            If a character is NOT listed here, they are considered new and MUST be introduced with 'senderType': "NEW_CHARACTER" upon their first significant appearance or mention.
            [ ${characters.formatToJsonArray()} ]
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

        **CHARACTER DETAILS REFERENCE (STRICT ADHERENCE REQUIRED):**
        // The following message is the **ABSOLUTE AND UNALTERABLE SOURCE** for the character's core identity.
        // From this reference message, you **MUST EXTRACT AND USE EXACTLY** the character's:
        // 1.  **NAME**:
        //     -   If a name is explicitly mentioned (e.g., "John", "Seraphina") in this message, you **MUST USE IT EXACTLY**.
        //     -   **If NO name is mentioned** in this message (e.g., "a mysterious stranger", "an old woman"), you **MUST INVENT a new, unique, and fitting name** for the character. The invented name must make sense within the saga's genre and context.
        //     -   **DO NOT USE "Unknown", "Desconhecido", "Stranger", or similar generic terms for the character's name.** Always provide a proper, specific name.
        // 2.  **GENDER**: Derive from explicit mentions or strong implications (e.g., "jovem guerreira" (young warrior) or "cavaleiro" (knight)). If gender is not explicitly stated or clearly implied, you may invent it.
        // 3.  **Core APPEARANCE DETAILS** (e.g., facial features, hair, clothing, equipment): Use ALL details provided in this message.
        // **IMPORTANT**: Appareance description needs to be: 
        HIGHLY CONTEXTUALIZED TO THE THEME ${saga.data.genre.title}.
        A highly precise, objective, and detailed description of the character's physical appearance and typical attire.
        This description is CRITICAL for consistent visual representation across all chapters and for high-fidelity image generation.
        // 4.  **Personality hints**: Use ALL hints provided in this message.
        // 5. **RACE**: 
         - Use ALL details provided in this message.
         - **If no race is specified assume that its a human**
        // 6. **Ethnicity**: Use ALL details provided in this message.
           - Use ALL details provided in this message.
           - **If no ethnicity is specified use a random etnicity(caucasian, black, asian, latin)**
        // **GENERATE A JSON RESPONSE** for the new character following this **EXACT STRUCTURE**.
        ${toJsonMap(Character::class.java)}
        // For any other fields required in the JSON output that are **NOT explicitly present or derivable** from the reference message (e.g., detailed backstory, specific occupation, precise height, weight, ethnicity, hexColor, image URL), you should invent reasonable and consistent details that perfectly fit the character's core identity and the saga's genre.
        ' $description '
        """
}
