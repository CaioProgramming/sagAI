package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
// Added import for GenrePrompts if it's not already there implicitly by package
import com.ilustris.sagai.core.ai.prompts.GenrePrompts

object SagaPrompts {
    fun details(saga: Saga) = saga.storyDetails()

    fun Saga.storyDetails() =
        """
        Title: $title
        Description: ${description.trimEnd()}
        Genre: $genre
        """.trimIndent()

    private data class SagaGenerationContext(
        val sagaSetup: SagaForm,
        val initialPlayerInteractionLog: String,
    )

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
                SagaGen::class.java, // Ensure SagaGen is the correct class representing the desired output
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

            **Overall Output Format:**
            *   Your entire response MUST be a single, valid JSON object that strictly conforms to the structure of `SagaGen`.
            *   A template representing the fields you need to generate is shown below (system-managed fields that you should NOT generate are excluded from this template).
            *   Do NOT include any text outside this JSON object.

            **Part 1: Populating the `saga` object (within `SagaGen`)**
            *   **`title`**: Use `SAGA_SETUP.sagaDraft.title`.
            *   **`genre`**: Use `SAGA_SETUP.sagaDraft.genre`.
            *   **`description`**: Generate an engaging synopsis (max 75 words). This synopsis should establish the adventure's setting, core theme, and outline the player's journey. Base this on SAGA_SETUP and INITIAL_PLAYER_INTERACTION_LOG.
            *   **`story`**: Generate a more detailed narrative introduction to the world and its current state (1-3 paragraphs). Describe the atmosphere, initial situation, and any immediate hooks or mysteries. Expand from SAGA_SETUP.sagaDraft.description and INITIAL_PLAYER_INTERACTION_LOG.
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

    fun introductionGeneration(saga: SagaContent) =
        """
        Write a introduction text for the story,
        presenting the world building,
        and surface overview of our objective.
        The introduction should encourage the player to start the adventure.
        
        **Saga and Character Context**
        ${saga.toJsonFormat()}
        
        The introduction should include:
        1.  Main character introduction.
        2.  The primary antagonist or opposing force.
        3.  The main quest or objective for the player characters.
        4.  Potential for moral dilemmas or significant choices.
        5.  An indication of the adventure's scope and potential.
        Target a description length of 50 words, ensuring it captures the essence of a playable RPG experience.
        """.trimIndent()

    private data class SagaEndCreditsContext(
        val sagaTitle: String,
        val playerInfo: Character?,
        val fullSagaStructure: List<ActContent>,
    )

