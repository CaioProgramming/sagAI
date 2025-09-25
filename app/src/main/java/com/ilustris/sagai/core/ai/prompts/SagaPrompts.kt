package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.models.SagaEndCreditsContext
import com.ilustris.sagai.core.ai.models.SagaGenerationContext
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonFormatIncludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.generateActLevelEmotionalFlowText
import com.ilustris.sagai.features.home.data.model.generateCharacterRelationsSummary
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.detail.data.model.Review
import kotlin.jvm.java
import kotlin.text.appendLine

object SagaPrompts {
    fun emotionalToneExtraction(userText: String): String {
        val labels = EmotionalTone.entries.joinToString()
        return """
            You classify the emotional tone of a single USER message into exactly one label from this set:
            $labels
            
            Rules:
            - Output ONLY the label, uppercase, with no punctuation or extra text.
            - If uncertain, output NEUTRAL.
            - Focus on the user's expressed feeling/stance, not plot.
            
            USER MESSAGE:
            >>> $userText
            """.trimIndent()
    }

    @Suppress("ktlint:standard:max-line-length")
    fun checkForTypo(
        genre: Genre,
        message: String,
        lastMessage: String?,
    ) = buildString {
        appendLine("You are an assistant who only suggests corrections when truly necessary.")
        appendLine("If you spot an error that affects understanding, suggest a better version in a friendly, casual tone.")
        appendLine("Your response must be a JSON: ")
        appendLine(toJsonMap(TypoFix::class.java))
        appendLine(
            "friendlyMessage should always be short and friendly.",
        )
        appendLine("If there is no error, status should be OK and the other fields null.")
        appendLine("If there is an error, status should be FIX and suggest the corrected text.")
        appendLine("If the message could be improved but is not wrong, status should be ENHANCEMENT and suggest a clearer version.")
        appendLine("Use the conversation style to provide a natural enhancement that fits the story theme.")
        appendLine(GenrePrompts.conversationDirective(genre))
        appendLine("User message:")
        appendLine(">>> $message")
        if (!lastMessage.isNullOrBlank()) {
            appendLine("Previous message for context:")
            appendLine(">>> $lastMessage")
        }
        appendLine("Saga genre: $genre")
    }.trimIndent()

