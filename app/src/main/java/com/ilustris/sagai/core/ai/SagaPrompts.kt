package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm

object SagaPrompts {
    fun details(saga: SagaData) = saga.storyDetails()

    fun SagaData.storyDetails() =
        """
        You are set in the RPG world of:   
        Title: $title
        Description: ${description.trimEnd()}
        """

    fun SagaForm.storyDetails() =
        """
        Adventure Details:
        Title: $title
        Description: $description
        Genre: $genre
        """

    fun sagaGeneration(saga: SagaForm) =
        """
        Develop a synopsis that engage the player to joins the adventure.
        This synopsis should establish the adventure's setting and core theme,
        outlining the journey players will undertake.
        Do not include specific characters or plot points,
        but rather focus on the overarching narrative and the world in which the adventure takes place.
        
        ${saga.storyDetails()}
        
        The synopsis should include:
        1.  An engaging hook that sets the scene.
        3.  The main quest or objective for the player characters.
        5.  An indication of the adventure's scope and potential for a grand finale.
        
        Target a short synopsis with a maxium of 75 words, ensuring it engages the player to join the RPG experience.
        """

    fun narratorGeneration(
        saga: SagaData,
        messages: List<String>,
    ) = """
        Write a narrator break for the story, summarizing the events that have happened so far.
        
        ${saga.storyDetails()}
        
        Use the following messages as context:
        ${messages.joinToString(separator = "\n") { it }}
        
        The narrator break should include:
        1.  A brief summary of the main events that have happened so far.
        2.  A recap of the main character's actions and decisions.
        3.  An indication of the current state of the world and the main character's situation.
        Target a description length of 50 words, ensuring it captures the essence of a playable RPG experience.
        """

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
        """

    fun chapterGeneration(
        sagaData: SagaData,
        messages: List<String>,
    ) = """
        Write a new chapter to continue the adventure in a role-playing game (RPG) set in the world of ${sagaData.title}.
        The story is ${sagaData.description}.
        The genre is ${sagaData.genre.name}.
        
        Write a overview of what should be the next events connecting with the past events from the messages.
        The last messages in the conversation were:
        ${messages.joinToString("\n") { it }}
        
        Your summary should be in character, reflecting the context of the story and the events that have happened so far.
        The chapter should include:
        1.  A brief summary of the main events that have happened so far.
        2.  A recap of the main character's actions and decisions.
        3.  An indication of the current state of the world and the main character's situation.
        Target a description length of 100 words, ensuring it captures the essence of a playable RPG experience.
        """.trimIndent()
}
