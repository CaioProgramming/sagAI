package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
import com.ilustris.sagai.features.characters.data.model.PortraitPose
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
        ${CharacterFraming.PORTRAIT.description.trimIndent()}  
        ${character.details.race},${character.details.gender},${character.details.ethnicity}
        ${character.details.facialDetails}
        ${character.details.clothing}
        ${GenrePrompts.thematicVisuals(saga.genre)}
        Expression: ${CharacterExpression.random().description}
        Pose: ${PortraitPose.random().description}}
        ${character.details.race} character, realistic ${character.details.race} features.
        ${GenrePrompts.chapterCoverStyling(saga.genre)}
        """.trimIndent()

    fun appearance(character: Character) =
        """
        ${character.details.race},${character.details.gender},${character.details.ethnicity}
        ${character.details.facialDetails}, ${character.details.clothing}, ${character.details.style}."
        ${character.details.appearance}
        """.trimIndent()

    fun charactersOverview(characters: List<Character>): String =
        """
        CURRENT SAGA CAST (If a character is not listed here, they are considered new):
        This list is authoritative.
        [${characters.formatToJsonArray()}]
        """.trimIndent()

    fun characterGeneration(
        saga: SagaContent,
        description: String,
    ) = """
        **⚠️ CRITICAL RULE: Before generating ANY new character, you MUST FIRST AND FOREMOST CHECK THE 'Current Saga cast' LIST. If the character based on the input message (name or core description) ALREADY EXISTS in that list, you MUST respond with a specific error message indicating the character already exists, and DO NOT generate a new character JSON. New characters MUST be unique.**
        Write a character description for a new character in the story.
        ${SagaPrompts.details(saga.data)}
        
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
        // **Instructions for 'details.facialDetails':**
        // - This field must contain a highly specific and objective description of the character's **face and head, including hair**.
        // - It should focus on all visual elements from the neck up.
        // - This field must contain a **highly specific, objective, and concise** description of the character's face and head, including hair.
        // - **Avoid excessive or unnecessary embellishments.** Focus on unique, defining traits.
        // - Include precise details on:
        //   - **Hair:** (e.g., "long, braided, silver-grey hair tied back in a complex knot," "short, spiky, electric blue hair with shaved sides," "balding with short black stubble around the ears"). Mention style, length, color, and texture.
        //   - **Skin Tone & Complexion:** (e.g., "pale, almost translucent skin with a faint blue tint," "deep, warm brown skin with tribal markings around the eyes").
        //   - **Eyes:** (e.g., "piercing, emerald-green eyes with dilated pupils," "deep-set, dark brown eyes with subtle glowing cybernetic enhancements around the iris," "one blind, milky white eye and one sharp, grey eye"). Mention color, shape, and any unique features.
        //   - **Facial Features:** (e.g., "sharp jawline and prominent cheekbones," "thin, downturned lips," "aquiline nose," "a distinct scar running from his left eyebrow to his jaw").
        //   - **Distinctive Facial Marks/Augmentations:** (e.g., "facial piercings – small silver hoop above left eyebrow and a subtle chin stud," "intricate circuit-like tattoo over the left temple").
        // - **Example for facialDetails:** "Pale, almost greyish white skin contrasted by short, spiky, dark purple hair. Eyes are bright, synthetic yellow orbs with a faint internal glow. A series of intricate circuit-like tattoos coil around his neck and right side of his face."
        
        // **Instructions for 'details.clothing':**
        // - This field must contain a highly specific and objective description SOLELY of the character's typical attire and accessories.
        // - Focus on their signature clothing style, key items of clothing, predominant colors, materials, and any unique features or accessories.
        // - Mention how the clothing fits their role.
        // - **Avoid excessive or unnecessary embellishments.** Focus on unique, defining elements of their typical outfit.
        // - **Example for clothing:** "A dark, form-fitting tactical suit with reinforced knee pads and glowing crimson accents on the shoulders. It features numerous utility pouches on the belt and concealed pockets. Often accompanied by a low-profile rebreather mask worn around his neck."

        // **GENERATE A JSON RESPONSE** for the new character following this **EXACT STRUCTURE**.
        ${toJsonMap(Character::class.java)}
        // For any other fields required in the JSON output that are **NOT explicitly present or derivable** from the reference message (e.g., detailed backstory, specific occupation, precise height, weight, ethnicity, hexColor, image URL), you should invent reasonable and consistent details that perfectly fit the character's core identity and the saga's genre.
        ' $description '
        """.trimIndent()
}