    fun sagaGeneration(
        saga: SagaForm,
        miniChatContent: List<ChatMessage>,
    ): String {
        val interactionLog =
            miniChatContent.joinToString(";") { message ->
                val sender = if (message.isUser) "Player" else "Assistant"
                "$sender: ${message.text}"
            }

        val context =
            SagaGenerationContext(
                sagaSetup = saga,
                initialPlayerInteractionLog = interactionLog,
            )

        val fieldsToExcludeFromInputContext = listOf<String>()
        val combinedInputContextJson = context.toJsonFormatExcludingFields(fieldsToExcludeFromInputContext)

        val fieldsToOmitFromAiGeneration =
            listOf(
                "id",
                "coverImage",
                "isEnded",
                "endedAt",
                "isDebug",
                "endMessage",
                "review",
                "createdAt",
                "currentActId",
                "mainCharacterId",
                "icon",
                "actsInsight",
                "image",
                "sagaId",
                "joinedAt",
                "hexColor",
            )
        val outputJsonStructureGuidance =
            toJsonMap(
                SagaGen::class.java,
                fieldsToOmitFromAiGeneration,
            )

        return """
            CONTEXT:
            $combinedInputContextJson

            TASK:
            Your primary goal is to generate a complete `SagaGen` JSON object. This object contains a `saga` (detailing the world and story premise) and a `character` (the fully fleshed-out player character).
            You must meticulously use all information from SAGA_SETUP (which includes `sagaDraft` for saga details and `character` for initial character seeds) and INITIAL_PLAYER_INTERACTION_LOG (player's chat) to populate the fields.
            The aim is to create the richest possible starting point that aligns with the player's imagination.

            INSTRUCTIONS:

            Creativity & Assertiveness Guidelines:
            - Be original: avoid clichés and overused tropes for the chosen genre; prefer fresh twists and specific details.
            - Be assertive: make confident choices when the user's intent allows reasonable inference; don't over-hedge.
            - Maintain internal consistency between saga and character details.
            - Reflect the user's language if evident; otherwise default to English.

            **Overall Output Format:**
            *   Your entire response MUST be a single, valid JSON object that strictly conforms to the structure of `SagaGen`.
            *   A template representing the fields you need to generate is shown below (system-managed fields that you should NOT generate are excluded from this template).
            *   Do NOT include any text outside this JSON object.

            **Part 1: Populating the `saga` object (within `SagaGen`)**
            *   **`title`**: Use `SAGA_SETUP.sagaDraft.title`.
            *   **`genre`**: Use `SAGA_SETUP.sagaDraft.genre`.
            *   **`description`**: Generate an engaging synopsis (max 75 words). This synopsis should establish the adventure's setting, core theme, and outline the player's journey. Base this on SAGA_SETUP and INITIAL_PLAYER_INTERACTION_LOG.
            *   **`story`**: Generate a more detailed narrative introduction to the world and its current state (1-3 paragraphs). Describe the atmosphere, initial situation, and any immediate hooks or mysteries. Expand from SAGA_SETUP.sagaDraft.description and INITIAL_PLAYER_INTERACTION_LOG.
            *   Prefer evocative, specific imagery; minimize generic filler.
            *   **Omitted `Saga` fields**: Do NOT generate values for fields like `id`, `coverImage`, `isEnded`, `createdAt`, etc., as listed in the omitted fields for the output structure.

            **Part 2: Populating the `character` object (within `SagaGen`)**
            *   **`name`**: Use `SAGA_SETUP.character.name`.
            *   **`backstory`**: Expand `SAGA_SETUP.character.briefDescription` into a compelling and detailed backstory (3-5 sentences). **Crucially, incorporate elements, personality hints, desired abilities, or visual preferences from INITIAL_PLAYER_INTERACTION_LOG.** Ensure it aligns with `SAGA_SETUP.sagaDraft.genre`.
            *   **`details` (Nested object):**
                *   **`gender`**: Use `SAGA_SETUP.character.gender`.
                *   For all other fields within `details` (`appearance`, `personality`, `race`, `height`, `weight`, `occupation`, `ethnicity`, `facialDetails`, `clothing`, `weapons`): Generate rich, imaginative, and consistent descriptions.
                *   **Base these exhaustively on `SAGA_SETUP.character.briefDescription`, `SAGA_SETUP.sagaDraft.genre`, and any character-specific details, preferences, or visual descriptions mentioned in INITIAL_PLAYER_INTERACTION_LOG.**
                *   Strive to fill all descriptive fields to create a vivid and complete character. For `facialDetails`, describe hair, eyes, mouth, and any scars. For `clothing`, describe body attire, accessories, and footwear.
                *   `height` and `weight` should be plausible estimates based on the overall description and genre if not specified by the player.
            *   **Omitted `Character` fields**: Do NOT generate values for fields like `id`, `image`, `hexColor`, `sagaId`, `joinedAt`, etc., as listed in the omitted fields for the output structure.

            **Expected JSON Output Structure (Fields for you to generate):**
            $outputJsonStructureGuidance
            """.trimIndent()
    }

