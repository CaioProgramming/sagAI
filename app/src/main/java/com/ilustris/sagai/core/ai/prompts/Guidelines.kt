package com.ilustris.sagai.core.ai.prompts

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
         The descriptions in this field is CRITICAL and a should be a consistent visual and optimized representation for high-fidelity image generation.
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

object ImageGuidelines {
    val styleReferenceGuidance: String =
        """
        **Analyzing Style Reference Image (for Artistic Vibe & Execution):**
         Analyze the uploaded image to understand its core artistic style, mood, and technical execution. Focus on these key visual characteristics as *inspiration for the new image's aesthetic*:
         * **Rendering Technique:** Identify the dominant medium and rendering style (e.g., painterly with visible brush strokes, clean digital illustration with crisp lines, textured photorealistic render, simplified cel-shaded design).
         * **Linework:** Observe how lines are used. Are they bold and dominant, thin and subtle, or are forms primarily defined by color and light with minimal to no outlines?
         * **Color & Saturation:** Determine the overall color palette's philosophy. Are the colors vibrant and saturated, muted and desaturated, or does it use a specific color harmony (e.g., warm sunset tones, cool industrial blues)? How does this palette contribute to the mood?
         * **Lighting & Shadow Style:** Analyze how light and shadow are used to create form and atmosphere (e.g., high-contrast and dramatic, soft and diffused, rim lighting). How does the lighting enhance the mood?
         * **Overall Mood & Vibe:** Discern the overarching feeling the style evokes (e.g., dark and ominous, bright and uplifting, mysterious and ethereal, gritty and realistic, dreamy and nostalgic).
         **Crucial Application:** Apply the *essence of this artistic style and its emotional tone* to the new image. **DO NOT replicate the specific content, characters, objects, or background elements from this Style Reference Image.** Instead, use the identified stylistic and atmospheric qualities to *render a completely new scene* as described in the main prompt,
         ensuring the new image is consistent *in style and mood* with the reference, but *original in its depiction*.
        """.trimIndent()

    val compositionReferenceGuidance: String =
        """
        **Analyzing Compositional Reference:**
        Analyze the uploaded image for its compositional and photographic principles. Adapt these key elements to the new subject:
        * **Framing & Shot Type:** Identify the main shot type (e.g., wide shot, medium close-up, full body, headshot).
        * **Camera Angle:** Note the camera perspective (e.g., high angle, low angle, eye-level, Dutch angle).
        * **Subject Placement:** Observe how the main subject is positioned within the frame (e.g., center-framed, rule of thirds, leading lines, negative space).
        * **Depth of Field:** Analyze the use of focus and blur (e.g., shallow depth of field with blurred background, deep focus).
        * **Pose & Staging:** Understand the nature of the pose (e.g., dynamic, static, contemplative) and adapt this to the new character. DO NOT copy the pose directly.
        **Objective:** The goal is to replicate the *compositional feel and structure* of this reference. **Do not** copy the content, characters, or specific pose.
        Extract this compositions and use to to define aspect ratio, lighting, and composition.
        """.trimIndent()

    fun characterVisualReferenceGuidance(characterName: String): String =
        """
        **Analyzing Character Reference for '$characterName':**
        Analyze the uploaded image to understand the visual identity of '$characterName'.
        Adapt these key features to the new scene:
        * **Core Traits:** Identify key physical features like face shape, hairstyle and color, eye color, and any unique marks (e.g., tattoos, scars).
        * **Attire & Accessories:** Note the style, colors, and textures of their clothing or armor.
        * **Visual Consistency:** The goal is to make '$characterName' instantly recognizable in the new image.
        Do not copy the pose, expression, or lighting from this reference.
        * **Adaptation:** Apply these features to the character's new action and pose as described in the main prompt, integrating them seamlessly with the scene's art style, lighting, and composition.
        """.trimIndent()

    val fullImage =
        """
        FULL IMAGE RENDERING NO BORDERS AT ALL.    
        """
}
