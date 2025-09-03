package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.ai.prompts.GenrePrompts.artStyle
import com.ilustris.sagai.core.ai.prompts.GenrePrompts.moodDescription
import com.ilustris.sagai.features.newsaga.data.model.Genre

object CharacterGuidelines {
    val creationGuideline =
        """
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

object ChapterCoverGuideline {
    fun guidelinesForCover(genre: Genre) =
        """
        *CRITICAL RULES FOR CONVERSION:**
                
                // Section 1: Artistic Style - Highest Priority
                ${artStyle(genre)}
                
                // Section 2: Exclusions - High Priority
                no text,
                no words,
                no typography,
                no letters,
                no UI elements.
                
                // Section 3: Core Content & Details
                1. Translate Accurately:
                   Translate all Portuguese values from the input fields into precise English.
                
                2. Infer Visuals from Summary:
                   From the Chapter Summary/Description, infer and elaborate on:
                   - Dominant Mood/Atmosphere & Lighting Theme: ${moodDescription(genre)}.
                
                3. Integrate Main Characters:
                   If characters are listed, integrate them visually into the scene.
                   The main character(s) MUST be the absolute central and dominant focus.
                
                4. Thematic Consistency:
                   Ensure all generated visual descriptions align with the ${genre.name} theme.  
             
                ${artStyle(genre)}
        """
}

object ImageGuidelines {
    val styleReferenceGuidance: String = """
        **Interpreting the Style Reference Image (Context, Not Replication):**
        This image serves as a primary inspiration for the *overall artistic style and aesthetic execution*. When generating the new image, analyze and draw from this reference's:
        *   **Artistic Medium/Technique:** Identify the core technique (e.g., oil painting, watercolor, digital illustration, cel-shaded anime, photorealistic 3D render, pixel art, charcoal sketch, cinematic photography).
        *   **Texture & Detail Level:** Note the surface qualities and amount of fine detail (e.g., gritty and textured, smooth and polished, painterly strokes, highly detailed, impressionistic and abstract).
        *   **Color Palette Philosophy:** Understand the approach to color (e.g., vibrant and saturated, muted and desaturated, monochromatic, specific dominant hues, harmonious or contrasting).
        *   **Lighting & Shadow Style:** Observe how light and shadow are used to create form and mood (e.g., dramatic chiaroscuro, soft and diffused, stark and high-contrast, specific light source types implied, rim lighting).
        *   **Overall Mood & Atmosphere:** Discern the feeling the style evokes (e.g., dark and ominous, bright and uplifting, mysterious and ethereal, epic and grand, gritty and realistic).
        **Crucial:** The goal is to capture the *essence* of this artistic style and apply it to the *new, distinct subject* of your current task. **DO NOT replicate the specific content, characters, or scene elements from this Style Reference Image.** It guides *how* the new image is rendered, not *what* it depicts beyond the subject you are tasked to create.
    """.trimIndent()

    val compositionReferenceGuidance: String = """
        **Interpreting the Composition Reference Image (Inspiration, Not Replication):**
        This image provides inspiration for the *compositional arrangement, framing, and visual structure*. Focus on and adapt these underlying principles:
        *   **Subject Framing:** Note how the main subject is framed (e.g., close-up, extreme close-up, medium shot, cowboy shot, full body, wide shot showing environment).
        *   **Camera Angle & Perspective:** Consider the viewpoint (e.g., eye-level, low angle looking up, high angle looking down, bird's-eye view, Dutch angle).
        *   **Subject Placement & Balance:** Analyze how elements are arranged within the frame (e.g., rule of thirds, golden ratio, central focus, leading lines, use of negative space, symmetry or asymmetry).
        *   **Depth of Field & Focus:** Observe the use of focus (e.g., shallow depth of field with a blurred background (bokeh), deep focus with all elements sharp, selective focus).
        *   **Pose & Staging Cues (for characters/subjects, if applicable):** If this reference contains figures, look at their poses, gestures, and interaction with the space as *inspiration only*. **DO NOT directly copy poses.** Instead, understand the *type* of pose (e.g., dynamic action, static and powerful, contemplative, interactive) and adapt this concept to the new character and their specific context and narrative needs for a fresh, original pose.
        *   **Visual Flow & Leading Lines:** How does the composition guide the viewer's eye?
        **Crucial:** Adapt these compositional principles to the *new subject and scene* of your current task. The aim is to achieve a similar structural feel or solve a similar compositional challenge, **not to directly replicate the layout, subject matter, or specific pose from this Composition Reference Image.** It guides *how* the scene is structured, not *what specific elements or poses* must be present.
    """.trimIndent()

    fun characterVisualReferenceGuidance(characterName: String): String = """
        **Interpreting the Visual Reference for '$characterName' (Inspiration for Appearance, Not Direct Copy):**
        This image is the primary visual blueprint for '$characterName'. Use it to understand and then creatively adapt:
        *   **Key Physical Features:** Note their facial structure, hairstyle and color, eye color, build (as visible), skin tone, and any distinctive marks like scars, tattoos, or unique racial traits for '$characterName'.
        *   **Signature Attire & Accessories:** Identify the style, cut, colors, and textures of '$characterName''s typical clothing, armor, or any notable accessories they wear.
        *   **Overall Visual Identity:** Get a sense of '$characterName''s established look and feel.

        **Crucial Application:**
        *   **Adapt, Don't Copy-Paste:** Your goal is to *re-interpret* these visual details for '$characterName' and render them within the target art style specified by a Style Reference or the main prompt. Do not aim for a pixel-perfect replication of this reference image.
        *   **Integrate with Context:** Seamlessly blend '$characterName''s appearance with the specific pose, expression, action, and lighting described in the main text prompt or inspired by a Composition Reference.
        *   **Maintain Recognizability:** While adapting, ensure '$characterName' remains clearly recognizable based on their core visual traits from this reference.
        *   **Flexibility for Narrative:** If the prompt describes a change in attire for '$characterName' or a new temporary feature (e.g., battle damage, a disguise), prioritize the prompt's description while keeping '$characterName''s base appearance consistent where appropriate.
        This image defines *who* '$characterName' is visually. Your task is to bring that visual identity to life authentically within the new scene's specific requirements.
    """.trimIndent()
}