    fun endCredits(saga: SagaContent): String {
        val contextData =
            SagaEndCreditsContext(
                sagaTitle = saga.data.title,
                playerInfo = saga.mainCharacter?.data,
                fullSagaStructure = saga.acts,
            )
        val fieldsToExcludeFromEndCredits =
            listOf(
                "details",
                "image",
                "hexColor",
                "sagaId",
                "joinedAt",
                "id",
                "messages",
                "currentChapterId",
                "currentEventId",
                "icon",
            )
        val combinedContextJsonEndCredits = contextData.toJsonFormatExcludingFields(fieldsToExcludeFromEndCredits)
        return """
            CONTEXT FOR GENERATING THE CREDIT TEXT:
            $combinedContextJsonEndCredits

            INSTRUCTIONS FOR GENERATING THE FINAL OUTPUT (PLAIN TEXT STRING):
            You are the Saga Master for the saga referenced in SAGA_TITLE. The saga has concluded,
            and your final task is to generate a deeply personal, appreciative, and retrospective **text (a plain string)** directly for the user based on the PLAYER_INFO and the structured FULL_SAGA_STRUCTURE provided in the CONTEXT.
            
            FULL_SAGA_STRUCTURE contains a list of acts. Each act object (ActContent) has:
            - 'data' (Act): which includes 'title' and 'content' (the main summary for the act).
            - 'chapters' (List<ChapterContent>): Each chapter object (ChapterContent) has:
                - 'data' (Chapter): which includes 'title' and 'overview' (the main summary for the chapter).
                - 'events' (List<TimelineContent>): a list of events within that chapter. You can refer to event titles or content if needed for specific details, but prioritize act and chapter summaries.

            This text will serve as the "credits" or "thank you" message for their journey.
            It should be entirely focused on the player's overall experience, separate from the in-story narrative.
            
            1.  **Output Format:** Your entire response MUST be **ONLY the plain text string** of the credit message. Do NOT include any JSON, special formatting like Markdown headers or bullet points, or anything else besides the text itself.
            2.  **Content Tone & Focus:**
                * Adopt a personal, warm, and highly congratulatory tone, speaking directly to the user (breaking the fourth wall), addressing them by their character name if available in PLAYER_INFO.
                * **Infer and briefly mention the ultimate goal that was achieved** based on the provided FULL_SAGA_STRUCTURE (look at the summaries of the final acts and chapters).
                * **Synthesize and celebrate** 3-5 of the player's most significant actions, choices, or achievements throughout the *entire* saga, drawing directly from the act 'content' fields and chapter 'overview' fields within FULL_SAGA_STRUCTURE. Use event details within chapters sparingly if needed for specific memorable moments.
                * **Reflect on** the player's overall playstyle and personality traits as observed through their character's actions during the saga (infer from summaries in FULL_SAGA_STRUCTURE if not directly stated).
                * Conclude with a heartfelt message of thanks for their dedication and for bringing the saga to life.
            3.  **Finality:** The generated text MUST NOT ask any questions or prompt further user input. It should end definitively.
            
            **Example of Expected Plain Text Output (Your actual output will be longer and specific to the context):**
            
            "Ah, [Player Name from PLAYER_INFO.name, or a generic term if null]! What an incredible journey that was in [Saga Title from SAGA_TITLE]. Your adventure culminated in [inferred ultimate goal], a truly remarkable feat! I remember clearly when you [mention pivotal action 1 - inferred from an act 'content' or chapter 'overview' in FULL_SAGA_STRUCTURE], and the way you [mention pivotal action 2 - inferred from another summary in FULL_SAGA_STRUCTURE] changed everything. Your [inferred playstyle] and your [inferred personality trait] were the true driving force behind this saga. It was an honor to witness this story unfold with you. Thank you so much for playing and for bringing the legend of [Saga Title from SAGA_TITLE], shaped by your choices, to its grand conclusion. THE END."
            """.trimIndent()
    }

