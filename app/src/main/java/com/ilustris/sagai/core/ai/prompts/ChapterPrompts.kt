package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
import com.ilustris.sagai.features.characters.data.model.CharacterPose
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.timeline.data.model.Timeline

object ChapterPrompts {
    fun chapterOverview(chapter: Chapter?) =
        if (chapter == null) {
            emptyString()
        } else {
            """
        Current chapter overview:
        
        Title: ${chapter.title}
        Overview: ${chapter.overview}

        """
        }

    fun chapterGeneration(
        sagaContent: SagaContent,
        lastAddedEvents: List<Timeline>,
    ) = """
        Write a new chapter to continue the adventure in a role-playing game (RPG) set in the world of ${sagaContent.data.title}.
        ${SagaPrompts.details(sagaContent.data)}
        
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

    fun coverCompositions(characters: List<Character>) =
        """
        ${
            when (characters.size) {
                1 ->
                    CharacterPrompts.appearance(characters.first()) +
                        "in a ${CharacterPose.random()} pose and a ${CharacterExpression.random()} expression."
                else -> "${characters.size} Characters: ${characters.joinToString {
                    "${CharacterPrompts.appearance(
                        it,
                    )} in a ${CharacterPose.random()} pose and a ${CharacterExpression.random()} expression."
                }}"
            }
        }
        """

    fun coverGeneration(
        data: SagaData,
        description: String,
    ) = """
        ${GenrePrompts.artStyle(data.genre)}
        ${CharacterFraming.FULL_BODY.description}
        $description
        """.trimIndent()

    fun coverDescription(
        data: SagaData,
        chapter: Chapter,
        characters: List<Character>,
    ): String =
        """
        Your task is to act as an AI Image Prompt Engineer specializing in generating concepts for **Chapter Covers** for the saga "${data.title}" (Genre: ${data.genre.title}).
        You will receive a chapter's title, its detailed summary, and a list of main characters involved.
        Your goal is to convert this information into a single, highly detailed, unambiguous, and visually rich English text description.
        This description will be directly used as a part of a larger prompt for an AI image generation model.

        **Crucially, this description MUST be formulated to be compatible with a 'Chapter Cover' framing,
        conveying the essence and mood of the chapter in a single, impactful image.
        Adhere strictly to the provided 'Story Theme' (${data.genre.title}).**
        
        **Chapter Context:**
        
        1.  **Chapter Title:** ${chapter.title}
        2.  **Chapter Summary/Description: ${chapter.overview}
        3.  **Main Characters Involved:** [ ${characters.formatToJsonArray()} ]
        
        **Guidelines for Conversion and Expansion:**
        
        1.  **Translate Accurately:** Translate all Portuguese values from the input fields into precise English.
        2.  **Infer Visuals from Summary:** **This is critical.** From the `Chapter Summary/Description`, infer and elaborate on:
            * **Primary Setting/Environment:** Describe the main location(s) with vivid detail (e.g., "dense, ancient forest with gnarled trees and ethereal mist," "swampy terrain with eerie bioluminescent flora," "crumbling stone altar overgrown with vines").
            * **Dominant Mood/Atmosphere:** Translate the chapter's tone into visual cues (e.g., "ominous and mysterious," "tense and adventurous," "peaceful yet ancient").
            * **Key Actions/Moments:** Identify the most visually impactful actions or climactic moments described and suggest how they could be represented (e.g., "Mila navigating through murky water," "a standoff with the guardian," "light emanating from an ancient artifact").
            * **Important Objects/Elements:** Include any significant items, creatures, or symbols mentioned that would enhance the cover's narrative (e.g., "glowing elven artifact," "shadowy figures among the trees," "ancient runes on the altar").
        3.  **Integrate Main Characters:** If characters are listed, integrate them visually into the scene, ensuring their appearance (if previously established) and their role/action in the chapter are subtly or prominently displayed as appropriate for a cover.
        4.  **Composition for Cover/Banner:** Formulate the prompt to suggest a **dynamic, wide-angle or cinematic composition** suitable for a book cover or poster. Think of elements that draw the eye, perhaps a central figure or object framed by the environment. The image should encapsulate the chapter's essence at a glance.
            * **Suggested terms to use:** "wide shot," "cinematic perspective," "epic scale," "dynamic composition," "foreground, midground, background elements," "strong visual narrative," "suitable for title overlay at the top/bottom."
        5.  **Thematic Consistency (${data.genre.title}):** Ensure all generated visual descriptions align with the ${data.genre} genre of "${data.title}".
        6.  **Art Style Consistency:** Maintain the specified artistic style: ${GenrePrompts.artStyle(data.genre)}
        7.  **Exclusions:** NO TEXT, NO WORDS, NO TYPOGRAPHY, NO LETTERS, NO UI ELEMENTS.
        
        **Example of Expected English Text Output (single paragraph, ready for image model):**
        
        "A wide shot, classical oil painting of a lone figure, Mila, navigating a treacherous, mist-shrouded ancient forest. Gnarled, moss-covered trees loom overhead, their branches forming eerie silhouettes against a moonlit sky. A faint, unsettling bioluminescent glow emanates from hidden flora within a murky swamp in the foreground. The air is thick with a sense of ancient mystery and subtle danger. Mila, wearing practical mercenary gear, moves cautiously, her silhouette hinting at determination and resilience. In the midground, faint, glowing runes are visible on crumbling stone ruins partially consumed by vines, hinting at an elven altar. Strong chiaroscuro lighting emphasizes the depths of the forest, with pockets of natural light cutting through the canopy, creating a tense, adventurous atmosphere. Rich impasto texture and visible brushstrokes. Harmonious colors, authentic painterly grain. No borders, no text, no words, no typography, no letters, no UI elements."
        
                """
}
