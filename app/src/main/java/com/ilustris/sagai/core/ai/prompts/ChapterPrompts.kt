package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
import com.ilustris.sagai.features.characters.data.model.CharacterPose
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki

object ChapterPrompts {
    private data class ChapterEndingPromptContext(
        val sagaInfo: Saga,
        val mainCharacter: Character?,
        val characterCast: List<Character>,
        val allSagaChaptersSummary: List<Chapter>,
        val fullSagaTimelineSummary: List<Timeline>,
        val eventsOfChapterBeingEnded: List<Timeline>,
        val worldKnowledge: List<Wiki>,
    )

    fun chapterGeneration(
        sagaContent: SagaContent,
        currentChapterContent: ChapterContent,
    ): String {
        val promptDataContext =
            ChapterEndingPromptContext(
                sagaInfo = sagaContent.data,
                mainCharacter = sagaContent.mainCharacter,
                characterCast = sagaContent.characters,
                allSagaChaptersSummary = sagaContent.flatChapters().map { it.data },
                fullSagaTimelineSummary = sagaContent.flatEvents().map { it.timeline },
                eventsOfChapterBeingEnded = currentChapterContent.events.map { it.timeline },
                worldKnowledge = sagaContent.wikis,
            )

        val fieldsToExclude =
            listOf(
                "id",
                "messages",
                "icon",
                "details",
                "image",
                "hexColor",
                "sagaId",
                "joinedAt",
                "coverImage",
                "isEnded",
                "endedAt",
                "isDebug",
                "endMessage",
                "review",
                "messageReference",
                "createdAt",
                "currentActId",
                "currentChapterId",
                "currentEventId",
                "actId",
                "chapterId",
            )

        val combinedContextJson = promptDataContext
        promptDataContext.toJsonFormatExcludingFields(fieldsToExclude)

        return """
            CONTEXT:
             $combinedContextJson

             TASK:
             You are writing the concluding narrative for the chapter detailed in the CHAPTER_BEING_ENDED object within the CONTEXT.
             This narrative will form the "overview" for this chapter. Based on the SAGA_INFO and particularly the EVENTS_OF_CHAPTER_BEING_ENDED:

             1.  Craft a concise (around 100 words) **concluding narrative**. This narrative MUST:
                 a.  Summarize the key outcomes and the immediate aftermath of the EVENTS_OF_CHAPTER_BEING_ENDED.
                 b.  Reflect on the MAIN_CHARACTER's current situation, pivotal decisions made, or consequences faced as a result of these events.
                 c.  Provide a clear transition or hook that **sets the stage for the next chapter**, hinting at new challenges, mysteries, or paths forward.

             2.  Suggest a "title" for this chapter (it can be the same as the original title in CHAPTER_BEING_ENDED if one exists, or a new one if the original title is empty or needs refinement to reflect its conclusive nature).

             3.  From the CHARACTER_CAST, select up to 3 "featuredCharacters" most relevant to this chapter's conclusion and its transition to the next.
                 **Critical rule: YOU MUST SELECT AT LEAST 1 CHARACTER.**
                 **Critical rule: YOU CAN ONLY RETURN THE NAME OF A CHARACTER, NOT ANY OTHER DETAIL FROM THEIR PROFILE.**

             Use WORLD_KNOWLEDGE for consistent terminology.
             The tone should be engaging and make the player anticipate what's next.

             OUTPUT_FORMAT_EXPECTED:
              ${currentChapterContent.data.toJsonFormatExcludingFields(fieldsToExclude)}
             // "title", "overview", and "featuredCharacters" are the primary fields to generate for this chapter's concluding summary.
            """.trimIndent()
    }

    fun coverGeneration(
        data: Saga,
        description: String,
    ) = """
        ${GenrePrompts.artStyle(data.genre)}
        $description
        """.trimIndent()