    fun endCredits(saga: SagaContent): String {
        val contextData =
            SagaEndCreditsContext(
                sagaTitle = saga.data.title,
                playerInfo = saga.mainCharacter,
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

    fun iconDescription(
        saga: Saga,
        character: Character,
    ) = """
                                Your task is to act as an AI Image Prompt Engineer. You will generate a highly detailed and descriptive text prompt for an AI image generation model.
                                This final text prompt will be used to create a **Dramatic, Close-up Character Icon** for the saga "${saga.title}" (Genre: ${saga.genre.title}).

                                **CRITICAL CONTEXT FOR YOU (THE AI IMAGE PROMPT ENGINEER):**

                                1.  **Foundational Art Style (Mandatory):**
                                    *   The primary rendering style for the icon MUST be: `${GenrePrompts.artStyle(saga.genre)}`.

                                2.  **Specific Color Application Instructions (Mandatory):**
                                    *   The following rules dictate how the genre's key colors are applied: `${GenrePrompts.getColorEmphasisDescription(
        saga.genre,
    )}`.
                                    *   **Important Clarification on Color:**
                                        *   The color rules from `getColorEmphasisDescription` are primarily for:
                                            *   The **background's dominant color**.
                                            *   **Small, discrete, isolated accents on character features** (e.g., eyes, specific clothing patterns, small tech details, minimal hair streaks).
                                        *   **CRUCIAL: DO NOT use these genre colors to tint the character's overall skin, hair (beyond tiny accents), or main clothing areas.** The character's base colors should be preserved and appear natural.
                                        *   Lighting on the character should be primarily dictated by the foundational art style (e.g., chiaroscuro for fantasy, cel-shading for anime) and should aim for realism or stylistic consistency within that art style, not an overall color cast from the genre accents. The genre accents are design elements, not the primary light source for the character.

                                3.  **Visual Reference Image (Your Inspiration for Composition & Details - Not for Direct Mention in Output):**
                                    *   You WILL have access to a Visual Reference Image (Bitmap).
                                    *   From this, draw inspiration for:
                                        *   **Compositional Framing:** (e.g., extreme close-up, angle). Adapt for an icon.
                                        *   **Background Characteristics (to be colored by genre rules):** (e.g., solid, abstract, subtly textured).
                                        Adapt for a simple icon background.
                                        *   **Compatible Visual Details & Mood:** (e.g., subtle textures, expressions) that fit the art style and color rules.
                                        * Also you will have access to character visual reference to provide a more precise description.

                                4.  **Character Details (Provided Below):** The character to be depicted.

                                **YOUR TASK (Output a single text string for the Image Generation Model):**
                                Generate a single, highly detailed, unambiguous, and visually rich English text description. This description must:
                                *   Integrate the **Character Details**.
                                *   Render the scene in the **Foundational Art Style**.
                                *   Explicitly describe the **background color** and the **specific character accents** using the genre colors as per `getColorEmphasisDescription`.
                                *   Ensure the description implies that the character's base colors (skin, hair, main clothing) are preserved and not tinted by the accent colors. Lighting on the character should be consistent with the art style, with genre colors applied as specific, non-overwhelming details.
                                *   Incorporate **Compositional Framing** and compatible **Visual Details & Mood** inspired by the Visual Reference Image.
                                *   **CRUCIAL: Your output text prompt MUST NOT mention the Visual Reference Image.** It must be a self-contained description.

                                **Saga Context (for thematic consistency):**
                                ${saga.toJsonFormat()}

                                **Main Character Details (to be depicted):**
                                ${character.toJsonFormatExcludingFields(listOf("backstory", "image", "sagaId", "joinedAt", "hexColor", "id"))}

                                ---
                                **Example of how your output prompt for the image generator might start (VARY BASED ON YOUR ANALYSIS AND THE SPECIFIC GENRE/CHARACTER):**
                                "Dramatic icon of [Character Name], a [Character's key trait/role]. Rendered in a distinct [e.g., 80s cel-shaded anime style with bold inked outlines].
                                The background is a vibrant [e.g., neon purple as per genre instructions].
                                Specific character accents include [e.g., luminous purple cybernetic eye details and thin circuit patterns on their black bodysuit, as per genre instructions].
                                The character's skin tone remains natural, and their primary hair color is [e.g., black], with lighting appropriate to the cel-shaded style.
                                The composition is an [e.g., intense extreme close-up]. [Character Name] has [e.g., piercing blue eyes (unless overridden by genre accent color for eyes)]..."
                                ---
                                YOUR SOLE OUTPUT MUST BE THE GENERATED IMAGE PROMPT STRING.
        """.trimIndent()

    private data class SagaReviewContext(
        val playerCharacter: Character?,
        val sagaInfo: Saga,
        val playerMessageCount: Int,
        val messageTypeCountsText: String,
        val dominantMessageType: String,
        val sagaActsStructure: List<ActContent>,
        val topInteractiveCharactersText: String,
        val topMentionedCharactersText: String,
        val totalUniqueNPCsEncountered: Int,
        val languageDirective: String,
    )

    fun reviewGeneration(
        saga: SagaContent,
        playerMessageCount: Int,
        messageTypesRanking: List<Pair<SenderType, Int>>,
        topCharacters: List<Pair<Character, Int>>,
        topMentions: List<Pair<Character, Int>>,
    ): String {
        val reviewContextData =
            SagaReviewContext(
                playerCharacter = saga.mainCharacter,
                sagaInfo = saga.data,
                playerMessageCount = playerMessageCount,
                messageTypeCountsText = messageTypesRanking.joinToString(";") { "${it.first.name} : ${it.second}" },
                dominantMessageType = messageTypesRanking.firstOrNull()?.first?.name ?: "N/A",
                sagaActsStructure = saga.acts,
                topInteractiveCharactersText = topCharacters.joinToString(";") { "name: ${it.first.name}, messageCount: ${it.second}" },
                topMentionedCharactersText = topMentions.joinToString(";") { "name: ${it.first.name}, mentions: ${it.second}" },
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
            Your goal is to generate a deeply personal, engaging, and celebratory textual review of the player's completed saga, in the style of a "Spotify Wrapped" year-end summary.

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
                * **`playStyle`**: Based on your analysis of the player's DOMINANT_MESSAGE_TYPE and MESSAGE_TYPE_COUNTS_TEXT (using the provide
            """.trimIndent()
        // This trimIndent() might be misaligned if the content above is not fully pasted.
    }
}
