package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.models.ChapterConclusionContext
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonFormatIncludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterGeneration
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.getDirective

object ChapterPrompts {
    fun chapterSummary(sagaContent: SagaContent) =
        if (sagaContent.currentActInfo?.chapters?.isEmpty() == true) {
            "No chapters written yet on this act,"
        } else {
            """
        **CURRENT ACT CHAPTERS (Most Recent First):**
        // This section provides the summaries of chapters already written in the current act.
        // Use this to understand the immediate narrative progression and context within the act.
        ${sagaContent.currentActInfo?.chapters?.filter { it.isComplete() }?.map { it.data }?.reversed()?.formatToJsonArray(
                listOf(
                    "id",
                    "emotionalReview",
                    "actId",
                    "currentEventId",
                    "coverImage",
                    "createdAt",
                    "featuredCharacters",
                ),
            )}
            
        """
        }

    fun chapterIntroductionPrompt(
        sagaContent: SagaContent,
        currentChapter: Chapter,
        currentAct: ActContent,
    ): String =
        buildString {
            val actContent = sagaContent.findChapterAct(currentChapter)
            val chaptersInAct = currentAct.chapters.filter { it.isComplete() }.map { it.data }
            val isFirst = chaptersInAct.isEmpty()
            val excludedFields =
                listOf(
                    "details",
                    "image",
                    "hexColor",
                    "sagaId",
                    "joinedAt",
                    "id",
                    "firstSceneId",
                    "createdAt",
                )
            val sagaExclusion =
                listOf(
                    "id",
                    "icon",
                    "createdAt",
                    "mainCharacterId",
                    "isDebug",
                    "endMessage",
                    "currentActId",
                    "endedAt",
                    "review",
                    "emotionalReview",
                    "isEnded",
                )

            val chapterExclusions =
                listOf(
                    "id",
                    "emotionalReview",
                    "actId",
                    "currentEventId",
                    "coverImage",
                    "createdAt",
                    "featuredCharacters",
                )

            appendLine("Your task is write a introduction to engage the player to continue the story.")
            appendLine("Use the context provided to create a relevant and compelling introduction paragraph.")
            appendLine("Keep the introduction around 40-60 words.")
            appendLine("Do not reference the chapter title or any characters by name.")
            appendLine("Do not include any quotes or dialogue.")
            appendLine("Do not include any text other than the introduction paragraph.")
            appendLine("Saga context:")
            appendLine(sagaContent.data.toJsonFormatExcludingFields(sagaExclusion))
            appendLine("Main character Context:")
            appendLine(sagaContent.mainCharacter?.data.toJsonFormatExcludingFields(excludedFields))

            val description =
                actContent?.data?.introduction?.ifEmpty {
                    sagaContent.data.description
                }

            appendLine("Use this description to understand current context of the saga and act.")
            appendLine(description)

            if (chaptersInAct.isNotEmpty()) {
                appendLine("Use the following chapters in this act to understand the immediate context:")
                appendLine(chaptersInAct.filter { it.id != currentChapter.id }.formatToJsonArray(chapterExclusions))
            }

            appendLine("Use this directive to understand the improve the introduction:")
            appendLine(sagaContent.getDirective())

            appendLine("Output only the introduction paragraph, no titles, quotes, or extra text.")
        }

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
                eventsOfThisChapter =
                    currentChapterContent.events
                        .filter { it.isComplete() }
                        .map { it.data },
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
                ChapterGeneration::class.java,
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
        appendLine(
            "3. Extract 1 - 3 most important characters to include in featuredCharacters array.",
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
                "sagaGenre" to content.data.genre.name,
                "chapterTitle" to chapter.title,
                "chapterDescription" to chapter.overview,
                "charactersInvolved" to characters,
            )
        val fieldsToExcludeForCover =
            listOf(
                "joinedAt",
                "id",
                "image",
                "hexColor",
                "sagaId",
                "abilities",
                "emojified",
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
                 *   The following rules dictate the color palette and light composition for the image generation, are applied:
                 `${GenrePrompts.getColorEmphasisDescription(content.data.genre)}`.
                 *   **Important Clarification on Color:**
                     *   **CRUCIAL: DO NOT use these genre colors to tint the overall image, characters' base skin tones, hair (beyond tiny accents), or main clothing areas.** Characters' base colors should be preserved and appear natural.
                     *   Lighting on characters should be primarily dictated by the foundational art style, not an overall color cast from the genre accents.
             3. **Cinematography & Composition Directives (Genre-Specific):**   
             * The following rules dictate the composition and cinematography for the image generation, are applied:
             `${GenrePrompts.cinematographyComposition(content.data.genre)}`.
             **YOUR TASK (Output a single text string for the Image Generation Model):**
             Generate a single, highly detailed, unambiguous, and visually rich English text description for an AI image generation model. This description MUST create a **MINIMALISTIC CHAPTER COVER FOCUSED ON THE CHARACTERS**.

            The description must:
            1. Prioritize Character Depiction:
             * High Focus on the character(s) listed in charactersInvolved from the JSON context. Incorporate them on the mood and environment.
             * Ensure their appearance and expression are primarily inspired by their individual Visual Reference Images.
             * Their POSE must be INTENSELY DYNAMIC and reflect the chapter's core theme. The final **cinematic framing** must prioritize **emotional impact and narrative significance**. The composition can utilize a **dynamic non-standard camera perspective** (e.g., extreme close-up, low-angle, dutch angle, or eye-level shot with creative staging) to maximize drama. 
             *   If the chapter involves **direct, intense confrontation**, include a strong **foreground element** (like a weapon hilt, a character's hand, or a close-up section of a face) to convey immediate danger and scale. However, if the chapter emphasizes **alliance, mystery, dialogue, or emotional reflection**, the composition **MUST AVOID foreground obstruction, especially weapons.** Instead, prioritize **expressive interaction** and **dynamic character staging** (e.g., side-by-side, one observing another, shared gaze, or focused on a shared object of narrative importance) that highlights their connection, emotions, or the chapter's central theme. The dramatic scale in these cases should come from **character expressions, dramatic lighting, and environmental mood, NOT from a foreground object.**             * Ensure facial expressions are described with vivid, high-intensity emotional language (e.g., "vengeful stare", "strained defiance", "visible terror") to capture the essence of the chapter's conflict.
             * If multiple characters are present, their interaction or composition should be clear and engaging, suitable for a cover.
             * Ensure to place characters in dramatic and dynamic poses providing more emotion on the generated image.
               Narrative Fidelity (MANDATORY MINIMALISM CHECK): The agent MUST cross-reference the chapterDescription to identify the **2 most critical characters** driving the central conflict, alliance, or emotional beat of the chapter. To maintain a **MINIMALISTIC** and high-impact cover, the final image **MUST FOCUS ONLY ON THESE 2 CHARACTERS** in the main composition. All other characters from the JSON, even if briefly mentioned, must be **EXCLUDED** from the image prompt to ensure maximum dramatic focus on the central duo.             * Ensure their appearance and expression are primarily inspired by their individual Visual Reference Images. Their POSE, however, should be INTENSELY DYNAMIC AND ACTION-ORIENTED, derived from their context within the chapter (Implants and Revelation) or their inherent traits. The action description must maximize the perceived urgency and confrontation. The overall compositional Visual Reference Image can inspire the framing, but the final composition MUST prioritize the cinematic angle dictated by the Dynamic Pose Rule
            2.  **Adherence to Directives:**
                 *   Render the scene in the **Foundational Art Style**.
                 *   Ensure the description implies that characters characteristics are preserved and follow the visual reference provided.
             3.  **Visual Reference Synthesis:**
                 *   **Heavily rely on the general Visual Reference Image for overall art style, compositional framing (suitable for a minimalistic character-focused cover), character pose *inspiration* (not direct replication), and mood.**
                 *   The specific CHARACTERS are dictated by the `charactersInvolved` in the JSON. Their individual appearances are inspired by their respective Visual Reference Images.
                 *   Synthesize these character appearances into the artistic and compositional framework derived from the general Visual Reference Image, **ensuring the poses are dynamic and narratively suggestive for the chapter cover.** Describe these poses vividly.
                 *   The prompt must NOT mention any Visual Reference Image directly. It must be a self-contained description.
             4.  **No Text or borders: **
                 *   Focus entirely on the art description, ensure that no text from the context is described in the final result.
            YOUR SOLE OUTPUT MUST BE THE GENERATED IMAGE PROMPT STRING. DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS.
            """.trimIndent()
    }
}