    fun coverDescription(
        content: SagaContent,
        chapter: Chapter,
        characters: List<Character>,
    ): String {
        val coverContext =
            mapOf(
                "sagaTitle" to content.data.title,
                "sagaGenre" to content.data.genre.title,
                "chapterContext" to chapter,
                "charactersInvolved" to characters,
            )
        val fieldsToExcludeForCover =
            listOf(
                "sagaId",
                "joinedAt",
                "actId",
                "events",
                "currentEventId",
                "synopsis",
                "fullContent",
                "soundTrack",
                "isEnded",
                "endedAt",
                "isDebug",
                "endMessage",
                "review",
                "mainCharacterId",
                "currentActId",
                "id",
                "image",
                "hexColor",
                "backstory",
            )
        val coverContextJson = coverContext.toJsonFormatExcludingFields(fieldsToExcludeForCover)

        return """
            Your task is to act as an AI Image Prompt Engineer specializing in generating concepts for **Chapter Covers**.
            You will receive contextual information about the chapter and characters involved.
            You will also (outside this prompt) have access to a Visual Reference Image to inspire composition and specific details.

            **CRITICAL CONTEXT FOR YOU (THE AI IMAGE PROMPT ENGINEER):**
            1.  **Chapter & Character Information (JSON below):** Details about the saga, chapter, and characters.
                $coverContextJson

            **CORE STYLISTIC AND COLOR DIRECTIVES (MANDATORY):**
            1.  **Foundational Art Style:**
                *   The primary rendering style for the cover MUST be: `${GenrePrompts.artStyle(content.data.genre)}`.
            2.  **Specific Color Application Instructions:**
                *   The following rules dictate how the genre's key colors (derived from `${content.data.genre.title}`)
                are applied: `${GenrePrompts.getColorEmphasisDescription(content.data.genre)}`.
                *   **Important Clarification on Color:**
                    *   These color rules are primarily for:
                        *   The **background's dominant color theme**.
                        *   **Small, discrete, isolated accents on characters or key elements** within the chapter scene.
                    *   **CRUCIAL: DO NOT use these genre colors to tint the overall image, characters' base skin tones, hair (beyond tiny accents), or main clothing areas.** Characters' base colors should be preserved and appear natural.
                    *   Lighting on characters and scene elements should be primarily dictated by the foundational art style and aim for realism or stylistic consistency within that art style, not an overall color cast from the genre accents. Genre accents are design elements, not the primary light source for characters.

                    **YOUR TASK (Output a single text string for the Image Generation Model):**
                    Generate a single, highly detailed, unambiguous, and visually rich English text description. This description must:
                    *   Integrate the **Character Details**.
                    *   Render the scene in the **Foundational Art Style**.
                    *   Explicitly describe the **background color** and the **specific character accents** using the genre colors as per `getColorEmphasisDescription`.
                    *   Ensure the description implies that the character's base colors (skin, hair, main clothing) are preserved and not tinted by the accent colors.
                        Lighting on the character should be consistent with the art style, with genre colors applied as specific, non-overwhelming details.
                    *   Incorporate **Compositional Framing** and compatible **Visual Details & Mood** inspired by the Visual Reference Image.
                    *   **CRUCIAL: Your output text prompt MUST NOT mention the Visual Reference Image.** It must be a self-contained description.

            Adherence to the **CORE STYLISTIC AND COLOR DIRECTIVES** is critical for a successful output. Pay close attention to the mandated art style and color application rules based on the saga's genre.

            **YOUR TASK (Output a single text string for the Image Generation Model):**
            Generate a single, highly detailed, unambiguous, and visually rich English text description for an AI image generation model to create the Chapter Cover. This description must:
            1.  Synthesize information from the **Chapter & Character Information (JSON)** to depict the correct characters, setting, and mood for the chapter.
            2.  Render the scene in the **Foundational Art Style**.
            3.  Explicitly describe the **background color theme** and any **specific character/element accents** using the genre colors as per the `Specific Color Application Instructions`.
            4.  Ensure the description implies that characters' base colors (skin, hair, main clothing) are preserved and not tinted by the accent colors. Lighting should be consistent with the art style.
            5.  **Heavily rely on the Visual Reference Image (which you have access to) as the PRIMARY source of inspiration for the overall art style, compositional framing (suitable for a chapter cover), character poses, expressions, and the general mood.** Synthesize the *specific elements* (characters, setting details) from the **Chapter & Character Information (JSON)** into this artistic and compositional framework derived from the Visual Reference Image. The Visual Reference dictates the 'how it looks and feels'; the JSON dictates 'what is in it'.
            6.  **CRUCIAL: Your output text prompt MUST NOT mention the Visual Reference Image directly.** It must be a self-contained description.

            YOUR SOLE OUTPUT MUST BE THE GENERATED IMAGE PROMPT STRING. DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS.
            """.trimIndent()
    }
}
