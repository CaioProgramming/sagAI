package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterPose
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.joinMessage
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.meaning
import com.ilustris.sagai.features.saga.chat.ui.components.description
import com.ilustris.sagai.features.saga.detail.data.model.Review

object SagaPrompts {
    fun details(saga: Saga) = saga.storyDetails()

    fun Saga.storyDetails() =
        """
        Title: $title
        Description: ${description.trimEnd()}
        Genre: $genre
        """.trimIndent()

    fun SagaForm.storyDetails() =
        """
        Adventure Details:
        Title: $title
        Description: $description
        Genre: $genre
        """.trimIndent()

    fun sagaGeneration(saga: SagaForm) =
        """
        Develop a synopsis that engage the player to joins the adventure.
        This synopsis should establish the adventure's setting and core theme,
        outlining the journey players will undertake.
        Do not include specific characters or plot points,
        but rather focus on the overarching narrative and the world in which the adventure takes place.
        
        ${saga.storyDetails()}
        
        Starring:
        ${CharacterPrompts.details(saga.character)}
        
        The synopsis should include:
        1.  An engaging hook that sets the scene.
        3.  The main quest or objective for the player characters.
        5.  An indication of the adventure's scope and potential for a grand finale.
        
        Target a short synopsis with a maxium of 75 words, ensuring it engages the player to join the RPG experience.
        """.trimIndent()

    fun introductionGeneration(
        saga: Saga,
        character: Character?,
    ) = """
        Write a introduction text for the story,
        presenting the world building,
        and surface overview of our objective.
        The introduction should encourage the player to start the adventure.
        
        ${saga.storyDetails()}
        
        ${CharacterPrompts.details(character)}
        
        The introduction should include:
        1.  Main character introduction.
        2.  The primary antagonist or opposing force.
        3.  The main quest or objective for the player characters.
        4.  Potential for moral dilemmas or significant choices.
        5.  An indication of the adventure's scope and potential.
        Target a description length of 50 words, ensuring it captures the essence of a playable RPG experience.
        """.trimIndent()

    fun endingGeneration(sagaContent: SagaContent) =
        """
        You are the Saga Master for the saga "${sagaContent.data.title}"
        Due to external system constraints, this saga must now conclude immediately.
        Your task is to generate a final, conclusive message for the player, providing a sense of closure even if the ultimate goal was not fully met.
        **CONTEXT FOR FORCED CONCLUSION MESSAGE GENERATION:**
        
        1.  **Player Information:**
            ${sagaContent.mainCharacter.toJsonFormat()}
        
        2.  **Recent Saga Context (Most Critical Summary):**
            Current final act description:
            ${sagaContent.acts.last().toJsonFormat()}
            ------
            Last chapter description:
            ${sagaContent.chapters.last().toJsonFormat()}
            Last Events:
            ${sagaContent.timelines.takeLast(10)}
            Last Messages:
            ${
            sagaContent.messages.takeLast(10).map { it.joinMessage() }
                .joinToString { it.formatToString() }
        }
  
            **INSTRUCTIONS FOR GENERATING THE FORCED CONCLUSION MESSAGE:**
            
            1.  **Sender & Flag:** This must be the *final* message of the saga. The `senderType` MUST be `NARRATOR`. The `shouldEndSaga` flag in the JSON MUST be set to `true`.
            2.  **Tone & Perspective:** Adopt a reflective, perhaps slightly bittersweet or fated tone. Do NOT mention any technical limitations, message counts, or "forced" reasons. Instead, frame the conclusion narratively. Use phrases that suggest the story has reached its natural (or destined) resting point, or that the current chapter of the journey has found its end.
            3.  **Content Requirements:**
                * Explicitly state that this chapter of the journey or the saga itself has reached its conclusion.
                * Briefly acknowledge the player's efforts and the path they've walked.
                * Provide a sense of closure to the immediate situation, even if grand mysteries remain unsolved.
                * The message should be concise and conclusive.
            4.  **Finality:** This message MUST NOT ask any questions or prompt any further player input or actions. It is a definitive "The End."
        """

