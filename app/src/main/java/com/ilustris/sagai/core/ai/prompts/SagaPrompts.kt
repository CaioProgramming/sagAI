package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.joinMessage

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
            ${sagaContent.messages.takeLast(10).map { it.joinMessage() }.joinToString { it.formatToString() }}
  
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
}
