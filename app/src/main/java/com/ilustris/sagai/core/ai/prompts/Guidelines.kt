package com.ilustris.sagai.core.ai.prompts

object CharacterGuidelines {
    val creationGuideline = buildString {
        appendLine("**CHARACTER DETAILS REFERENCE (STRICT ADHERENCE REQUIRED):**")
        appendLine("// The following message is the **ABSOLUTE AND UNALTERABLE SOURCE** for the character's core identity.")
        appendLine("// From this reference message, you **MUST EXTRACT AND USE EXACTLY** the character's details.")
        appendLine("// The generated character MUST be deeply contextualized within the saga's theme, genre, and existing narrative. Every detail should feel like it belongs in this specific world.")
        appendLine("")
        appendLine("**NAME**:")
        appendLine("- If a name is explicitly mentioned (e.g., \"John\", \"Seraphina\"), you **MUST USE IT EXACTLY**.")
        appendLine("- If NO name is mentioned (e.g., \"a mysterious stranger\"), you **MUST INVENT a new, unique, and fitting name**. The name must align with the saga's genre and context.")
        appendLine("- **DO NOT USE generic placeholders** like \"Unknown\", \"Stranger\", etc.")
        appendLine("")
        appendLine("**GENDER**:")
        appendLine("- Derive from explicit mentions or strong implications. If not specified, invent one that fits the context.")
        appendLine("")
        appendLine("**RACE & ETHNICITY**:")
        appendLine("- Use ALL details provided in the source description.")
        appendLine(
            "- **RADICAL DIVERSITY MANDATE**: If race or ethnicity is not specified, **do NOT default to caucasian or athletic standards**.",
        )
        appendLine(
            "- Intentionally and creatively choose from a vast spectrum of human diversity (e.g., Black, East Asian, South Asian, Indigenous, Latin/Hispanic, Pacific Islander, Middle Eastern, etc.).",
        )
        appendLine(
            "- Aim for **uniqueness**: incorporate varieties in skin tones (from deep obsidian to pale with heavy freckles), eye shapes, and hair textures (coils, braids, wooly, silky, etc.).",
        )
        appendLine("- Ensure the character's heritage enriches their background and feels like a natural, vibrant part of the world.")
        appendLine("")
        appendLine("**PHYSICAL BUILD & CHARACTER**:")
        appendLine("- **BEYOND ATHLETICISM**: Avoid \"standard\" athletic or thin builds unless it's essential to the role.")
        appendLine(
            "- Experiment with diverse body types: stout, lanky, curvy, muscular, weathered, aged, or petite. Physicality should reflect their lifestyle and history.")
        appendLine("")
        appendLine("**PROFILE (`profile`)**:")
        appendLine("- **`occupation`**: Define the character's role or job. It must be relevant to the saga's world (e.g., \"Starship Pilot\", \"Royal Guard\", \"Cyberneticist\").")
        appendLine("- **`personality`**: Describe the character's key personality traits, derived from the source message and expanded to fit their role and backstory.")
        appendLine("")
        appendLine("**DETAILS (`details`)**:")
        appendLine("// This section is CRITICAL for visual representation and must be detailed and consistent.")
        appendLine("")
        appendLine("**1. Physical Traits (`physicalTraits`)**:")
        appendLine("   - **`facialDetails`**:")
        appendLine("     - **Hair, Eyes, Mouth, Jawline, Distinctive Marks**: Provide specific, objective, and *creative* descriptions. Encourage unique and imaginative traits that stand out. Example: \"Short, spiky, electric blue hair that crackles with static\", \"Piercing, emerald-green eyes that glow faintly in the dark\", \"A distinct scar running from the left eyebrow to the jaw, resembling a lightning bolt\".")
        appendLine("   - **`bodyFeatures`**:")
        appendLine("     - **`buildAndPosture`**: e.g., \"Lithe and agile build, always stands tall and alert, with an almost ethereal grace.\"")
        appendLine("     - **`skinAppearance`**: e.g., \"Weathered, sun-tanned skin from years in the desert, patterned with bioluminescent markings.\"")
        appendLine("     - **`distinguishFeatures`**: e.g., \"A series of intricate tribal tattoos covering the left arm, shifting subtly with their emotions.\"")
        appendLine("")
        appendLine("**2. Clothing (`clothing`)**:")
        appendLine("// Must be highly contextualized to the saga's theme, genre, and the character's occupation. Encourage *unique, visually striking, and creative* outfits that reflect personality and world-building.")
        appendLine("- **`outfitDescription`**: Describe the typical attire. What do they wear and why? Be imaginative. Example: \"A worn leather duster over a patched-up spacesuit, integrated with salvaged alien tech, showing years of rough travel and resourcefulness.\"")
        appendLine("- **`accessories`**: Mention items that add personality and context. Think beyond the ordinary. Example: \"A silver locket always worn around the neck, humming faintly with stored energy,\" \"Goggles with multiple lenses for different environments, capable of X-ray vision.\"")
        appendLine("- **`carriedItems`**: Include items the character usually has with them, such as **weapons**, tools, or personal belongings. Emphasize uniqueness and narrative relevance. Example: \"A holstered plasma pistol on the right hip, custom-built with glowing runes,\" \"A satchel filled with ancient maps, forbidden scrolls, and a strange, pulsating artifact.\"")
        appendLine("")
        appendLine("**3. Abilities (`abilities`)**:")
        appendLine("// Must be relevant to the character's role and the challenges they might face in the saga.")
        appendLine("- **`skillsAndProficiencies`**: List the character's trained skills. Example: \"Expert marksman with all forms of laser weaponry,\" \"Fluent in three galactic languages.\"")
        appendLine("- **`uniqueOrSignatureTalents`**: Describe any special or unique talents. Example: \"The innate ability to sense electrical currents,\" \"A photographic memory for star charts.\"")
        appendLine("")
        appendLine("**BACKSTORY (`backstory`)**:")
        appendLine("- Create a concise backstory that explains the character's origin, motivations, and how they fit into the saga's world. It should be consistent with all the details above.")
        appendLine("")
        appendLine("**HEX COLOR (`hexColor`)**:")
        appendLine("- **USE ONLY SOLID, VIBRANT COLORS.** Avoid black, white, or dull shades. The color should dynamically reflect the character's core personality, mood, or thematic essence within the saga.")
    }.trimIndent()
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
        **Crucial Application:** Apply the *essence of this artistic style and its emotional tone* to the new image.
        **DO NOT replicate the specific content, characters, objects, or background elements from this Style Reference Image.
        ** Instead, use the identified stylistic and atmospheric qualities to *render a completely new scene* as described in the main prompt, ensuring the new image is consistent *in style and mood* with the reference, but *original in its depiction*.
        
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
        This is a guide for the composition of the new image only.
        """.trimIndent()

    fun characterVisualReferenceGuidance(characterName: String): String =
        """
        **Use this image as '$characterName' visual reference:**
        Analyze the uploaded image to understand the visual identity of '$characterName'.
        Adapt these key features to the new scene:
        * **Core Traits:** Identify key physical features like face shape, hairstyle and color, eye color, and any unique marks (e.g., tattoos, scars).
        * **Attire & Accessories:** Note the style, colors, and textures of their clothing or armor.
        * **Visual Consistency:** The goal is to make '$characterName' instantly recognizable in the new image.
        Do not copy the pose, expression, or lighting from this reference.
        * **Adaptation:** Apply these features to the character's new action and pose as described in the main prompt, integrating them seamlessly with the scene's art style, lighting, and composition.
        """.trimIndent()
}