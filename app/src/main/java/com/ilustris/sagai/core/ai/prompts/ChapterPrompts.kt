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
import com.ilustris.sagai.features.home.data.model.flatEvents
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
            if (sagaContent.acts.size > 1 && currentAct == sagaContent.acts.firstOrNull()) {
                null
            } else {
                val currentActIndex =
                    sagaContent.acts.indexOfFirst { it.data.id == currentAct.data.id }
                if (currentActIndex > 0) sagaContent.acts[currentActIndex - 1] else null
            }

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

            val allChapters = sagaContent.flatChapters()
            val currentChapterIndex = allChapters.indexOfFirst { it.data.id == currentChapter.id }
            val previousChapter =
                if (currentChapterIndex > 0) allChapters[currentChapterIndex - 1] else null

            val allEvents = sagaContent.flatEvents()
            val latestEvent = allEvents.lastOrNull { it.isComplete() }

            // Provide context based on what's actually established
            when {
                isFirstChapter -> {
                    appendLine("### CONTEXT: This is the VERY FIRST CHAPTER of the entire saga.")
                    appendLine("## Act Introduction:")
                    appendLine(currentAct.data.introduction)
                    appendLine(
                        "- **Ambiguity & Hook:** Do NOT force a direction, specific objective, or immediate situation. Keep the opening ambiguous and focus on a compelling hook that invites the PLAYER to decide how the story starts and what their goals are.",
                    )
                    appendLine(
                        "- **Tone & Atmosphere:** Use the saga's premise and the act introduction to establish the vibe, but leave the 'what' and 'why' open for discovery.",
                    )
                    appendLine(
                        "- **Starting Point:** Describe a moment in time or a setting that feels alive but hasn't yet been defined by a specific mission or conflict.",
                    )
                }

                previousChapter != null || latestEvent != null -> {
                    appendLine("### Story Progression Context:")
                    previousChapter?.let {
                        appendLine("#### Previous Chapter: ${it.data.title}")
                        appendLine("Overview: ${it.data.overview}")
                        if (it.data.actId != currentAct.data.id) {
                            appendLine("(Note: This chapter concluded the previous act)")
                        }
                    }

                    latestEvent?.let { event ->
                        appendLine("#### DEFINITIVE LATEST MOMENT (Grounding):")
                        appendLine("Event Summary: ${event.data.content}")
                        if (event.messages.isNotEmpty()) {
                            appendLine("Immediate Context (Last Messages):")
                            appendLine(
                                event.messages
                                    .reversed()
                                    .map { it.message }
                                    .normalizetoAIItems(ChatPrompts.messageExclusions),
                            )
                        }
                        appendLine(
                            "- **MANDATORY CONTINUITY:** Your introduction MUST be a direct, realistic, and coherent continuation of this specific moment.",
                        )
                    }

                    appendLine("### Current Act Theme:")
                    appendLine(currentAct.data.introduction)
                    appendLine(
                        "- Bridge the gap seamlessly. Use the 'Previous Chapter' to understand the overall journey and the 'Definitive Latest Moment' to ground the immediate transition.",
                    )
                }

                else -> {
                    appendLine("### Current Act Theme:")
                    appendLine(currentAct.data.introduction)
                    appendLine(
                        "- Create an introduction that fits the current act's theme and progression, using the saga's premise as a guide.",
                    )
                }
            }

            appendLine("### Narrative Directive (Pacing and Style):")
            appendLine(sagaContent.getDirective())

            appendLine("## YOUR TASK")
            appendLine("Write a single paragraph introduction that:")
            appendLine(
                "1. **Reflects Current State:** Based ONLY on the provided context, especially the 'Definitive Latest Moment'.",
            )
            if (isFirstChapter) {
                appendLine(
                    "2. **Ambiguous Opening:** Create an atmospheric opening that hooks the player without defining their path or objective. Let them lead.",
                )
            } else {
                appendLine(
                    "2. **Seamless Transition:** Pick up exactly where the latest event left off. No time skips unless explicitly suggested by the context.",
                )
            }
            appendLine(
                "3. **Engages Without Fabrication:** Create anticipation through atmosphere and established stakes, not new invented plot points.",
            )

            appendLine("## CRITICAL RULES")
            appendLine("- NEVER invent events that haven't been established in the provided context.")
            if (isFirstChapter) {
                appendLine(
                    "- **NO FORCED OBJECTIVES:** Do not tell the player what they must do or why they are there. Describe the world and wait for their lead.",
                )
            }
            appendLine(
                "- **STRICT CONTINUITY:** Your opening sentence must acknowledge or stem from the very last messages/actions in the 'Definitive Latest Moment'.",
            )

            appendLine("## OUTPUT REQUIREMENTS")
            appendLine("- **Length:** 1 concise paragraph (40-60 words).")
            appendLine("- **Content:** NO dialogue. Focus on atmosphere, setting, and the immediate transition.")
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
