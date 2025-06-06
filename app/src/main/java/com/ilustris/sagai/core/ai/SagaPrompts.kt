package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.home.data.model.SagaData

object SagaPrompts {
    fun sagaGeneration(saga: SagaData) =
        """
        Develop a synopsis that engage the player to joins the adventure.
        This synopsis should establish the adventure's setting and core theme,
        outlining the journey players will undertake.
        Do not include specific characters or plot points,
        but rather focus on the overarching narrative and the world in which the adventure takes place.
        
        Adventure Details:
        Title: ${saga.title}
        Description: ${saga.description}
        Genre: ${saga.genre}
        
        
        The synopsis should include:
        1.  An engaging hook that sets the scene.
        3.  The main quest or objective for the player characters.
        5.  An indication of the adventure's scope and potential for a grand finale.
        
        Target a short synopsis with a maxium of 75 words, ensuring it engages the player to join the RPG experience.
        """

    fun narratorGeneration(
        saga: SagaData,
        messages: List<Message>,
    ) = """
        Write a narrator break for the story, summarizing the events that have happened so far.
        Adventure Details:
        1.  **Title:** ${saga.title}
        2.  **Description:** ${saga.description}
        3.  **Genre:** ${saga.genre.name}
        4.  **Last Messages context:** ${messages.joinToString("\n") { it.text }}
        
        The narrator break should include:
        1.  A brief summary of the main events that have happened so far.
        2.  A recap of the main character's actions and decisions.
        3.  An indication of the current state of the world and the main character's situation.
        Target a description length of 50 words, ensuring it captures the essence of a playable RPG experience.
        """
}