    fun endCredits(saga: SagaContent) =
        """
        You are the Saga Master for the saga "${saga.data.title}" The saga has concluded.
        and your final task is to generate a deeply personal, appreciative, and retrospective **text (a plain string)** directly for the user.
        This text will serve as the "credits" or "thank you" message for their journey through the saga.
        It should be entirely focused on the player's overall experience, separate from the in-story narrative.

        **CONTEXT FOR GENERATING THE CREDIT TEXT:**
        
        1.  **Player Information:**
            ${saga.mainCharacter.toJsonFormat()}
        
        2.  **Key Saga Summaries for Synthesis:**
            Below are the crucial summarized moments from the saga, structured by Act and Chapter.
            **You will not receive pre-defined "highlights" or "pivotal moments";
            your task is to synthesize them from these summaries.
            ** Use this context to **infer the ultimate goal of the saga** that was achieved, and to **identify and celebrate** the player's most significant deeds, their overarching playstyle, and personality throughout the entire saga.
        
            ${ActPrompts.actSummaries(saga)}
  
            *(Ensure all summaries are concise and directly relevant to the player's journey, making sure the total input fits within the token budget. The goal is signal, not exhaustive detail.)*
        
        **INSTRUCTIONS FOR GENERATING THE FINAL OUTPUT (PLAIN TEXT STRING):**
        
        1.  **Output Format:** Your entire response MUST be **ONLY the plain text string** of the credit message. Do NOT include any JSON, special formatting like Markdown headers or bullet points, or anything else besides the text itself.
        2.  **Content Tone & Focus:**
            * Adopt a personal, warm, and highly congratulatory tone, speaking directly to the user (breaking the fourth wall).
            * **Infer and briefly mention the ultimate goal that was achieved** based on the provided Act and Chapter summaries and the saga's resolution.
            * **Synthesize and celebrate** 3-5 of the player's most significant actions, choices, or achievements throughout the *entire* saga, drawing directly from the provided Act and Chapter summaries. These "highlights" should reflect the player's impact and key decisions.
            * **Reflect on** the player's overall playstyle and personality traits as observed through their character's actions during the saga.
            * Conclude with a heartfelt message of thanks for their dedication and for bringing the saga to life.
        3.  **Finality:** The generated text MUST NOT ask any questions or prompt further user input. It should end definitively.
        
        **Example of Expected Plain Text Output (Your actual output will be longer):**
        
        "Ah, [Nome do Jogador]! Que jornada incrível foi essa em Data.seek. Sua aventura culminou na [mencionar objetivo final inferido], um feito e tanto! Lembro-me claramente de quando você [mencionar ação pivotal 1 - inferida de um resumo de Ato/Capítulo], e a forma como você [mencionar ação pivotal 2 - inferida de outro resumo] mudou tudo. Sua [mencionar estilo de jogo] e sua [mencionar personalidade] foram a verdadeira força motriz desta saga. Foi uma honra vivenciar essa história com você. Muito obrigado por jogar. A lenda de Data.seek, moldada por suas mãos, agora é completa. O FIM."
        
                """

