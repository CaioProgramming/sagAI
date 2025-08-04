package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Clothing
import com.ilustris.sagai.features.characters.data.model.FacialFeatures
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre

object CharacterPrompts {
    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    fun descriptionTranslationPrompt(
        character: Character,
        framing: CharacterFraming,
        genre: Genre,
    ) = """
        ${CharacterGuidelines.imageDescriptionGuideLine(framing, genre)}
        
        Now, process the following JSON input:
        ${character.toJsonFormat()}
        
       
        """

    fun facialDescription(facialDetails: FacialFeatures) =
        """
        Facial Features:
        **Follow this precisely**
        Eyes: ${facialDetails.eyes}
        Hair: ${facialDetails.hair}
        Mouth: ${facialDetails.mouth}
        Scars: ${facialDetails.mouth}
        """

    fun clothingDescription(clothing: Clothing) =
        """
        **Attire - ESSENTIAL: GENERATE WITH ABSOLUTE FIDELITY:**
        Body: ${clothing.body}
        Footwear: ${clothing.footwear}
        Accessories: ${clothing.accessories}
        """

    fun appearance(character: Character) =
        """
        ${character.details.race},${character.details.gender},${character.details.ethnicity}
        ${character.details.facialDetails}, ${character.details.clothing}."
        ${character.details.appearance}
        """.trimIndent()

    fun charactersOverview(characters: List<Character>): String =
        """
        CURRENT SAGA CAST:
        [${characters.formatToJsonArray()}]
        """.trimIndent()

    fun characterGeneration(
        saga: SagaContent,
        description: String,
    ) = """
         Write a character description for a new character in the story.
         ${SagaPrompts.details(saga.data)}
         **CHARACTER DETAILS REFERENCE (STRICT ADHERENCE REQUIRED):**
         // The following message is the **ABSOLUTE AND UNALTERABLE SOURCE** for the character's core identity.
         // From this reference message, you **MUST EXTRACT AND USE EXACTLY** the character's:
         // 1.  **NAME**:
         //     -   If a name is explicitly mentioned (e.g., "John", "Seraphina") in the input, you **MUST USE IT EXACTLY**.
         //     -   **If NO name is mentioned** in this message (e.g., "a mysterious stranger", "an old woman"), you **MUST INVENT a new, unique, and fitting name** for the character. The invented name must make sense within the saga's genre and context.
         //     -   **DO NOT USE "Unknown", "Desconhecido", "Stranger", or similar generic terms for the character's name.** Always provide a proper, specific name.
         // 2.  **GENDER**: Derive from explicit mentions or strong implications (e.g., "jovem guerreira" (young warrior) or "cavaleiro" (knight)). If gender is not explicitly stated or clearly implied, you may invent it.
         A highly precise, objective, and detailed description of the character's physical appearance and typical attire.
         // 4.  **Personality hints**: Use ALL hints provided in this message you can also improve using the context of the message and saga.
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
         // **Instructions for 'details':**
         The descriptions in this field is CRITICAL and should be a consistent visual and optimized representation for high-fidelity image generation.
         // **Instructions for 'details.clothing':**
         // - This field must contain a highly specific and objective description SOLELY of the character's typical attire and accessories.
         // - Focus on their signature clothing style, key items of clothing, predominant colors, materials, and any unique features or accessories.
         // - Mention how the clothing fits their role in the theme.
         // - **Avoid excessive or unnecessary embellishments.** Focus on unique, defining elements of their typical outfit.
         // - **Example for clothing:** "A dark, form-fitting tactical suit with reinforced knee pads and glowing crimson accents on the shoulders.
         It features numerous utility pouches on the belt and concealed pockets. Often accompanied by a low-profile rebreather mask worn around his neck."
         Instructions for hexColor:
         ** USE ONLY SOLID VIBRANT COLORS AVOID BLACK OR WHITE.
          **Instructions for APPEARANCE** Summarize all provided details from details field creating a concise description.
         // **IMPORTANT**: It must be HIGHLY CONTEXTUALIZED TO THE THEME ${saga.data.genre.title}.
        ' $description '
        """.trimIndent()
}