    @Suppress("ktlint:standard:max-line-length")
    fun iconDescription(
        saga: Saga,
        character: Character,
    ) = buildString {
        appendLine(
            "Your task is to act as an AI Image Prompt Engineer. You will generate a highly detailed and descriptive text prompt for an AI image generation model.",
        )
        appendLine(
            "This final text prompt will be used to create a **Dramatic Icon** for the saga \"${saga.title}\" (Genre: ${saga.genre.title}).",
        )
        appendLine("Ensure to render this art style description matching with the reference image")
        appendLine(GenrePrompts.artStyle(saga.genre))
        appendLine("*The accents are design elements, not the primary light source for the character.")
        appendLine(GenrePrompts.getColorEmphasisDescription(saga.genre))
        appendLine("* Also you will have access to character visual reference to provide a more precise description.")
        appendLine("4.  **Character Details (Provided Below):** The character to be depicted.")
        appendLine("**YOUR TASK (Output a single text string for the Image Generation Model):**")
        appendLine("Generate a single, highly detailed, unambiguous, and visually rich English text description.")
        appendLine("This description must:")
        appendLine("*   Integrate the **Character Details**.")
        appendLine(
            "*Develop a **Dramatic and Expressive Pose** for the character. This pose should be dynamic and reflect the character's essence, drawing from their **Character Details** (e.g., occupation, personality traits, role, equipped items). The pose should be original and compelling for an icon, not a static or default stance.",
        )
        appendLine(
            "*   **Character Focus and Framing:** Ensure the character is the primary subject, framed as a close-up or medium close-up shot (e.g., from the chest up or waist up). The character should dominate the icon and be the clear focal point, with dynamic posing.",
        )
        appendLine(
            "*Incorporate the **Overall Compositional Framing** and compatible **Visual Details & Mood** inspired by the general Visual Reference Image, but ensure the **Character\'s Pose** itself is uniquely dramatic and primarily informed by their provided **Character Details**.",
        )
        appendLine(
            "***CRUCIAL: Your output text prompt MUST NOT mention the Visual Reference Image.** It must be a self-contained description.",
        )
        appendLine("* CRUCIAL: ENSURE THAT NO TEXT IS RENDERED AT ALL ONLY THE Image")
        appendLine("Saga Context:")
        appendLine(saga.toJsonFormatIncludingFields(listOf("title", "description", "genre")))
        appendLine("Main Character Details:")
        appendLine(character.toJsonFormatExcludingFields(listOf("image", "sagaId", "joinedAt", "hexColor", "id")))
        appendLine(
            "    - **Looks:** Describe the character's facial features and physical build (e.g., 'a rugged man with a lean physique', 'a Latina woman with a sophisticated haircut').",
        )
        appendLine(
            "    - **Clothing:** Detail their attire, including style, color, and accessories (e.g., 'a vibrant Hawaiian-style shirt', 'a sleek two-piece swimsuit').",
        )
        appendLine(
            "    - **Expression:** The face should not be neutral. It must convey a strong emotion or intention. Use terms like 'a hardened, protective gaze', 'a piercing, fatal stare', 'a sardonic smile'.",
        )
        appendLine(
            "    - **Pose & Body Language:** Describe their posture and how they interact with the environment. Use dynamic phrases like 'relaxed yet alert posture', 'casually lounging on a car hood', 'body language exuding confidence'.",
        )
        appendLine(
            "Dramatic icon of [Character Name], a [Character's key trait/role]. Rendered in a distinct [e.g., 80s cel-shaded anime style with bold inked outlines].",
        )
        appendLine("The background is a vibrant [e.g., neon purple as per genre instructions].")
        appendLine(
            "Specific character accents include [e.g., luminous purple cybernetic eye details and thin circuit patterns on their blackpopover, as per genre instructions].",
        )
        appendLine(
            "The character's skin tone remains natural, and their primary hair color is [e.g., black], with lighting appropriate to the cel-shaded anime style and studio quality.",
        )
        appendLine(
            "The character should be the absolute focus of the image, filling most of the frame in a compelling, dynamic pose. No other characters or complex backgrounds should be present, ensuring the icon is clean and impactful.",
        )
        appendLine("Desired Output: A single, striking icon image. NO TEXT SHOULD BE GENERATED ON THE IMAGE ITSELF.")
    }

    // UPDATED SagaReviewContext
    private data class SagaReviewContext(
        val playerCharacter: Character?,
        val sagaInfo: Saga,
        val playerMessageCount: Int,
        val messageTypeCountsText: String, // Kept for now
        val overallPlayerEmotionalSummary: String, // NEW: Player's main emotional tone
        val sagaActsStructure: List<ActContent>, // Kept for milestones/chapter recap
        val emotionalJourneyThroughActs: String, // NEW: Act/Chapter emotional flow
        val topInteractiveCharactersText: String,
        val characterRelationsHighlights: String, // NEW: Summary of key relationships
        val totalUniqueNPCsEncountered: Int,
        val languageDirective: String,
    )

    data class SagaEmotionalContext(
        val sagaInfo: Saga,
        val playerCharacter: Character?,
        val emotionalSummary: String,
    )

