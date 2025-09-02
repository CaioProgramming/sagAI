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
    fun chapterIntroductionPrompt(
        sagaContent: SagaContent,
        currentChapter: ChapterContent,
    ): String = buildString {
        val actContent = sagaContent.findChapterAct(currentChapter.data)
        val chaptersInAct = actContent?.chapters?.map { it.data } ?: emptyList()
        val index = chaptersInAct.indexOfFirst { it.id == currentChapter.data.id }

        appendLine("CONTEXT:")
        appendLine("You are an AI assistant helping to write a saga chapter.")
        appendLine("Saga Title: \"${sagaContent.data.title}\"")
        appendLine("Saga Genre: ${sagaContent.data.genre.title}")
        appendLine("Act Title: \"${actContent?.data?.title ?: ""}\"")

        if (index == 0) {
            // First chapter in act: use act description as base
            val actDescription = actContent?.data?.content?.take(250) ?: ""
            appendLine("Act Description: \"$actDescription\"")
            appendLine("\nTASK:")
            appendLine("Generate a single, compelling introductory paragraph (around 40-60 words) for the FIRST CHAPTER of this act.")
            appendLine("Base the introduction on the act's description to set the immediate context and hook the reader to continue.")
        } else {
            val previous = chaptersInAct.getOrNull(index - 1)
            val prevTitle = previous?.title ?: ""
            val prevOverview = previous?.overview?.take(250) ?: ""
            appendLine("Previous Chapter Title: \"$prevTitle\"")
            appendLine("Previous Chapter Overview: \"$prevOverview\"")
            appendLine("\nTASK:")
            appendLine("Generate a single, compelling introductory paragraph (around 40-60 words) for the NEXT CHAPTER of this act.")
            appendLine("Use the previous chapter's title and overview to smoothly transition, contextualize, and engage the reader to continue the story.")
        }
        appendLine("Output only the introduction paragraph, no titles, quotes, or extra text.")
    }
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
                eventsOfThisChapter = currentChapterContent.events.filter { it.isComplete() }.map { it.data },
                previousChaptersInCurrentAct = currentChapters.map { it.data },
                previousActData = previousAct?.data,
            )

        val includedFields =
            listOf(
                "sagaData",
                "mainCharacter",
                "previousActData",
                "previousChaptersInCurrentAct",
                "eventsOfThisChapter",
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
                        "introduction",
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
            "1. A concise overview (around 100 words) that summarizes the key outcomes, significant developments, and the immediate aftermath of these events. This overview should also provide a natural hook or transition setting the stage for what might come next.",
        )
        appendLine(
            "2. Generate a fitting title for this chapter that accurately reflects its core content or theme as derived from the events. **The title should be short (ideally 2-5 words) and impactful, creating intrigue or summarizing the chapter's essence memorably.**",
        )
        appendLine("Consider the `SAGA_DATA` for overall tone and style, and the `MAIN_CHARACTER`'s perspective if relevant to the events.")
        appendLine("EXPECTED OUTPUT FORMAT:")
        appendLine(chapterOutput)
    }

    fun coverDescription(
        content: SagaContent,
        chapter: Chapter, // Chapter context is kept for potential genre/saga reference, but not for detailed scene elements
        characters: List<Character>, // List of 2 or 3 characters to feature
    ): String {
        val coverContext =
            mapOf(
                "sagaTitle" to content.data.title,
                "sagaGenre" to content.data.genre.title,
                // "chapterContext" is still here, but the prompt will de-emphasize its use for scene details.
                // It primarily serves to link to the saga's overall theme/genre.
                "chapterTitle" to chapter.title, // Keep title for minimal context if needed, but AI will be told to ignore for scene
                "charactersInvolved" to characters,
            )
        // Keep exclusions tight, especially for chapter-specific details that might suggest complex scenes.
        val fieldsToExcludeForCover =
            listOf(
                "sagaId",
                "joinedAt",
                "actId",
                "events", // Exclude chapter events from context
                "currentEventId",
                "synopsis", // Exclude detailed synopsis
                "fullContent", // Exclude full content
                "soundTrack",
                "isEnded",
                "endedAt",
                "isDebug",
                "endMessage",
                "review",
                "mainCharacterId",
                "currentActId",
                "id",
                "image", // Character image is handled by visual reference
                "hexColor",
                "backstory", // Character backstory is too detailed for cover prompt
                "featuredCharacters", // This is passed via the `characters` parameter directly
                "createdAt",
                "coverImage",
                "personality", // Character personality is too detailed for cover prompt
                "overview", // Chapter overview excluded
                "emotionalReview", // Chapter emotional review excluded
                // Fields from Chapter data class that might imply setting or plot:
                "content",
                "order",
            )
        val coverContextJson = coverContext.toJsonFormatExcludingFields(fieldsToExcludeForCover)

        return """
            Your task is to act as an AI Image Prompt Engineer specializing in generating concepts for **Minimalistic Chapter Covers**.
            You will receive contextual information about the SAGA (title, genre) and the specific CHARACTERS to be featured.
            You will also (outside this prompt) have access to Visual Reference Images for each character involved to inspire their appearance, and a general Visual Reference Image for overall composition and style.

            **CRITICAL CONTEXT FOR YOU (THE AI IMAGE PROMPT ENGINEER):**
            1.  **Saga & Character Information (JSON below):** Details about the saga's genre and the characters to feature.
                $coverContextJson

            **CORE STYLISTIC AND COLOR DIRECTIVES (MANDATORY):**
            1.  **Foundational Art Style:**
                *   The primary rendering style for the cover MUST be: `${GenrePrompts.artStyle(content.data.genre)}`.
            2.  **Specific Color Application Instructions:**
                *   The following rules dictate how the genre's key colors (derived from `${content.data.genre.title}`)
                are applied: `${GenrePrompts.getColorEmphasisDescription(content.data.genre)}`.
                *   **Important Clarification on Color:**
                    *   These color rules are primarily for:
                        *   A **MINIMALISTIC background** with a dominant color theme derived from the genre.
                        *   **Small, discrete, isolated accents ON THE CHARACTERS**.
                    *   **CRUCIAL: DO NOT use these genre colors to tint the overall image, characters' base skin tones, hair (beyond tiny accents), or main clothing areas.** Characters' base colors should be preserved and appear natural.
                    *   Lighting on characters should be primarily dictated by the foundational art style, not an overall color cast from the genre accents.

            **YOUR TASK (Output a single text string for the Image Generation Model):**
            Generate a single, highly detailed, unambiguous, and visually rich English text description for an AI image generation model. This description MUST create a **MINIMALISTIC CHAPTER COVER FOCUSED ON THE CHARACTERS**.

            The description must:
            1.  **Prioritize Character Depiction:**
                *   Focus EXCLUSIVELY on the character(s) listed in `charactersInvolved` from the JSON context.
                *   Ensure their appearance, pose, and expression are primarily inspired by their individual Visual Reference Images and the overall compositional Visual Reference Image.
                *   If multiple characters are present, their interaction or composition should be clear and engaging, suitable for a cover.
            2.  **Minimalistic Background:**
                *   Describe a SIMPLE and MINIMALISTIC background.
                *   The background should primarily feature the dominant color theme derived from the `sagaGenre` as per the `Specific Color Application Instructions`. Avoid complex scenes or detailed environmental elements from the chapter's specific content. The focus is on the characters against a stylized, genre-appropriate backdrop.
            3.  **Adherence to Directives:**
                *   Render the scene in the **Foundational Art Style**.
                *   Explicitly describe the **background color theme** and any **specific character accents** using the genre colors as per the `Specific Color Application Instructions`.
                *   Ensure the description implies that characters' base colors (skin, hair, main clothing) are preserved.
            4.  **Visual Reference Synthesis:**
                *   **Heavily rely on the general Visual Reference Image for overall art style, compositional framing (suitable for a minimalistic character-focused cover), character poses, and mood.**
                *   The specific CHARACTERS are dictated by the `charactersInvolved` in the JSON. Their individual appearances are inspired by their respective Visual Reference Images.
                *   Synthesize these character appearances into the artistic and compositional framework derived from the general Visual Reference Image.
            5.  **Self-Contained Prompt:**
                *   **CRUCIAL: Your output text prompt MUST NOT mention any Visual Reference Image directly.** It must be a self-contained description.
            6. **No Text or borders: **
                * Focus entirely on the art description, ensure that no text from the context is described in the final result.
            YOUR SOLE OUTPUT MUST BE THE GENERATED IMAGE PROMPT STRING. DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS.
            """.trimIndent()
    }
}
