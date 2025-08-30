package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonFormatIncludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki

object ChapterPrompts {
    private data class ChapterConclusionContext(
        val sagaData: Saga,
        val mainCharacter: Character?,
        val eventsOfThisChapter: List<Timeline>,
        val previousActData: Act?,
        val previousChaptersInCurrentAct: List<Chapter>,
    )

    @Suppress("ktlint:standard:max-line-length")
    fun chapterGeneration(
        sagaContent: SagaContent,
        currentChapterContent: ChapterContent,
    ) = buildString {
        val chapterAct = sagaContent.findChapterAct(currentChapterContent.data)
        val isFirstAct =
            sagaContent.acts
                .first()
                .data.id == chapterAct?.data?.id
        val currentChapters = chapterAct?.chapters?.filter { it.data.id != currentChapterContent.data.id } ?: emptyList()

        val previousAct =
            if (isFirstAct) {
                null
            } else {
                val previousActIndex = sagaContent.acts.indexOfFirst { it.data.id == chapterAct?.data?.id } - 1
                sagaContent.acts[previousActIndex]
            }

        val promptDataContext =
            ChapterConclusionContext(
                sagaData = sagaContent.data,
                mainCharacter = sagaContent.mainCharacter?.data,
                eventsOfThisChapter = currentChapterContent.events.map { it.data },
                previousChaptersInCurrentAct = currentChapters.map { it.data },
                previousActData = previousAct?.data,
            )

        val includedFields =
            listOf(
                "title",
                "description",
                "content",
                "genre",
                "name",
                "backstory",
            )

        val combinedContextJson = promptDataContext.toJsonFormatIncludingFields(includedFields)

        val chapterOutput =
            toJsonMap(
                Chapter::class.java,
                filteredFields =
                    listOf(
                        "coverImage",
                        "currentEventId",
                        "createdAt",
                        "featuredCharacters",
                        "emotionalReview",
                        "id",
                        "actId",
                    ),
            )

        appendLine("Context:")
        appendLine(combinedContextJson)

        appendLine("TASK:")
        appendLine("You are an AI assistant tasked with concluding a chapter of a saga.")
        appendLine(
            "Based ENTIRELY on the `EVENTS_OF_THIS_CHAPTER` provided in the CONTEXT, you need to generate two pieces of information for the `CHAPTER_BEING_CONCLUDED`:",
        )
        appendLine(
            " - If `PREVIOUS_CHAPTERS_IN_CURRENT_ACT` is provided and not empty, use their `title`s and `overview`s to understand the immediate preceding narrative progression within this act. Your generated `overview` for the current chapter should flow naturally from these, and its hook should set the stage for what might come next, considering this continuity.",
        )
        appendLine(
            " - If `PREVIOUS_ACT_DATA` is provided, use its `title` and `description` (or `overview`) to understand the broader story arc of the saga leading up to the current act. Ensure the current chapter's conclusion aligns with this larger progression.",
        )

        appendLine(
            "1. A concise \"overview\" (around 100 words) that summarizes the key outcomes, significant developments, and the immediate aftermath of these events. This overview should also provide a natural hook or transition setting the stage for what might come next.",
        )
        appendLine(
            "2. Generate a fitting \"title\" for this chapter that accurately reflects its core content or theme as derived from the events. **The title should be short (ideally 2-5 words) and impactful, creating intrigue or summarizing the chapter's essence memorably.**",
        )
        appendLine("Consider the `SAGA_DATA` for overall tone and style, and the `MAIN_CHARACTER`'s perspective if relevant to the events.")
        appendLine("EXPECTED OUTPUT FORMAT:")
        appendLine(chapterOutput)
    }

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
                "featuredCharacters",
                "createdAt",
                "coverImage",
                "personality",
                "overview",
                "emotionalReview",
            )
        val coverContextJson = coverContext.toJsonFormatExcludingFields(fieldsToExcludeForCover)

        return """
            Your task is to act as an AI Image Prompt Engineer specializing in generating concepts for **Chapter Covers**.
            You will receive contextual information about the chapter and characters involved.
            You will also (outside this prompt) have access to a Visual Reference Image to inspire composition and specific details.
            This references will include the characters actual images following the respective order on the field 'charactersInvolved'.
            

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