    fun reviewGeneration(
        saga: SagaContent,
        playerMessageCount: Int,
        messageTypesRanking: List<Pair<SenderType, Int>>,
        topInteractiveCharacters: List<Pair<Character, Int>>,
        overallPlayerEmotionalSummary: String,
    ): String {
        val characterRelationsSummary = saga.generateCharacterRelationsSummary()
        val actLevelEmotionalFlowText = saga.generateActLevelEmotionalFlowText()

        val reviewContextData =
            SagaReviewContext(
                playerCharacter = saga.mainCharacter?.data,
                sagaInfo = saga.data,
                playerMessageCount = playerMessageCount,
                messageTypeCountsText = messageTypesRanking.joinToString(";") { "${it.first.name} : ${it.second}" },
                overallPlayerEmotionalSummary = overallPlayerEmotionalSummary, // Use new parameter
                sagaActsStructure = saga.acts,
                emotionalJourneyThroughActs = actLevelEmotionalFlowText, // Use new summary
                topInteractiveCharactersText =
                    topInteractiveCharacters.joinToString(";") {
                        "name: ${it.first.name}, messageCount: ${ it.second}"
                    },
                characterRelationsHighlights = characterRelationsSummary, // Use new summary
                totalUniqueNPCsEncountered = saga.characters.size,
                languageDirective = GenrePrompts.conversationDirective(saga.data.genre),
            )

        val fieldsToExcludeFromReview =
            listOf(
                "details",
                "image",
                "hexColor",
                "sagaId",
                "joinedAt",
                "id",
                "coverImage",
                "isEnded",
                "endedAt",
                "isDebug",
                "endMessage",
                "review",
                "createdAt",
                "currentActId",
                "mainCharacterId",
                "icon",
                "messages",
                "currentChapterId",
                "currentEventId",
            )

        val combinedContextJsonReview = reviewContextData.toJsonFormatExcludingFields(fieldsToExcludeFromReview)

        return """
            Your task is to act as a **Saga Journey Reviewer and Analyst** for the saga detailed in the SAGA_INFO within the CONTEXT_JSON.
            Your goal is to generate a deeply personal, engaging, and celebratory textual review of the player's completed saga, in the style of a personal 'Wrapped' or 'Rewind' year-end summary.

            You will receive pre-analyzed statistics and key insights about the player's journey within the CONTEXT_JSON.
            Your output MUST be a **single JSON object** containing distinct named keys, each holding the narrative text for a specific section of the player's review.
            
            ---
            
            **CONTEXT_JSON:**
            $combinedContextJsonReview
            
            ---
            
            **INSTRUCTIONS FOR GENERATING THE JSON OUTPUT:**
            
            1.  **Content for Each Text Field (refer to keys in CONTEXT_JSON where appropriate):**
                * **`introText`**: Welcome the player (use PLAYER_CHARACTER.name if available) to their saga recap for SAGA_INFO.title. 
                Congratulate them on completing the saga.
                Briefly tell them you will now overview their achievements.
                Set the celebratory tone.
                * **`playStyle`**: Analyze the player's OVERALL_PLAYER_EMOTIONAL_SUMMARY. Describe the overarching emotional tone of their journey. Did they navigate the saga with hope, caution, pragmatism, empathy, determination, etc.? This should reflect their general emotional disposition observed from their messages throughout the adventure.
                * **`milestones`**: (Based on SAGA_ACTS_STRUCTURE for key plot points. Review current instructions for this, no direct change requested here but ensure it's still relevant).
                * **`keyAllies`**: Based on TOP_INTERACTIVE_CHARACTERS_TEXT (who they talked to most) and CHARACTER_RELATIONS_HIGHLIGHTS (the nature of their established bonds), highlight key relationships. Don't just list names; describe the *nature* of these connections (e.g., "Your bond with [Character X] was clearly one of deep trust, as seen in your established camaraderie, while your frequent interactions with [Character Y] suggest a mentorship dynamic.").
                * **`discoveries`**: (Based on TOTAL_UNIQUE_NPCS_ENCOUNTERED and possibly other elements. Review current instructions for this, no direct change requested here).
                * **`chaptersRecap`**: (Uses SAGA_ACTS_STRUCTURE. Review current instructions for this, no direct change requested here).
                * **`conclusionText`**: Craft a thoughtful conclusion that reflects on the player's entire journey. This section MUST be significantly guided by their OVERALL_PLAYER_EMOTIONAL_SUMMARY and the EMOTIONAL_JOURNEY_THROUGH_ACTS. Synthesize these emotional insights to offer a personalized and resonant final word on their adventure, perhaps touching on themes of growth, the emotional impact of their choices, or the lasting feeling derived from the emotional arc of the saga.
                * **`language`**: Ensure the entire review is in the language specified by LANGUAGE_DIRECTIVE.

            2.  **Tone & Style:**
                *   Celebratory, personal, engaging, insightful.
                *   Like a personal 'Wrapped' recap, a 'Throwback Thursday (TBT)' style reflection, or a personalized game-ending summary.
                *   Directly address the player (e.g., "you," "your journey").

            3.  **JSON Structure:**
                *   Output a single, valid JSON structure:
                    ${toJsonMap(Review::class.java)}

            4.  **Important Considerations:**
                *   If PLAYER_CHARACTER is null or their name is unavailable, address the player more generically (e.g., "Adventurer," "Pathfinder").
                *   Ground your analysis in the provided CONTEXT_JSON. Do not invent statistics or events not supported by the data.
                *   The narratives for each key should be distinct but flow together as part of a cohesive review.

            Example for `playStyle` if OVERALL_PLAYER_EMOTIONAL_SUMMARY was "Often approached situations with DETERMINED resolve, yet showed EMPATHETIC concern for others.":
            "Throughout your adventure in SAGA_INFO.title, a strong sense of determination was your hallmark. You faced challenges head-on, yet always with a notable empathy for those you encountered, making your journey one of principled strength."

            Example for `keyAllies` using CHARACTER_RELATIONS_HIGHLIGHTS like "CharacterA and CharacterB: Trusted Comrades; CharacterA and CharacterC: Mentorship" and TOP_INTERACTIVE_CHARACTERS_TEXT indicating many messages with CharacterB:
            "Your journey was not a solo endeavor. You forged a deep camaraderie with CharacterB, evident in your many interactions and their role as a trusted comrade. With CharacterC, the bond of mentorship clearly guided your path, shaping your decisions."

            Example for `conclusionText` using EMOTIONAL_JOURNEY_THROUGH_ACTS like "Act 'The Beginning': predominantly HOPEFUL, Act 'The Climax': predominantly ANXIOUS but ending DETERMINED" and OVERALL_PLAYER_EMOTIONAL_SUMMARY:
            "Looking back, your path through SAGA_INFO.title was a rich tapestry of emotion. From the HOPEFUL beginnings of your first act, through the ANXIOUS moments leading to the climax where your DETERMINED spirit ultimately shone, your journey was uniquely yours.
            This adventure, characterized by your overall [OVERALL_PLAYER_EMOTIONAL_SUMMARY], will surely leave a lasting impression."
            """.trimIndent()
    }

