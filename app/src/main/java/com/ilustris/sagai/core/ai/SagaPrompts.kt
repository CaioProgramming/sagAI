package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
import com.ilustris.sagai.features.characters.data.model.CharacterPose
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm

object SagaPrompts {
    fun details(saga: SagaData) = saga.storyDetails()

    fun SagaData.storyDetails() =
        """
        Title: $title
        Description: ${description.trimEnd()}
        Genre: $genre
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
        
        You MUST fill the visuals field:
        //WRITE THE FIELDS IN ENGLISH
        // FOLLOW this structure to fill the visuals field:
        {
          "illustrationVisuals": { // This object will describe the visual aspects for generating saga illustrations.
            "characterPose": "string", // Describe the main character's pose (e.g., "Standing heroically with crossed arms", "Crouching cautiously", "Running frantically").
            "characterExpression": "string", // Describe the character's facial expression (e.g., "Determined and serious", "Curious and slightly surprised", "Melancholy with a touch of hope").
            "environmentDetails": "string", // Describe the main environment of the illustration (e.g., "Rainy cyberpunk street with neon signs", "Ancient elven forest with gnarled trees and magic mist", "Deserted and rusty spaceship interior").
            "lightingDetails": "string", // Describe the lighting of the scene (e.g., "Dramatic neon backlight from behind the character", "Soft moonlight filtering through leaves", "Harsh overhead fluorescent lights").
            "colorPalette": "string", // Describe the dominant color palette (e.g., "Dominance of electric blues, purples, and dark grays, with splashes of red", "Earthy greens, browns, and golden hues, with mysterious purples").
            "foregroundElements": "string", // Describe elements in the foreground that add depth (e.g., "Raindrops streaking across the view, smoke rising from manholes", "Gnarled roots on the ground, small floating light orbs").
            "backgroundElements": "string", // Describe elements in the background that enrich the scene (e.g., "Towering skyscrapers with glass facades and LED panels displaying distorted ads", "Silhouetted mountains, a distant burning city").
            "overallMood": "string" // Describe the overall mood or emotion of the illustration (e.g., "Gritty and tense", "Mysterious and enchanting", "Desolate yet hopeful").
          }
        }
        """

    fun wallpaperGeneration(
        saga: SagaData,
        mainCharacter: Character,
    ) = """
        ${GenrePrompts.artStyle(saga.genre)}
        ${CharacterFraming.EPIC_WIDE_SHOT.description}
        Featuring:
        Appearance: ${mainCharacter.details.appearance}
        Expression: ${CharacterExpression.random().description}
        Pose: ${CharacterPose.random().description}
        Color palette: ${saga.visuals.colorPalette}
        
        Environment: ${saga.visuals.environmentDetails}
        Background: ${saga.visuals.backgroundElements}
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
        chapters: List<Chapter>,
        characters: List<Character>,
    ) = """
        Write a new chapter to continue the adventure in a role-playing game (RPG) set in the world of ${sagaData.title}.
        The story is ${sagaData.description}.
        The genre is ${sagaData.genre.name}.
        
        Write a overview of what should be the next events connecting with the past events from the messages.
        The last messages in the conversation were:
        ${messages.joinToString("\n") { it }}
        
        You can use the following chapters as context:
        ${chapters.joinToString("\n") { it.toJsonFormat() }}
        
        Your summary should be in character, reflecting the context of the story and the events that have happened so far.
        The chapter should include:
        1.  A brief summary of the main events that have happened so far.
        2.  A recap of the main character's actions and decisions.
        3.  An indication of the current state of the world and the main character's situation.
        Target a description length of 100 words, ensuring it captures the essence of a playable RPG experience.
        
        Saga photography:
        Color palette: ${sagaData.visuals.colorPalette},
        illumination: ${sagaData.visuals.lightingDetails},
        environment: ${sagaData.visuals.environmentDetails}
        
        On the visualDescription field Write a prompt of a illustration that defines this chapter.
        YOU MUST USE THE SAGA PHOTOGRAPHY TO IMPROVE YOUR PROMPT
        You can use the characters in the story to improve your prompt:
        USE ONLY RELEVANT CHARACTERS FROM THE CURRENT CHAPTER
        ${characters.map { it.toJsonFormat() }}}
        """.trimIndent()

    fun loreGeneration(
        saga: SagaData,
        messages: List<String>,
        character: Character,
    ) = """"
        You are the "Saga Chronicler" for a text-based RPG.
        Your primary role is to efficiently and accurately summarize key narrative elements from a given segment of the saga's conversation history,
        **integrating it with the existing long-term memory.**
        This updated summary will serve as a continuous, evolving long-term memory for the main Saga Master AI,
        ensuring narrative consistency over extended gameplay sessions and across multiple chapters.

        Your summary must be concise, accurate, and focus only on the most crucial details for ongoing
        story progression, character consistency, and plot points that remain relevant across chapters.
        DO NOT include conversational filler, minor details, or repetitive elements that are no longer critical.
        Focus on WHAT happened, WHERE it happened (if significant), WHO was involved, and WHY it's important for the future.
        
        SAGA CONTEXT:
        // This section provides high-level information about the saga to help you contextualize the events.
        Title: ${saga.title}
        Description: ${saga.description}
        Player Character: $character
        
        CURRENT LORE SUMMARY (Existing Long-Term Memory):
        / This is the most recent condensed long-term memory of the saga's key events and states.
        // Use this as the foundational base upon which to integrate and update with new information.
        // If this is the first summary, this section will be empty or contain an initial saga premise.
        ${saga.lore}
        
        CONVERSATION HISTORY (FOR CONTEXT ONLY, do NOT reproduce this format in your response):
        ${messages.joinToString("\n") { it }}

        GENERATE THE UPDATED SAGAS LORE/MEMORY AS A CONCISE LIST OF BULLET POINTS.
        This new summary should be a synthesis of the 'CURRENT LORE SUMMARY' and the 'CONVERSATION HISTORY TO SUMMARIZE'.
        Prioritize:
        - Major plot developments and revelations.
        - Key character interactions or changes in relationships (e.g., trust gained/lost).
        - Main objectives, quests, or unsolved mysteries that are still active.
        - Introduction of new significant NPCs or locations.
        - Important items acquired or lost.
        - Overall shifts in the saga's tone or major turning points.
        - Ensure the summary does not exceed ~200-300 words to maintain conciseness.
        """"
}
