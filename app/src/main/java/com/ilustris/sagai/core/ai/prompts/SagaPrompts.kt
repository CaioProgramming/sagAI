package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.joinMessage

object SagaPrompts {
    fun details(saga: SagaData) = saga.storyDetails()

    fun SagaData.storyDetails() =
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
        saga: SagaData,
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
            
        **Example JSON Output (Remember to fill `message.text` with your generated content):**
        ```json
        {
          "message": {
            "actId": [REPLACE_WITH_CURRENT_ACT_ID],
            "chapterId": [REPLACE_WITH_CURRENT_CHAPTER_ID],
            "characterId": [REPLACE_WITH_PLAYER_CHARACTER_ID],
            "id": [REPLACE_WITH_NEXT_MESSAGE_ID],
            "sagaId": [REPLACE_WITH_CURRENT_SAGA_ID],
            "senderType": "NARRATOR",
            "speakerName": null,
            "text": "As the echoes of your latest actions settle, a profound stillness descends upon your path. The intricate threads of this saga, woven through countless decisions, now find their fated resting place. Though mysteries may linger in the whispers of the wind, your journey through [mention recent location/conflict] has reached its undeniable end. The story you've lived here is now complete. Thank you for your courage. The End.",
            "timestamp": [REPLACE_WITH_CURRENT_TIMESTAMP]
          },
          "newCharacter": null,
          "shouldCreateCharacter": false,
          "shouldEndSaga": true
        }
        """
}
