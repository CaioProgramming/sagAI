package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki

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

    fun wallpaperGeneration(
        saga: SagaData,
        mainCharacter: Character,
        description: String,
    ) = GenrePrompts.iconPrompt(
        genre = saga.genre,
        mainCharacter = mainCharacter,
        description = description
    ).trimIndent()

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

    fun chapterGeneration(
        sagaContent: SagaContent,
        lastAddedEvents: List<Timeline>,
    ) = """
        Write a new chapter to continue the adventure in a role-playing game (RPG) set in the world of ${sagaContent.data.title}.
        ${details(sagaContent.data)}
        
        You can use the following chapters as context:
        ${sagaContent.chapters.formatToJsonArray()}
        
        This is the current saga full timeline:
        ${sagaContent.timelines.formatToJsonArray()}
        
        The most recent events on the story was:
        ${lastAddedEvents.formatToJsonArray()}
        
        **EXISTING WORLD WIKI ENTRIES (For consistent terminology and referencing important world elements):**
        // This is a comprehensive list of all known world entities (locations, organizations, items, concepts, events, technologies, etc.) in the saga's World Knowledge Base.
        // Use this list to ensure you use the correct and consistent names for known entities when mentioning them in the chapter description. Your goal is to naturally weave these terms into the narrative where relevant.

        [ ${sagaContent.wikis.formatToJsonArray()} ]
        Always follow that structure for items in the array: 
        ${toJsonMap(Wiki::class.java)}
        
        **Your chapter description should be concise, compelling, and around 100 words.** It must capture the essence of a playable RPG experience and prepare the player for the next phase of the story.
        
        The chapter description MUST effectively summarize the latest developments by including:
        1.  **Key Events & Current Situation:** A brief summary of the main events that have just happened, clearly indicating the current state of the world and the main character's situation. **Ensure this incorporates relevant world entities (locations, organizations, items, concepts, technologies, or events) from the 'EXISTING WORLD WIKI ENTRIES', especially if they are new or have become significantly important in this segment.**
        2.  **Main Character's Role:** A recap of the main character's (Any's) actions and pivotal decisions within this chapter's events.

        Saga photography:
        Color palette: ${sagaContent.data.visuals.colorPalette},
        illumination: ${sagaContent.data.visuals.lightingDetails},
        environment: ${sagaContent.data.visuals.environmentDetails}
        
        On the visualDescription field, Write a concise prompt for an illustration that visually defines this chapter.
        YOU MUST USE THE SAGA PHOTOGRAPHY TO IMPROVE YOUR PROMPT.
        You can use the characters in the story to improve your prompt:
        USE ONLY RELEVANT CHARACTERS FROM THE CURRENT CHAPTER.
        IMPORTANT TO USE CHARACTER APPEARANCE ON YOUR PROMPT, KEEPING CONSISTENT.
        ${sagaContent.characters.formatToJsonArray()}
        """.trimIndent()

    fun loreGeneration(
        sagaContent: SagaContent,
        messages: List<String>,
    ) = """
        "
         You are the Saga Chronicler, an AI specialized in maintaining the long-term memory and knowledge base of the 'bit to bit' RPG.
         Your task is to review the saga's current timeline and the latest segment of conversation history (e.g., last 20 messages), then generate three things:
         1. A list of NEW, significant timeline events that occurred in this conversation segment.
         2. A list of any new or significantly updated world knowledge entries.
         3. A list of any existing characters whose details have significantly changed.

         Saga Context:
         ${details(sagaContent.data)}
         
         **CURRENT LORE TIMELINE (Existing Saga's History):**
         // This is a chronological list of key events and developments that have already occurred in the saga.
         // Each entry is a concise, independent summary of a major plot point, character arc progression, or significant revelation.
         // Use this list to understand the saga's current historical state and to ensure new events are not duplicates.
         // Your task is to identify truly NEW and important events from 'CONVERSATION HISTORY TO SUMMARIZE' that are NOT already covered here.
         [
          ${sagaContent.timelines.formatToJsonArray()}
         ]
         
         CONVERSATION HISTORY TO SUMMARIZE (New Segment - e.g., last 20 messages):
         // This is the new chunk of messages that needs to be analyzed for new lore events and character updates.
         // Focus on extracting the most significant and lasting events from this segment.
         [
          ${messages.joinToString(",\n")}
         ]
         
         EXISTING WORLD WIKI ENTRIES (For reference):
         [
          ${sagaContent.wikis.formatToJsonArray()}
         ]

         EXISTING SAGA CAST (For reference, to identify character updates):
         [
          ${sagaContent.characters.formatToJsonArray()}
         ]
         
         GENERATE A SINGLE, COMPREHENSIVE JSON RESPONSE that contains a list of NEW timeline events, any new or significantly updated WORLD WIKI ENTRIES, and any updated CHARACTER DETAILS.
         Follow this EXACT JSON structure for your response. Do NOT include any other text outside this JSON.
         DO NOT Wrap objects in array with string, return a JSON Object
         {
          ${toJsonMap(LoreGen::class.java)}
         }
         
          
         **EXISTING SAGA CAST (For reference, to identify character updates):**
         // This is a list of all characters (NPCs) that are ALREADY in the saga's current cast.
         // Use this list to identify if an existing character's status (e.g., alive/dead), backstory, occupation, or personality has SIGNIFICANTLY changed due to events in the 'CONVERSATION HISTORY TO SUMMARIZE'.
         // **CRITICAL INSTRUCTIONS FOR 'characterUpdates' output:**
         // - Only include a character in 'characterUpdates' if their 'status' (e.g., if they died), 'backstory', 'occupation', or 'personality' has undergone a major, lasting change in the 'CONVERSATION HISTORY TO SUMMARIZE'.
         // - If you update a character, their 'name' in 'characterUpdates' MUST EXACTLY match their 'name' in this 'EXISTING SAGA CAST' list.
         // - Provide ONLY the fields that have changed. Do NOT include fields that remain the same.
         // - For 'backstory' or 'personality' fields, provide the *new, updated complete text* if it's changing, not just a summary of the change.        
        [ ${sagaContent.characters.formatToJsonArray()} ]
         "
        """.trimIndent()
}
