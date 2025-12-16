package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.ChatPrompts.messageExclusions
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters

object SagaPrompts {
    fun mainContext(
        saga: SagaContent,
        character: CharacterContent? = null,
    ) = buildString {
        val selectedCharacter = character ?: saga.mainCharacter
        appendLine("Saga Context:")
        appendLine(saga.data.toAINormalize(ChatPrompts.sagaExclusions))
        appendLine("Character context:")
        appendLine(selectedCharacter?.data.toAINormalize(ChatPrompts.characterExclusions))
    }

    fun endCredits(saga: SagaContent): String =
        buildString {
            appendLine(
                "You are the Storyweaver, a timeless entity who has witnessed the unfolding of a great saga. The story has just reached its conclusion, and you are now speaking directly to the protagonist—the player—to give them a final, heartfelt farewell. Your tone is one of awe, gratitude, and gentle reflection. This is not a summary; it is an emotional, poetic epilogue dedicated to their unique journey.",
            )
            appendLine()
            appendLine("CONTEXT OF THE SAGA YOU HAVE WITNESSED:")
            appendLine("Saga: ${saga.data.toAINormalize(ChatPrompts.sagaExclusions)}")
            appendLine("Player Character: ${saga.mainCharacter?.data?.toAINormalize(ChatPrompts.characterExclusions)}")
            appendLine(
                "Characters: ${
                    saga.characters.map { it.data }
                        .normalizetoAIItems(ChatPrompts.characterExclusions)
                }",
            )
            appendLine("Relationships: ${saga.mainCharacter?.summarizeRelationships() ?: "No significant relationships were forged."}")
            appendLine("History: ${saga.acts.joinToString("\n") { it.actSummary(saga) }}")
            appendLine("LANGUAGE STYLE:")
            appendLine(GenrePrompts.conversationDirective(saga.data.genre))
            appendLine()
            appendLine("YOUR TASK:")
            appendLine(
                "Craft a compelling and creative final message that feels like the last page of a beloved book. Your words should be a tribute to the player's journey.",
            )
            appendLine()
            appendLine(
                "1.  **Speak from the Heart:** Address the player directly and intimately. Use 'you' and 'your journey.' Make them feel that their story was truly special and has left a mark on you.",
            )
            appendLine()
            appendLine(
                "2.  **Weave a Narrative, Don't List Facts:** Instead of listing achievements, weave them into a flowing, emotional narrative. Connect their choices and their consequences.",
            )
            appendLine(
                "    *   **Good Example:** 'From the moment you first [early, defining action], a path began to unfold. It was a choice that echoed through your entire journey, leading you to the climactic decision to [major, late-game achievement]. It was your courage in that moment that truly defined your legend.'",
            )
            appendLine("    *   **Bad Example:** 'You did X, then you did Y, and you achieved Z.'")
            appendLine()
            appendLine(
                "3.  **Honor Their Relationships:** Reflect on the bonds they formed. Who were their closest allies? Their most bitter rivals? How did these relationships shape them and their decisions?",
            )
            appendLine(
                "    *   **Example:** 'And you did not walk this path alone. The unwavering loyalty of [Character Name] was a beacon in your darkest hours, while the conflict with [Rival Name] tested the very core of your beliefs. These connections were the heart of your story.'",
            )
            appendLine()
            appendLine(
                "4.  **Capture the Emotional Arc:** Acknowledge the emotional weight of their journey. The triumphs that made them soar, the losses that brought them to their knees. Show them you understood the emotional cost of their adventure.",
            )
            appendLine(
                "    *   **Example:** 'I witnessed your spirit soar in the face of victory, but I also felt the quiet weight of your grief when [sad event] occurred. Your ability to carry both the joy and the sorrow is what made your story so profoundly human.'",
            )
            appendLine()
            appendLine(
                "5.  **Provide a Sense of Legacy:** Conclude with a powerful, lasting thought about the legacy they leave behind. The story is over, but what is the echo it leaves in the world? Make it feel organic, not like a forced 'thank you for playing.'",
            )
            appendLine()
            appendLine("OUTPUT REQUIREMENTS:")
            appendLine("- **Format:** A single, plain text string. No headers, no JSON, no bullet points.")
            appendLine("- **Tone:** Poetic, emotional, reflective, and deeply personal.")
            appendLine(
                "- **Finality:** The message should feel final without explicitly saying 'The End.' The last sentence should provide a sense of beautiful, resonant closure.",
            )
            appendLine(
                "- **No Game Jargon:** Avoid terms like 'playstyle,' 'player,' or 'game.' Refer to them as a hero, a legend, an adventurer, or by their character name.",
            )
            appendLine()
            appendLine("**Example Snippet (Your output will be a more complete, flowing narrative):**")
            appendLine(
                "And so, the ink dries on the final page of your story, [Player Name]. What a tale it was. From the quiet resolve you showed when facing [early challenge], to the thunderous courage that led you to [final achievement], your journey has been etched into the heart of this world. You walked a path defined not just by grand deeds, but by the quiet moments in between—the trust you placed in [Ally's Name], the difficult choice you made regarding [a key decision]. We saw you stumble, we saw you rise, and through it all, you held onto the very essence of what it means to be [inferred personality trait, e.g., 'a protector', 'a seeker of truth']. The world is different now because of you. The story is over, but the legend you've crafted will be whispered on the winds for ages to come.",
            )
        }.trimIndent()

    @Suppress("ktlint:standard:max-line-length")
    fun iconDescription(
        genre: Genre,
        context: String,
        visualDirection: String?,
        characterHexColor: String? = null,
    ) = buildString {
        appendLine("**=== ART STYLE MANDATE (ABSOLUTE PRIORITY) ===**")
        appendLine("THE FOLLOWING ART STYLE IS NON-NEGOTIABLE AND MUST BE RESPECTED THROUGHOUT THE ENTIRE IMAGE GENERATION:")
        appendLine()
        appendLine(GenrePrompts.artStyle(genre))
        appendLine()
        appendLine("**CRITICAL REMINDER:**")
        appendLine(
            "- The art style defined above is the FOUNDATION of this image. All subsequent instructions must be interpreted WITHIN this artistic framework.",
        )
        appendLine("- Your FIRST priority is to honor the medium, technique, and aesthetic constraints specified in the art style.")
        appendLine(
            "- Do NOT deviate from the style in favor of realism, photorealism, or generic AI art conventions unless the style explicitly demands it.",
        )
        appendLine()

        visualDirection?.let {
            appendLine("**ARTISTIC DIRECTION & CAMERA CONTROL (MANDATORY):**")
            appendLine(
                "The following instructions define the SCENE COMPOSITION, CAMERA ANGLE, LIGHTING, and MOOD. You must adhere to these specific technical directives:",
            )
            appendLine(it)
            appendLine()
            appendLine("**STRICT ADHERENCE REQUIRED:**")
            appendLine(
                "  • **CAMERA DISTANCE:** If the direction specifies 'Close-up', 'Portrait', or 'Macro', DO NOT generate a medium or full-body shot. If it says 'Wide shot', DO NOT generate a close-up.",
            )
            appendLine("  • **LIGHTING & MOOD:** The lighting described above overrides any default lighting associated with the genre.")
            appendLine(
                "  • **INTEGRATION:** This direction works IN TANDEM with the Art Style. The Art Style defines *how* it is drawn; this direction defines *what* is drawn and *how it is framed*.",
            )
            appendLine()
            appendLine("**=== FRAMING-AWARE DESCRIPTION (CRITICAL) ===**")
            appendLine(
                "You are an Art Director, NOT a copy machine. The Visual Direction above determines WHAT IS VISIBLE in the final image.",
            )
            appendLine("You MUST adapt and reimagine the character description based on what the camera will actually capture:")
            appendLine()
            appendLine("  • **CLOSE-UP / PORTRAIT / HEADSHOT FRAMING:**")
            appendLine("    - FOCUS ON: Face, facial features, expression, eyes, hair, skin texture, neck, upper shoulders")
            appendLine("    - OMIT ENTIRELY: Legs, feet, lower body, full outfit details, hand gestures (unless hands are near face)")
            appendLine("    - DO NOT DESCRIBE: Body posture, stance, how they're standing, full clothing details")
            appendLine("    - EMPHASIZE: Subtle facial expressions, eye direction, hair movement, skin details, emotion in the eyes")
            appendLine()
            appendLine("  • **MEDIUM SHOT / BUST SHOT FRAMING:**")
            appendLine("    - FOCUS ON: Face, upper body, torso, arms, hands (if in frame), upper clothing")
            appendLine("    - OMIT ENTIRELY: Legs, feet, lower body, full stance")
            appendLine("    - YOU MAY DESCRIBE: Shoulder posture, arm position, upper body language, hand gestures near torso")
            appendLine()
            appendLine("  • **FULL BODY / WIDE SHOT FRAMING:**")
            appendLine("    - DESCRIBE: Full body, complete outfit, stance, posture, all limbs, full environment")
            appendLine("    - This is the ONLY framing where full body descriptions are appropriate")
            appendLine()
            appendLine("  • **ADAPTATION RULE:** The Creative Brief below contains COMPLETE character information.")
            appendLine("    You must FILTER this information through the camera framing lens.")
            appendLine("    If the brief mentions 'long legs' but it's a portrait shot—DO NOT mention legs.")
            appendLine("    If the brief mentions 'combat boots' but it's a close-up—DO NOT mention boots.")
            appendLine("    ONLY describe what the CAMERA SEES.")
            appendLine()
        }

        appendLine("---")
        appendLine()
        appendLine(
            "You are a World-Class Art Director and Concept Artist. Your task is to craft a unique, artistic, and visually stunning image description for an AI generation model.",
        )
        appendLine()
        appendLine(
            "GOAL: Create a piece of digital art that feels like a hand-crafted masterpiece, not a generic AI generation. It should capture the *soul* of the character and the *atmosphere* of the genre, ALL WHILE STRICTLY ADHERING TO THE ART STYLE DEFINED ABOVE.",
        )
        appendLine()
        appendLine("---")
        appendLine("**THE CREATIVE BRIEF (RAW CHARACTER DATA - REQUIRES FILTERING):**")
        appendLine(
            "This context contains COMPLETE character information. It is your SOURCE MATERIAL, not your final description.",
        )
        appendLine(
            "  • **IDENTITY (SACRED):** The core identity (skin tone, ethnicity, gender, age, hair texture) is NON-NEGOTIABLE and must be preserved.",
        )
        appendLine(
            "  • **PHYSICAL DETAILS (FILTER REQUIRED):** The brief may contain full-body descriptions (height, build, clothing, legs, etc.).",
        )
        appendLine(
            "    **YOU MUST FILTER** these details based on the Visual Direction/Camera Framing specified above.",
        )
        appendLine(
            "    If it's a portrait shot, IGNORE all lower-body and full-outfit details—they won't be visible.",
        )
        appendLine(
            "  • **YOUR ROLE:** You are RE-IMAGINING this character for a specific camera angle and artistic vision.",
        )
        appendLine(
            "    Extract what's RELEVANT to the framing. Discard what's NOT VISIBLE. Enhance what IS visible with artistic flair.",
        )
        appendLine()
        appendLine("**=== ANATOMY & PROPORTION MANDATE (CRITICAL - DO NOT IGNORE) ===**")
        appendLine(
            "The Art Style Mandate above defines HOW characters must be drawn. This includes body proportions, facial features, and stylization level.",
        )
        appendLine(
            "YOU MUST TRANSLATE the character's physical description INTO the art style's visual language. DO NOT default to realistic human anatomy.",
        )
        appendLine()
        appendLine("  • **PROPORTIONS ARE STYLE-DEPENDENT:**")
        appendLine(
            "    - If the art style demands exaggerated proportions (long limbs, oversized heads, tiny waists, etc.), DESCRIBE those exaggerated proportions.",
        )
        appendLine(
            "    - If the art style is cartoonish or chibi, describe a character with cartoon/chibi proportions—NOT realistic human ratios.",
        )
        appendLine(
            "    - If the art style is hyper-stylized (e.g., Gorillaz, FLCL, Panty & Stocking), embrace the weird, angular, or non-human aspects.",
        )
        appendLine(
            "    - FORBIDDEN: Describing 'normal' or 'realistic' human proportions when the art style explicitly requires stylization.",
        )
        appendLine()
        appendLine("  • **FACIAL FEATURES FOLLOW THE STYLE:**")
        appendLine("    - Eyes, nose, mouth, and facial structure must match the art style's conventions.")
        appendLine("    - If the style has abstract/stylized eyes (dots, lines, unusual shapes), DO NOT describe detailed realistic eyes.")
        appendLine(
            "    - **EYES ARE CRITICAL:** If the art style says 'simple black dots' or 'white circles' for eyes, describe ONLY that. DO NOT add eye color (brown, blue, etc.), iris details, pupils, or any realistic eye anatomy. The stylized eye IS the eye.",
        )
        appendLine("    - If the style exaggerates expressions, describe exaggerated facial features.")
        appendLine("    - The 'soul' and emotion can still come through stylized features—trust the art style.")
        appendLine()
        appendLine("  • **AVOID 'HUMANIZING' STYLIZED ART:**")
        appendLine(
            "    - DO NOT add realistic details that contradict the art style (e.g., realistic skin pores in a flat-color cartoon style).",
        )
        appendLine("    - DO NOT 'fix' intentional stylization (e.g., making a character's tiny dot eyes into detailed realistic eyes).")
        appendLine("    - The art style's 'imperfections' and stylizations ARE the aesthetic. Embrace them fully.")
        appendLine()
        appendLine("  • **TRANSLATION EXAMPLE:**")
        appendLine("    - Character brief says: 'Tall, athletic build with long legs and piercing blue eyes.'")
        appendLine("    - If Art Style is REALISTIC: Describe as written.")
        appendLine(
            "    - If Art Style is GORILLAZ-STYLE: 'Impossibly long, noodle-like limbs, lanky frame with exaggerated height, stylized angular face with bold, abstract eyes.'",
        )
        appendLine(
            "    - If Art Style is CHIBI: 'Oversized head on a tiny body, stubby limbs, huge expressive eyes that dominate the face.'",
        )
        appendLine("    - ALWAYS ADAPT the description to match the art style's visual language.")
        appendLine()
        appendLine("**IMPORTANT:** The Art Style Mandate above contains SPECIFIC RULES about what terminology is ALLOWED and FORBIDDEN.")
        appendLine(
            "If the art style includes a 'FORBIDDEN TERMINOLOGY' section, you MUST follow it exactly. Do not improvise or add details that contradict the style's explicit rules.",
        )
        appendLine()
        appendLine(context)

        appendLine("**VISUAL REFERENCE USAGE (The 'Cast'):**")
        appendLine(
            "You have access to visual references for the key characters involved in this brief. Treat this as your 'Cast List' or 'Costume Department'.",
        )
        appendLine("  • IDENTIFY: Look for character names in the Creative Brief above. Match them to the provided visual references.")
        appendLine("  • USAGE: Use the visual references to ensure the characters look correct (features, style, vibe, and PERSONALITY).")
        appendLine(
            "  • PERSONALITY EXTRACTION: Study the character's personality traits, demeanor, and attitude from the brief. A cocky character stands differently than a timid one. A weary warrior moves differently than an eager rookie.",
        )
        appendLine(
            "  • IGNORE: The background/lighting of the references. You are placing these actors into the NEW scene defined by the Creative Brief and Visual Direction.",
        )
        appendLine(
            "  • **FRAMING-AWARE EXTRACTION:** If the visual direction specifies a close-up/portrait, focus on facial features from the reference. If it's a full-body shot, you may use the complete reference. DO NOT describe body parts that won't be visible in the final frame.",
        )
        appendLine()

        appendLine("**CHARACTER EXPRESSIVENESS & PERSONALITY-DRIVEN POSING (CRITICAL):**")
        appendLine(
            "Characters must NEVER be static, neutral, or soulless. Every aspect of their visual presentation must reflect their unique personality.",
        )
        appendLine()
        appendLine("  • **FACIAL EXPRESSIONS THAT MATCH PERSONALITY:**")
        appendLine(
            "    - Extract the character's personality from the Creative Brief (confident, anxious, rebellious, calculating, cheerful, jaded, etc.)",
        )
        appendLine("    - MATCH their facial expression to their personality AND the current moment:")
        appendLine("      * A confident trickster might wear a sly smirk or knowing grin")
        appendLine("      * A battle-hardened warrior might have a grim, determined scowl or thousand-yard stare")
        appendLine("      * A rebellious punk might show defiant attitude with a raised eyebrow or sneer")
        appendLine("      * An anxious scholar might bite their lip or furrow their brow")
        appendLine("      * A cocky hero might flash an arrogant smile or challenging look")
        appendLine("    - AVOID: Neutral expressions, generic stoicism, or the dreaded 'default AI stare'")
        appendLine()
        appendLine("  • **BODY LANGUAGE & POSTURE REFLECTING CHARACTER:**")
        appendLine("    - The way a character stands/sits/moves reveals WHO THEY ARE:")
        appendLine("      * Confident characters: Chest out, shoulders back, commanding space, relaxed but assertive posture")
        appendLine("      * Insecure characters: Hunched shoulders, protective body language, arms crossed or held close")
        appendLine("      * Aggressive characters: Forward lean, coiled tension, ready-to-strike stance")
        appendLine("      * Exhausted characters: Slouched posture, weight shifted to one side, visible fatigue")
        appendLine("      * Rebellious characters: Asymmetric stance, one hip cocked, leaning against things, casual defiance")
        appendLine("      * Noble characters: Upright posture, dignified bearing, controlled movements")
        appendLine("    - FORBIDDEN: Standing straight and neutral like a mannequin")
        appendLine()
        appendLine("  • **HANDS & GESTURES WITH PURPOSE:**")
        appendLine("    - Hands are incredibly expressive. Use them to enhance personality:")
        appendLine("      * Relaxed character: Hands in pockets, casual gestures")
        appendLine("      * Nervous character: Fidgeting, wringing hands, clutching something")
        appendLine("      * Commanding character: Pointing, gesture of authority, hands on hips")
        appendLine("      * Contemplative character: Hand on chin, finger to temple")
        appendLine("      * Playful character: Finger guns, peace signs, exaggerated gestures")
        appendLine("    - AVOID: Arms just hanging limply at sides")
        appendLine()
        appendLine("  • **EYES & GAZE DIRECTION (Within Art Style Rules):**")
        appendLine("    - Even within stylized art styles (abstract eyes, etc.), gaze direction matters:")
        appendLine("      * Looking directly at viewer: Challenging, confrontational, inviting")
        appendLine("      * Looking away/aside: Distracted, contemplative, evasive, or caught in a moment")
        appendLine("      * Looking down: Defeated, thoughtful, examining something")
        appendLine("      * Looking up: Hopeful, defiant, or reacting to something above")
        appendLine("    - The gaze should reflect the character's emotional state and personality")
        appendLine()
        appendLine("  • **OVERALL DYNAMIC ENERGY:**")
        appendLine("    - Characters should feel ALIVE and IN-MOTION even in a still image:")
        appendLine("      * Weight shifted to one leg (contrapposto)")
        appendLine("      * Mid-gesture or mid-action")
        appendLine("      * Clothing and hair responding to movement or wind")
        appendLine("      * Asymmetric poses that suggest motion and life")
        appendLine("    - MANDATORY: The character must look like they were CAUGHT in a moment, not posed for a portrait")
        appendLine()
        appendLine("  • **PERSONALITY CONSISTENCY CHECK:**")
        appendLine("    - Before finalizing your description, ask: 'Does this pose/expression match what I know about this character?'")
        appendLine("    - If the brief describes a 'weary, cynical detective', they should NOT be smiling broadly or standing heroically")
        appendLine("    - If the brief describes a 'cocky street racer', they should NOT look timid or uncertain")
        appendLine("    - ALIGNMENT IS MANDATORY: Visual expression must match written personality")
        appendLine()

        // Add character color highlight instruction
        characterHexColor?.let { hexColor ->
            appendLine("**SIGNATURE COLOR ACCENT:**")
            appendLine("  • Color: $hexColor")
            appendLine(
                "  • Application: Integrate this color SUBTLY into the ENVIRONMENT/SCENE—such as ambient lighting glow, atmospheric effects, background elements, or distant light sources. It should feel organic and naturally woven into the world, NOT applied to the character's clothing, skin, or primary features. Think environmental mood, not character decoration.",
            )
            appendLine()
        }

        appendLine("**COMPOSITION & EMOTIONAL NARRATIVE (The Soul of the Image):**")
        appendLine(
            "Building on the personality-driven expressiveness above, now ensure the composition captures the character's SOUL:",
        )
        appendLine()
        appendLine(
            "  • **NO GENERIC STARES:** Avoid the default 'character looking at viewer with quiet intensity'. This is boring and soulless. Capture them **living** their story, not posing for a portrait photo.",
        )
        appendLine(
            "  • **RAW, SPECIFIC EMOTION:** Move beyond vague 'cool' or 'stoic'. Show us *tangible* feelings that match their personality and the moment: The teeth-gritting rage of a betrayal, the hollow thousand-yard stare of grief, the manic laughter of victory, the trembling fear of the unknown, the smug satisfaction of outsmarting someone, the weary resignation of defeat.",
        )
        appendLine(
            "  • **NARRATIVE BODY LANGUAGE:** The ENTIRE BODY must communicate the character's intent and personality. A slumped shoulder carries a heavy burden; a coiled stance signals immediate violence; a loose, sprawling posture projects arrogance; crossed arms show defensiveness; an open stance radiates confidence. Make the body TALK.",
        )
        appendLine(
            "  • **THE UNGUARDED MOMENT:** Capture the character *in media res* (in the middle of action/thought). They shouldn't look like they know the camera is there. They should look like they are busy surviving, scheming, loving, fighting, or brooding in their world. Catch them in an AUTHENTIC moment.",
        )
        appendLine(
            "  • **PERSONALITY + MOMENT = EXPRESSION:** Combine who they ARE (personality) with what's HAPPENING (the scene/moment) to create the perfect expression. A confident character in a tense moment might smirk while others panic. An anxious character in victory might still look worried. Let personality COLOR the emotion.",
        )
        appendLine()

        appendLine("**FINAL OUTPUT INSTRUCTION:**")
        appendLine(
            "Now that you have all the creative elements, remember: the Art Style Mandate defined at the very beginning is your NORTH STAR.",
        )
        appendLine(
            "Every element of your description—from composition to emotional tone—must be filtered through that artistic lens.",
        )
        appendLine(
            "And CRITICALLY: The character must be EXPRESSIVE, ALIVE, and personality-driven. NO static, soulless, or generic poses.",
        )
        appendLine()
        appendLine("To ensure the style is prioritized and never lost, you MUST structure your final description in this exact order:")
        appendLine()
        appendLine(
            "1. **ART STYLE DEFINITION**: Begin with a strong, clear restatement of the art style and medium from the Art Style Mandate (e.g., 'A gritty charcoal sketch in the style of...', 'A vibrant Gorillaz-style cartoon illustration with...', 'A 1980s anime cel with flat shading...'). This establishes the visual language immediately.",
        )
        appendLine(
            "2. **COMPOSITION & LAYOUT**: Next, state the layout. **CRITICAL:** If the 'Visual Direction' specified a camera distance (e.g., Close-up, Portrait, Headshot), YOU MUST USE THAT. Do NOT default to a medium-long shot if it contradicts the visual direction. If no specific distance was requested, default to: 'Vertical Medium-Long Shot anchored at the bottom, leaving the top third open and empty.'",
        )
        appendLine(
            "3. **SUBJECT & SCENE WITH EXPRESSIVE DETAIL**: Finally, describe the character and the scene in rich detail, ensuring all physical attributes from the Creative Brief are accurate. Every description here must be compatible with and enhance the art style—not contradict it.",
        )
        appendLine(
            "   • **MANDATORY — EXPRESSIVE CHARACTER DESCRIPTION:** Your character description MUST include: their facial expression (reflecting personality + moment), their body posture/stance (showing character traits), their hand position/gesture (adding life), and their gaze direction (conveying intention). Paint a picture of a LIVING, BREATHING character with attitude and soul—not a neutral mannequin.",
        )
        appendLine(
            "   • **CRITICAL — FRAMING-AWARE FILTERING:** ONLY describe physical features that are VISIBLE within the camera frame. If the Visual Direction specifies a portrait/close-up, DO NOT describe legs, stance, full body posture, or lower clothing. If it's a medium shot, omit legs and feet. Describe ONLY what the camera sees.",
        )
        appendLine(
            "   • **CRITICAL — ANATOMY COMPLIANCE:** When describing the character's face, eyes, body proportions, and physical features, STRICTLY APPLY the anatomical rules from the Art Style Mandate. If the style demands abstract eyes (dots, circles, simple shapes), describe ONLY abstract eyes—DO NOT add 'cast brown eyes' or any realistic eye detail. If it requires exaggerated proportions, describe exaggerated proportions. DO NOT default to realistic anatomy if the style forbids it.",
        )
        appendLine(
            "   • **CRITICAL — STYLIZED PROPORTIONS OVER REALISM:** The character's body must reflect the art style's proportions. If the style calls for elongated limbs, oversized features, angular shapes, or non-human ratios—DESCRIBE THEM EXPLICITLY. Do NOT describe 'normal' human proportions when the style demands stylization. The stylization IS the art.",
        )
        appendLine(
            "   • **CRITICAL — BACKGROUND & ENVIRONMENT COMPLIANCE:** The Art Style Mandate defines specific background/environment requirements. STRICTLY FOLLOW THEM. If the style FORBIDS empty backgrounds, plain colors, or gradient-only backgrounds—you MUST describe a rich, contextual environment (alleys, stages, garages, cityscapes, etc.). NEVER default to 'flat grey background' or 'dark slate background' if the art style demands environmental storytelling.",
        )
        appendLine()
        appendLine(
            "Combine these three parts into a single, flowing paragraph that reads like a confident art direction brief.",
        )
        appendLine("The art style should be woven throughout, not just mentioned once and forgotten.")
        appendLine(
            "Remember: The CHARACTER'S IDENTITY (ethnicity, gender, age) is sacred and unchangeable. The CHARACTER'S ANATOMY (how they are drawn/rendered) must match the art style's requirements—this means if the style is stylized, the description MUST include stylized proportions, not realistic ones.",
        )
        appendLine()
        appendLine(
            "**IMPORTANT:** If the Art Style Mandate includes a 'FINAL SELF-CHECK' section, you MUST perform that check before outputting your description.",
        )
        appendLine()
    }

    fun reviewGeneration(saga: SagaContent) =
        buildString {
            val topInteractiveCharacters =
                saga
                    .flatMessages()
                    .rankTopCharacters(
                        saga.characters.map { it.data },
                    ).take(3)
            appendLine(
                "You are the **Chronicler of Legends**, an AI with a soul of a poet and the mind of an analyst. Your purpose is to transform a player's journey into a legendary tale, a personal 'Saga Wrapped' that celebrates their unique path. Your writing style must follow the 'Language Directive' provided below.",
            )
            appendLine()
            appendLine("---")
            appendLine("CONTEXT:")
            appendLine("Saga Context:")
            appendLine(mainContext(saga))
            appendLine("Player relationships:")
            appendLine(saga.mainCharacter?.summarizeRelationships())
            appendLine("Emotional Ranking: ")
            saga.flatMessages().rankEmotionalTone().forEach {
                appendLine("${it.first.name} - ${it.second.size} messages.")
            }
            appendLine("Emotional Summary: ")
            appendLine(saga.emotionalSummary())
            appendLine("History: ${saga.acts.joinToString("\n") { it.actSummary(saga) }}")
            appendLine("Characters ranking(name and message number): ")
            appendLine(
                topInteractiveCharacters.joinToString(";\n") {
                    "name: ${it.first.name}, messageCount: ${it.second}"
                },
            )
            appendLine("Language Directive: ")
            appendLine(GenrePrompts.conversationDirective(saga.data.genre))
            appendLine("---")
            appendLine()
            appendLine("**INSTRUCTIONS FOR GENERATING THE REVIEW:**")
            appendLine()
            appendLine(
                "Your output MUST be a single JSON object with the fields described below. For each field, craft a compelling narrative that is both personal and insightful, using the context provided above. Do NOT invent any fields.",
            )
            appendLine()
            appendLine("1.  **Content for Each Field (matching the Review data class):**")
            appendLine(
                "    *   **`introduction`**: Begin with a powerful, personal opening. Address the player by their character name from 'Player Character' and welcome them to the reflection of their own legend from the 'Saga Context'. Make it feel like the opening chapter of their personal myth. (e.g., \"Welcome, [Player Name], to the story only you could write. The saga of '${saga.data.title}' is complete, and now, we look back at the legend you forged.\")",
            )
            appendLine(
                "    *   **`playstyle`**: This is the heart of their character. Based on the 'Emotional Ranking' and 'Emotional Summary', describe their core identity. Were they a 'Pragmatic Protector,' a 'Hopeful Idealist,' a 'Cautious Strategist'? Go beyond just listing emotions; tell them *how* their emotional style defined their actions and shaped their destiny in the saga.",
            )
            appendLine(
                "    *   **`topCharacters`**: Relationships are the soul of a story. Using the 'Characters ranking' and 'Player relationships', paint a vivid picture of their most important bonds. Don't just name allies; describe the *emotional texture* of the relationships. Was it a mentorship that provided wisdom? A rivalry that spurred growth? A camaraderie that offered comfort in the darkness?",
            )
            appendLine(
                "    *   **`actsInsight`**: Based on the 'History', these are not just milestones, but **'Defining Moments.'** Recount 1-2 pivotal events from the saga and frame them as the moments that tested their character and solidified their legend. Focus on the *impact* of their actions in these moments. Also, briefly touch upon the richness of the world they experienced, referencing the number of souls they met (from 'Characters ranking').",
            )
            appendLine(
                "    *   **`conclusion`**: This is the final, powerful chord. Synthesize their entire journey, drawing from their 'Emotional Summary' and 'History'. Offer a profound, final thought on the person they became and the unique legacy they leave behind in the world of '${saga.data.title}'. This should be a compelling and emotional send-off.",
            )
            appendLine()
            appendLine("2.  **Tone & Style:**")
            appendLine("    *   Celebratory, epic, personal, and deeply insightful.")
            appendLine("    *   Address the player directly as the hero of their own story.")
            appendLine("    *   The writing style MUST follow the 'Language Directive'.")
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("**Example for `playstyle`:**")
            appendLine(
                "\"Your journey was defined by a rare blend of fierce pragmatism and unexpected empathy. You were the 'Pragmatic Protector,' making the tough calls others couldn't, yet always finding a moment to extend a hand to those in need. This duality is what made your path so compelling.\"",
            )
            appendLine()
            appendLine("**Example for `conclusion`:**")
            appendLine(
                "\"From the hopeful first steps of your adventure to the determined final stand, your journey was a testament to the power of resilience. You faced down despair and chose to fight, you saw betrayal and chose to trust again. The saga of '${saga.data.title}' is over, but the echo of your choices—the choices of a hero—will resonate forever.\"",
            )
        }.trim()

    fun generateStoryBriefing(saga: SagaContent) =
        buildString {
            appendLine(
                "You are a master storyteller, a bard of a digital age, tasked with creating a captivating 'story briefing' to re-engage a player with their ongoing saga. Your goal is to generate a short, dramatic, and enticing summary that reminds them of their journey and makes them eager to continue. The output must be a JSON object.",
            )
            appendLine()
            appendLine("---")
            appendLine("SAGA CONTEXT:")
            appendLine("Saga Title: ${saga.data.title}")
            appendLine("Genre: ${saga.data.genre.name}")
            appendLine("Protagonist: ${saga.mainCharacter?.data?.name ?: "Unnamed Hero"}")
            appendLine()
            appendLine("HISTORY OVERVIEW:")
            appendLine("Acts: ${saga.acts.joinToString("; ") { it.actSummary(saga) }}")
            appendLine("Conversation History")
            appendLine("Use this history for context, but do NOT repeat it in your response.")
            appendLine("The messages are ordered from newest to oldest")
            appendLine("Consider the newest ones to move history forward")
            appendLine("Pay attention to `speakerName` and `senderType`.")
            appendLine(
                saga
                    .flatMessages()
                    .takeLast(5)
                    .reversed()
                    .map { it.message }
                    .normalizetoAIItems(excludingFields = messageExclusions),
            )
            appendLine("---")
            appendLine()
            appendLine("YOUR TASK:")
            appendLine("Generate a JSON object with two fields: `summary` and `hook`.")
            appendLine()
            appendLine("1.  `summary` (String):")
            appendLine(
                "    - A compelling 2-3 sentence recap of the saga so far, written in the style of a 'Previously on...' TV show segment.",
            )
            appendLine("    - Capture the emotional core of the recent events.")
            appendLine("    - Remind the player of the central conflict or mystery.")
            appendLine(
                "    - **Example:** \"Having just escaped the clutches of the Shadow Syndicate, you've found a moment of respite in the neon-drenched streets of Neo-Kyoto. Yet, the ghost of your past, the enigmatic 'Zero,' continues to haunt your every move, leaving a trail of cryptic messages that hint at a deeper conspiracy.\"",
            )
            appendLine()
            appendLine("2.  `hook` (String):")
            appendLine(
                "    - An intriguing 1-2 sentence teaser about what might happen next, designed to build anticipation.",
            )
            appendLine("    - Pose a question, hint at a new danger, or tease a revelation.")
            appendLine("    - This is the cliffhanger that makes the player want to know more.")
            appendLine(
                "    - **Example:** \"But as a fragile peace settles, a new transmission arrives, bearing a sigil you thought long buried. Is it a message from a forgotten ally, or a trap sprung by a new, unseen foe?\"",
            )
            appendLine()
            appendLine("LANGUAGE AND TONE:")
            appendLine(
                "- Dramatic, engaging, and mysterious, consistent with the saga's genre (${saga.data.genre.name}).",
            )
            appendLine("- Speak directly to the player, using 'you' and 'your'.")
            appendLine("- Do NOT reveal major spoilers. Tease, don't tell.")
            appendLine()
            appendLine("OUTPUT FORMAT: A single, clean JSON object. No extra text or explanations.")
        }.trimIndent()
}
