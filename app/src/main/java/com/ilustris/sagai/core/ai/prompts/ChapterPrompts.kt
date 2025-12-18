package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ChapterConclusionContext
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toJsonFormatIncludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterGeneration
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.getDirective

object ChapterPrompts {
    val CHAPTER_EXCLUSIONS =
        listOf("id", "currentEventId", "coverImage", "createdAt", "actId", "featuredCharacters")

    fun chapterSummary(sagaContent: SagaContent) =
        buildString {
            sagaContent.currentActInfo
                ?.chapters
                ?.filter { it.isComplete() }
                ?.map { it.data }
                ?.let { chapters ->
                    if (chapters.isNotEmpty()) {
                        appendLine("**CURRENT ACT CHAPTERS Overview:**")
                        appendLine("This section provides the summaries of chapters already written in the current act")
                        appendLine("// Use this to understand the immediate narrative progression and context within the act.")
                        appendLine(
                            chapters.normalizetoAIItems(
                                listOf(
                                    "id",
                                    "actId",
                                    "currentEventId",
                                    "coverImage",
                                    "createdAt",
                                    "featuredCharacters",
                                ),
                            ),
                        )
                    }
                }
        }

    fun chapterIntroductionPrompt(
        sagaContent: SagaContent,
        currentChapter: Chapter,
        currentAct: ActContent,
    ): String =
        buildString {
            sagaContent.findChapterAct(currentChapter)
            val previousChaptersInAct =
                currentAct.chapters
                    .filter { it.isComplete() }
                    .map { it.data }
                    .filter { it.id != currentChapter.id }

            // Check if this is the very first chapter of the saga
            val isFirstChapter =
                sagaContent
                    .flatChapters()
                    .first()
                    .data.id == currentChapter.id

            // Get previous act context if this is not the first act
            val previousAct =
                if (sagaContent.acts.size > 1 && currentAct == sagaContent.acts.firstOrNull()) {
                    null
                } else {
                    val currentActIndex =
                        sagaContent.acts.indexOfFirst { it.data.id == currentAct.data.id }
                    if (currentActIndex > 0) sagaContent.acts[currentActIndex - 1] else null
                }

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

            appendLine(
                "You are an AI storyteller writing an introduction for the next chapter of an ongoing saga.",
            )
            appendLine(
                "Your task is to create a natural transition based ONLY on established story context, not invented events.",
            )
            appendLine()

            appendLine(SagaPrompts.mainContext(sagaContent))

            // Provide context based on what's actually established
            when {
                isFirstChapter -> {
                    appendLine("### CONTEXT: This is the FIRST CHAPTER of the saga.")
                    appendLine("- Base your introduction on the saga's premise and the main character's starting situation.")
                    appendLine("- DO NOT reference events that haven't happened yet.")
                    appendLine("- Focus on setting the tone and establishing the beginning of the journey.")
                }

                previousChaptersInAct.isEmpty() && previousAct != null -> {
                    appendLine("### CONTEXT: This is the FIRST CHAPTER of a new act.")
                    appendLine("### Previous Act Context:")
                    appendLine("Title: ${previousAct.data.title}")
                    appendLine("Description: ${previousAct.data.content}")
                    appendLine("### Current Act Theme:")
                    appendLine(currentAct.data.introduction)
                    appendLine("- Create a bridge from the previous act to this new phase of the story.")
                }

                previousChaptersInAct.isNotEmpty() -> {
                    appendLine("### Previous Chapters in Current Act:")
                    appendLine(previousChaptersInAct.normalizetoAIItems(chapterExclusions))
                    appendLine("### Current Act Theme:")
                    appendLine(currentAct.data.introduction)
                    appendLine("- Continue naturally from where the previous chapters left off.")
                }

                else -> {
                    appendLine("### Current Act Theme:")
                    appendLine(currentAct.data.introduction)
                    appendLine("- Create an introduction that fits the current act's theme and progression.")
                }
            }

            appendLine("### Narrative Directive (Pacing and Style):")
            appendLine(sagaContent.getDirective())

            appendLine("## YOUR TASK")
            appendLine("Write a single paragraph introduction that:")
            appendLine(
                "1. **Reflects Current State:** Based ONLY on established context (previous chapters, act themes, saga premise).",
            )
            appendLine(
                "2. **Creates Natural Continuation:** Show where the story stands now without inventing new events.",
            )
            appendLine(
                "3. **Engages Without Fabrication:** Create anticipation for what's to come based on the natural story flow.",
            )
            appendLine(
                "4. **Maintains Consistency:** Never contradict or advance beyond what's actually been established.",
            )

            appendLine("## CRITICAL RULES")
            appendLine("- NEVER invent events that haven't been established in the provided context")
            appendLine("- For first chapters: Focus on the premise and starting situation, not imaginary prior events")
            appendLine("- For continuation chapters: Build only on what previous chapters actually established")
            appendLine("- Create hooks through atmosphere and anticipation, not fabricated plot points")

            appendLine("## OUTPUT REQUIREMENTS")
            appendLine("- **Length:** 1 concise paragraph (40-60 words).")
            appendLine("- **Content:** NO dialogue, character names, or specific plot details not in context.")
            appendLine("- **Format:** Output ONLY the introduction text itself. No quotes, no labels, no extra commentary.")
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
}