    fun reviewGeneration(
        saga: SagaContent,
        playerMessageCount: Int,
        messageTypesRanking: List<Pair<SenderType, Int>>,
        topCharacters: List<Pair<Character, Int>>,
        topMentions: List<Pair<Character, Int>>,
    ) = """
        Your task is to act as a **Saga Journey Reviewer and Analyst** for '${saga.data.title}'.
        Your goal is to generate a deeply personal, engaging, and celebratory textual review of the player's completed saga, in the style of a "Spotify Wrapped" year-end summary.

        You will receive pre-analyzed statistics and key insights about the player's journey.
        Your output MUST be a **single JSON object** containing distinct named keys, each holding the narrative text for a specific section of the player's review.
        
        ---
        
        **CONTEXT AND ANALYZED PLAYER DATA (Input from your Application):**
        
        1.  **PLAYER CHARACTER:**
            ${saga.mainCharacter.toJsonFormat()}
        
        2.  **SAGA INFORMATION:**
            ${saga.data.toJsonFormat()}
        
        3.  **PLAYER INTERACTION STATISTICS (PRE-ANALYZED BY APPLICATION):**
            * **Total Player Messages Sent:** $playerMessageCount
            * **Message Type Counts:**
            ${messageTypesRanking.joinToString(";\n") { "${it.first.name} : ${it.second}" }}
            * **Dominant Message Type:** ${messageTypesRanking.first().first.name}
            * **Acts Overview:**
            ${ActPrompts.actSummaries(saga)}

        
        4.  **CHARACTER INTERACTION STATISTICS (PRE-ANALYZED BY APPLICATION):**
            * **Top 3-5 Most interactive Characters:
            [
                ${topCharacters.joinToString { "name: ${it.first.name}, messageCount: ${it.second}" }} 
            ]**
            * **Top 3-5 Most Mentioned Characters: 
            [
                ${topMentions.joinToString { "name: ${it.first.name}, mentions: ${it.second}" }}
            ]
            * **Total Unique NPCs Encountered (Optional):** ${saga.characters.size}
        
        ---
        
        **INSTRUCTIONS FOR GENERATING THE JSON OUTPUT:**
        
        1.  **Content for Each Text Field:**
            * **`introText`**: Welcome the player to their saga Recap. 
            Congratulate them on completing the saga.
            Briefly tells that now he will overview his achievements on the saga.
            Set the celebratory tone.
            * **`playStyle`**: Analyze `Dominant Message Type` and `Message Type Percentages`. Describe what this reveals about the player's core interaction style, personality, and impact within the game. Use engaging language.
              ** consider the meaning of the types to write this field:
              ${SenderType.entries.joinToString("\n") { "${it.name}: ${it.meaning()}" }}
            * **`topCharacters`**: Highlight the `Top 3-5 Most Mentioned Characters`. Comment on the nature of their relationship with the player or why they were prominent. If the player character (`Vyz`) is in the top mentions, celebrate their own focus on their internal journey/actions.
            * **`actsInsights`**: Write a personal and concise overview about the acts events, highlight emotional and groundbreaking moments.**
            * **`conclusionText`**: Finalize with a heartfelt message of thanks for their dedication and for bringing the saga to life.
            Briefly recap the overall achievement in ${saga.data.title} using the `${saga.data.description}`.
            Appreciate the player for their dedication at the journey and for bringing the saga to life.
        3.  **Tone & Style:**
            * Maintain an enthusiastic, celebratory, appreciative, and slightly "wrapped" style (e.g., "Quem ditou o ritmo da sua jornada?", "Seu Top Personagem do Ano!").
            * Speak directly to the player.
            * The language should be ${GenrePrompts.conversationDirective(saga.data.genre)}.
        4.  **Exclusions:** Do NOT ask questions, provide choices, or generate any other content beyond the review narrative itself. Do not use external information not provided in the input. Ensure the JSON is valid and well-formed.
        
        FOLLOW THIS EXACTLY STRUCTURE:
        ${toJsonMap(Review::class.java)}
        """.trimIndent()

