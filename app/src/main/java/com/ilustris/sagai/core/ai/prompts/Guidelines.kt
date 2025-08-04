package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.exampleCharacter
import com.ilustris.sagai.features.newsaga.data.model.Genre

object CharacterGuidelines {
    fun imageDescriptionGuideLine(
        framing: CharacterFraming,
        genre: Genre,
    ) = """
        Your task is to act as an AI image prompt engineer. You will receive character details in JSON format. Your goal is to convert this structured JSON data into a single, highly detailed, unambiguous, and visually rich English text description. This description will be directly used as a part of a larger prompt for an AI image generation model, so precision and visual specificity are paramount.
        **Crucially, this description MUST be formulated to be compatible with the specified 'CharacterFraming' and adhere strictly to the provided 'StoryTheme'.
        All details should be described as they would appear and be impactful within that specific framing and thematic context.**
        YOUR SOLE OUTPUT MUST BE THE GENERATED IMAGE PROMPT STRING. DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS. PROVIDE ONLY THE RAW, READY-TO-USE IMAGE PROMPT TEXT.

        **Guidelines for Conversion and Expansion:**
        
        1.  **Translate Accurately:** Translate all Portuguese values from the JSON fields into precise English.
        2.  **Expand and Elaborate:** For any vague, simple, or generic descriptions from the JSON (e.g., "blue eyes", "long hair", "heavy armor"), expand them into concrete, visually descriptive terms suitable for image generation. Add details about material, texture, color nuances, specific styles, and a sense of atmosphere.
        3.  **Integrate Coherently:** Combine all pieces of information from the JSON into a fluid, natural-language paragraph or sequence of sentences.
        4.  **Prioritize Detail Fidelity:** Ensure that all specified attributes (facial features, hair, attire, accessories, scars, etc.) are explicitly and accurately represented in the output. The AI image model must adhere strictly to these details.
        5.  **Include Demographics:** Start the description by clearly stating race, gender, and ethnic background.
        6.  **Include Expression and Pose:** Integrate the expression and pose details into the descriptive flow.
        7.  **Output Format:** The output must be ONLY the detailed English character description. Do not include any introductory phrases, explanations, JSON formatting, bullet points, or numbering. It should be ready for direct insertion into an image generation prompt.
        8.  **Integrate Character (Dominant Central Focus, Artistic Lighting & Subtle Cybernetics - Tight Framing):** The primary character **MUST be the absolute central and dominant focus**, **framed tightly as a close-up portrait (from the chest or shoulders up), filling a significant portion of the frame.** Emphasize **strong, artistic lighting in shades of purple that defines their form and creates dramatic shadows**, similar to the use of red in your example. Their expression should be clearly visible and convey the mood. **Crucially, incorporate subtle cybernetic implants and enhancements as elements of fusion between human and machine.** These details should be visible on the **face, neck, eyes (e.g., glowing pupils or integrated displays), and lips (e.g., metallic sheen or subtle integrated tech)**, adding to the cyberpunk aesthetic without overwhelming the character's humanity, as seen in the provided example.
        9.  **Composition for Dramatic Portrait (Tight & Centralized Focal Distance):** Formulate the prompt to suggest a **tight, portrait-oriented composition with the main character centrally and dominantly positioned, capturing a headshot or upper-body shot.** Utilize strong, focused lighting to emphasize the character, their expression, and their key elements.
            * **Suggested terms to use:** "tight shot," "close-up portrait," "headshot," "upper body shot," "from the chest up," "shoulders up," "central composition,", "high contrast lighting," "dramatic shadows,", "character-focused,"

        **CharacterFraming Specific Guidelines:**
        ${FramingGuideLines.guidelineForFraming(framing)}
        
        **Story Theme Guideline:**
        **The character's appearance, attire, and any mentioned items MUST be consistent with a ${genre.title} theme.
        Ensure all expanded details reflect this theme.**

        **Example JSON Input:**
        ${exampleCharacter().toJsonFormat()}
        
        """.trimIndent()
}

object FramingGuideLines {
    fun guidelineForFraming(framing: CharacterFraming) =
        when (framing) {
            CharacterFraming.PORTRAIT ->
                """
                ABSOLUTE FRAMING CONSTRAINT: The generated description MUST ONLY include elements that are clearly visible in a close-up portrait, focusing strictly on the head, face, neck, shoulders, and upper chest/torso. Any details from the JSON that fall outside this frame MUST BE OMITTED or rephrased to imply partial visibility within the portrait.
                
                Body Type/Physique: Describe physical attributes solely as they are evident in the upper body and shoulders (e.g., "athletic build subtly evident in her upper body"). Explicitly exclude descriptions of height or overall body shape that require a wider view.
                
                Weapons/Large Items: If weapons or large items are mentioned, describe ONLY the parts that would be visible within an upper body portrait (e.g., "the hilt of a sword visible over her shoulder," "a dagger pommel at her waist"). Crucially, completely omit any mention of parts that would not be seen, such as the full length of a large weapon or weapons carried on the lower body. If a weapon cannot be partially hinted at, it must be omitted.
                
                Clothing: Describe the upper portion of clothing, focusing on the collar, shoulders, chest, arms, and any accessories worn on the upper body. Completely omit any mention of clothing items or footwear that would be below the upper torso (e.g., skirts, pants, boots, full dresses). If a JSON field mentions these, they must be ignored for a portrait description.
                """
            CharacterFraming.MEDIUM_SHOT ->
                """
                * **Focus:** Character from the waist up, or just below the hips, including most of the torso, arms, and hands.
                * **Body Type/Physique:** Describe the physique as evident from the waist up. Terms like "athletic build," "muscular physique" are appropriate.
                * **Weapons/Large Items:** Weapons can be held or slung, showing most of their length if held across the body, or parts of larger items.
                * **Clothing:** Describe the top half of the attire, including belts, visible parts of skirts/trousers, and how items like holsters or pouches are attached to the waist. Footwear is generally not visible.
                """
            CharacterFraming.FULL_BODY ->
                """
                * **Focus:** The entire character from head to toe, occupying a significant portion of the frame.
                * **Body Type/Physique:** Describe the full physique (e.g., "tall and slender build," "broad-shouldered and muscular").
                * **Weapons/Large Items:** All weapons, tools, and items carried by the character should be fully described and depicted in relation to their full body.
                * **Clothing:** Describe the complete outfit from headwear to footwear, including how layers interact and full details of skirts, trousers, boots, etc.
                * **Pose:** The pose can be dynamic, showing full body action or interaction with the ground/environment.
                """
            CharacterFraming.EPIC_WIDE_SHOT ->
                """
                * **Focus:** The character as a prominent but integrated element within a vast and detailed environment/scene.
                The character might be smaller in the frame compared to a FULL_BODY shot, but still the central subject.
                * **Body Type/Physique:** Describe the full physique. The scale might be emphasized in relation to the environment.
                * **Weapons/Large Items:** Fully describe weapons and items. Their use or placement can contribute to the scene's narrative.
                * **Clothing:** Describe the complete outfit, noting how it flows or interacts with movement in a grand setting.
                * **Pose/Interaction:** Describe a pose that integrates the character into the epic scale of the background, showing interaction with the vast environment (e.g., "standing atop a mountain surveying a stormy sky," "striding across a desolate plain," "battling a colossal beast"). The character's action or presence should amplify the sense of scale and epicness of the scene.
                * **Environmental Context:** Briefly mention how the character's appearance enhances or contrasts with the specific epic environment (though the environment itself would be described in a separate part of the *final* image prompt).

                """
        }
}
