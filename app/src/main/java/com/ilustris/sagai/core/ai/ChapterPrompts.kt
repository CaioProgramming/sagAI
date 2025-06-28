package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.SagaPrompts.details
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
import com.ilustris.sagai.features.characters.data.model.CharacterPose
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.timeline.data.model.Timeline

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
        
        Current saga cast:
        [ ${sagaContent.characters.formatToJsonArray()} ]
        
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
        
        
        **Your chapter description should be concise, compelling, and around 100 words.
        ** It must capture the essence of a playable RPG experience and prepare the player for the next phase of the story.
        
        The chapter description MUST effectively summarize the latest developments by including:
        1.  **Key Events & Current Situation:** A brief summary of the main events that have just happened, clearly indicating the current state of the world and the main character's situation. **Ensure this incorporates relevant world entities (locations, organizations, items, concepts, technologies, or events) from the 'EXISTING WORLD WIKI ENTRIES', especially if they are new or have become significantly important in this segment.**
        2.  **Main Character's Role:** A recap of the main character's (${sagaContent.mainCharacter?.name}) actions and pivotal decisions within this chapter's events.
        
        For the featuredCharacters field, select 3 characters with most relevance to the chapter.
        **Critical rule: YOU MUST SELECT AT LEAST 1 CHARACTER FROM THE LIST OF CHARACTERS.**
        **Critical rule: YOU CAN ONLY RETURN THE NAME OF CHARACTER, NOT ANY NAME OR OTHER DETAIL.**

        """.trimIndent()

    fun chapterCover(
        saga: SagaData,
        characters: List<Character>,
    ) = """
        ${GenrePrompts.artStyle(saga.genre)}
        Featuring:
        ${coverCompositions(characters)}
        ${GenrePrompts.coverComposition(saga.genre)}
        """

    fun coverCompositions(characters: List<Character>) =
        """
        ${
            when (characters.size) {
                1 ->
                    "Full-body shot of ${CharacterPrompts.appearance(characters.first())}" +
                        "in a ${CharacterPose.random()} pose and a ${CharacterExpression.random()} expression."
                else -> "${characters.size} Characters: ${characters.joinToString {
                    "Full-body shot of ${CharacterPrompts.appearance(
                        it,
                    )} in a ${CharacterPose.random()} pose and a ${CharacterExpression.random()} expression."
                }}"
            }
        }
        """
}
