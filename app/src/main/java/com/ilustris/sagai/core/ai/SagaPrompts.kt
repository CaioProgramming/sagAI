package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
import com.ilustris.sagai.features.characters.data.model.CharacterPose
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import com.ilustris.sagai.features.wiki.data.model.Wiki

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
        sagaContent: SagaContent,
        messages: List<String>,
    ) = """"
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
         ${sagaContent.timelines.joinToString(",\n") { it.toJsonFormat() }}
        ]
        
        CONVERSATION HISTORY TO SUMMARIZE (New Segment - e.g., last 20 messages):
        // This is the new chunk of messages that needs to be analyzed for new lore events, wiki updates, and character updates.
        // Focus on extracting the most significant and lasting events from this segment.
        // [As últimas 20 mensagens da conversa ou o segmento desde o último ponto de atualização]
        [
         ${messages.joinToString(",\n")}
        ]
        
        EXISTING WORLD WIKI ENTRIES (For reference, to avoid duplicates or identify updates):
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
        
        **EXISTING WORLD WIKI ENTRIES (For reference, to avoid duplicates or identify updates):**
        // This is a list of all world entities (locations, organizations, items, concepts, events, technologies etc.) that are ALREADY stored in this saga's World Knowledge Base.
        // Use this list to determine if an entity emerging in the 'CONVERSATION HISTORY TO SUMMARIZE' is truly new, or if it's an existing entity that needs an update.
        // **CRITICAL INSTRUCTIONS FOR 'wikiUpdates' output:**
        // - If an entity (by its 'name' or any of its 'aliases') from 'CONVERSATION HISTORY TO SUMMARIZE' already exists in THIS 'EXISTING WORLD WIKI ENTRIES' list, then:
        //   - DO NOT create a new entry for it in your 'wikiUpdates' array, UNLESS its 'description' has SIGNIFICANTLY changed or new crucial information has emerged for it in the 'CONVERSATION HISTORY TO SUMMARIZE'.
        //   - If you update an existing entity, ensure its 'name' in 'wikiUpdates' EXACTLY matches the existing entry's 'name'.
        // - If an entity from 'CONVERSATION HISTORY TO SUMMARIZE' is genuinely NEW and is NOT present in THIS 'EXISTING WORLD WIKI ENTRIES' list, then you MUST include it as a new entry in your 'wikiUpdates'.
        ${sagaContent.wikis.joinToString(",\n") { it.toJsonFormat() }}
        Follow this structure for wiki updates:
        [
          ${toJsonMap(Wiki::class.java)}
        ]
         
        **EXISTING SAGA CAST (For reference, to identify character updates):**
        // This is a list of all characters (NPCs) that are ALREADY in the saga's current cast.
        // Use this list to identify if an existing character's status (e.g., alive/dead), backstory, occupation, or personality has SIGNIFICANTLY changed due to events in the 'CONVERSATION HISTORY TO SUMMARIZE'.
        // **CRITICAL INSTRUCTIONS FOR 'characterUpdates' output:**
        // - Only include a character in 'characterUpdates' if their 'status' (e.g., if they died), 'backstory', 'occupation', or 'personality' has undergone a major, lasting change in the 'CONVERSATION HISTORY TO SUMMARIZE'.
        // - If you update a character, their 'name' in 'characterUpdates' MUST EXACTLY match their 'name' in this 'EXISTING SAGA CAST' list.
        // - Provide ONLY the fields that have changed. Do NOT include fields that remain the same.
        // - For 'backstory' or 'personality' fields, provide the *new, updated complete text* if it's changing, not just a summary of the change.        
        ${sagaContent.characters.joinToString(",\n") { it.toJsonFormat() }}

        """"
}
