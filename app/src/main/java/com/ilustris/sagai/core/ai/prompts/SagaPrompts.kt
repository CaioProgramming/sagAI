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
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.detail.data.model.Review
import kotlin.jvm.java
import kotlin.text.appendLine

object SagaPrompts {
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
            "This final text prompt will be used to create a **Dramatic Icon** for the saga \"${saga.title}\" (Genre: ${saga.genre.name}).",
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
}