    fun iconDescription(
        saga: Saga,
        character: Character,
    ) = """
        
        Your task is to act as an AI Image Prompt Engineer specializing in generating concepts for **Dramatic Character Portraits** for the saga "${saga.title}" (Genre: ${saga.genre.title}).
        You will receive a character's description and a brief context for their current mood or situation.
        Your goal is to convert this information into a single, highly detailed, unambiguous, and visually rich English text description.
        This description will be directly used as a part of a larger prompt for an AI image generation model.
        
        YOUR SOLE OUTPUT MUST BE THE GENERATED IMAGE PROMPT STRING. DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS. PROVIDE ONLY THE RAW, READY-TO-USE IMAGE PROMPT TEXT.
        
        **Crucially, this description MUST be formulated to be compatible with a 'Dramatic Portrait' framing,
        conveying the essence and mood of the character in a single, impactful image.
        Adhere strictly to the provided 'Story Theme' (${saga.genre.title}).**
        
        **Character Context:**
        ${character.toJsonFormat()}
        3.  **Current Mood/Situation (for dramatic pose/expression):** ${CharacterPose.random().description}
        
        **Guidelines for Conversion and Expansion:**
        
        1.  **Translate Accurately:** Translate all Portuguese values from the input fields into precise English.
        2.  **Infer Visuals from Context:** **This is critical.** From the `Character Description` and `Current Mood/Situation`, infer and elaborate on:
            * **Primary Setting/Environment (for background):** The background should be **extremely minimalist and somber**, serving only to enhance the character's presence. Consider a **plain, dark solid color (deep grey, black, or a muted dark tone)** or a **subtle, dark gradient**. Any implied environment should be heavily out of focus or suggested through minimal abstract shapes and dark tones. The focus *must* remain entirely on the character's portrait.
            * **Dominant Mood/Atmosphere & Lighting Theme: ${GenrePrompts.moodDescription(saga.genre)}.
            * **Key Pose/Expression:** Based on `Current Mood/Situation`, generate a **dynamic, expressive, and dramatic pose/facial expression** that captures the character's essence in a portrait.
            * **Important Objects/Elements:** Include any significant items, or symbols mentioned in the description, ensuring they are either held by or directly related to the central character and are potential candidates for the vivid highlights.
        3.  **Integrate Character (Dominant Central Focus & Red Accents):** The primary character **MUST be the absolute central and dominant focus of the image, filling a significant portion of the frame.** Frame the character as a **close-up portrait** (e.g., headshot to waist-up, or very close full body if the pose demands it, but always prioritizing the character's face/expression). **Incorporate vivid red details on or around the character that are thematically relevant**, ensuring they are the immediate focal points against the somber background.
        4.  **Integrate Character (Dominant Central Focus, Artistic Lighting & Subtle Cybernetics - Tight Framing):** The primary character **MUST be the absolute central and dominant focus**, **framed tightly as a close-up portrait (from the chest or shoulders up), filling a significant portion of the frame.** Emphasize **strong, artistic lighting in shades of purple that defines their form and creates dramatic shadows**, similar to the use of red in your example. Their expression should be clearly visible and convey the mood. **Crucially, incorporate subtle cybernetic implants and enhancements as elements of fusion between human and machine.** These details should be visible on the **face, neck, eyes (e.g., glowing pupils or integrated displays), and lips (e.g., metallic sheen or subtle integrated tech)**, adding to the cyberpunk aesthetic without overwhelming the character's humanity, as seen in the provided example.
        5.  **Composition for Dramatic Portrait (Tight & Centralized Focal Distance):** Formulate the prompt to suggest a **tight, portrait-oriented composition with the main character centrally and dominantly positioned, capturing a headshot or upper-body shot.** Utilize strong, focused lighting to emphasize the character, their expression, and their key elements.
        * **Suggested terms to use:** "tight shot," "close-up portrait," "headshot," "upper body shot," "from the chest up," "shoulders up," "central composition," "minimalist background (deep purple and black)," "artistic use of purple tones," "high contrast lighting," "dramatic shadows," "single light source (purple)," "rim lighting (lighter purple)," "stylized," "graphic," "character-focused," "futuristic portrait."
        6.  **Exclusions:** NO TEXT, NO WORDS, NO TYPOGRAPHY, NO LETTERS, NO UI ELEMENTS.    
        """
}