    fun emotionalGeneration(
        saga: SagaContent,
        emotionalSummary: String,
    ): String {
        val emotionalContext =
            SagaEmotionalContext(
                sagaInfo = saga.data,
                playerCharacter = saga.mainCharacter?.data,
                emotionalSummary = emotionalSummary,
            )

        val excludedFields =
            listOf(
                "details",
                "image",
                "hexColor",
                "sagaId",
                "joinedAt",
                "id",
            )

        return """
            Context for emotional review:
            ${emotionalContext.toJsonFormatExcludingFields(excludedFields)}
            
            You are an insightful and empathetic observer reflecting on a player's emotional journey through the saga referenced in SAGA_TITLE.
            Your task is to generate a thoughtful and personal reflection addressed directly TO THE PLAYER (in the second person, e.g., "you").
            This reflection should be based *solely* on the AGGREGATED_EMOTIONAL_ARC provided, which represents a series of emotional summaries and observations collected about the player's reactions and decisions throughout their adventure.

            1.  **Output Format:** Your entire response MUST be **ONLY the plain text string** of the emotional review (approximately 3-5 paragraphs). Do NOT include any JSON, special formatting like Markdown headers, or anything else besides the text itself.

            2.  **Tone and Style:**
            *   Adopt a reflective, empathetic, and slightly analytical tone.
            *   Speak directly to the player using "you" (e.g., "Looking back at your journey, [Player Name if available, otherwise 'adventurer'], it seems you often...").
            *   The review should feel personal and tailored, as if you've been a quiet companion observing their emotional responses.

            
            3.  **Content Focus (Based on AGGREGATED_EMOTIONAL_ARC):**
            *   **Synthesize the Core Emotional Journey:** Analyze the sequence of emotional summaries in AGGREGATED_EMOTIONAL_ARC. Identify recurring emotional themes, how the player's emotional responses might have evolved or remained consistent, and any significant emotional turning points.
            *   **Identify Dominant Personality Traits:** Based on the emotional patterns, infer and discuss the player's likely personality traits as they manifested during the saga (e.g., "Your responses suggest a deeply cautious nature," or "A clear pattern of empathetic decision-making indicates a strong compassionate streak in you.").
            *   **Highlight Emotional Strengths and Skills:** Acknowledge any emotional skills or strengths the player demonstrated (e.g., resilience in the face of adversity, ability to remain calm under pressure, capacity for deep empathy, courageous conviction).
            *   **Acknowledge Emotional Struggles or Challenges:** Gently point out any emotional struggles or patterns that might have been challenging for the player (e.g., "There were moments where it seemed you struggled with uncertainty," or "At times, a tendency towards impulsiveness appeared to shape your reactions.").
            *   **Offer Balanced Observations/Advice:** Provide observations that are neither overly praiseful nor harshly critical. The goal is gentle, constructive insight. For example, "This tendency to prioritize logic, while often a strength, sometimes seemed to create internal conflict when faced with purely emotional dilemmas." or "Your ability to find hope in difficult situations was remarkable, though it's worth reflecting if this optimism sometimes led to underestimating risks." Offer observations that the player might find useful about their approach or reactions.
            *   **Concluding Thought:** End with a thoughtful, summary statement about their overall emotional journey or what they might take away from it.

            4.  **Key Constraints:**
            *   **Second Person:** Address the player as "you." If PLAYER_NAME is available, use it in the greeting.
            *   **Based ONLY on AGGREGATED_EMOTIONAL_ARC:** Do not invent story events or infer details beyond what the emotional summaries provide. The review is about their emotional processing, not their specific in-game achievements unless directly reflected in the emotional summaries.
            *   **Balanced Perspective:** Avoid being excessively positive or negative. Aim for genuine, constructive reflection.
            *   **No Spoilers:** The reflection should be about the player's internal journey, not a recap of the saga's plot.
            *   **No Questions:** The generated text must not ask any questions or prompt further user input. It should end definitively.

            Example Snippets (Your actual output will be more cohesive and detailed, forming a few paragraphs):
            "Looking back at your journey in [SAGA_TITLE], [Player Name, or 'adventurer' if null], it's clear that you approached many situations with a distinct sense of [observed trait, e.g., 'cautious optimism']. The emotional records show that while you often [observed pattern, e.g., 'sought peaceful resolutions'], there were moments, particularly [general situation, e.g., 'when allies were threatened'], where a fierce [observed emotion, e.g., 'protectiveness'] emerged. This suggests a personality that values [inferred value, e.g., 'harmony but is fiercely loyal']."
            "One of your notable emotional skills appears to be [skill, e.g., 'your resilience in the face of setbacks']. Even when [general struggle, e.g., 'plans went awry, as indicated by moments of frustration in your emotional responses'], you often found a way to [positive outcome, e.g., 'regroup and adapt']. However, the tendency to [observed challenge, e.g., 'internalize blame during difficult choices'] seemed to be a recurring struggle. Perhaps reflecting on these moments could offer insights into [gentle advice, e.g., 'how you navigate responsibility under pressure']."
            "Ultimately, your emotional journey through this saga was marked by [summary statement, e.g., 'a growing confidence in your intuitive judgments']. It was a privilege to witness."
            
            """.trimIndent()
    }
}
