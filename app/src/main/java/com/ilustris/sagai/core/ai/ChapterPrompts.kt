package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.SagaPrompts.details
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki

object ChapterPrompts {
    fun chapterOverview(chapter: Chapter) =
        """
        Current chapter overview:
        
        Title: ${chapter.title}
        Overview: ${chapter.overview}

        """

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
        // Instructions for wikiUpdates:
        // - Extract information that is newly revealed, significantly detailed, or changed within the narrative of the chapter you are generating.
        // - Focus on key nouns: names of characters, places, organizations, unique items, specific technologies, and major plot developments.
        // - Each description should be brief and factual, summarizing only what is learned in this chapter.
        // - If the chapter does not introduce any new information relevant for a wiki update, the wikiUpdates array should be empty [].
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
}
