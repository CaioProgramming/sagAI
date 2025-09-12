package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.models.ActConclusionContext
import com.ilustris.sagai.core.utils.toJsonFormatIncludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent

object ActPrompts {

    @Suppress("ktlint:standard:max-line-length")
    fun generateActConclusion(
        sagaContent: SagaContent,
        currentActContent: ActContent,
        purpose: String,
    ): String {
        val isFirstAct =
            sagaContent.acts
                .first()
                .data.id == currentActContent.data.id

        val previousAct =
            if (isFirstAct) {
                null
            } else {
                sagaContent.acts[
                    sagaContent.acts.indexOfFirst {
                        it.data.id == currentActContent.data.id
                    } - 1,
                ]
            }

        val chapterSummariesInCurrentAct = currentActContent.chapters.map { it.data }

        val promptDataContext =
            ActConclusionContext(
                sagaData = sagaContent.data,
                mainCharacter = sagaContent.mainCharacter?.data,
                previousActData = previousAct?.data,
                chaptersInCurrentAct = chapterSummariesInCurrentAct,
                actPurpose = purpose,
            )

        val includedFields =
            listOf(
                "genre",
                "name",
                "backstory",
                "title",
                "description",
                "content",
                "overview",
                "sagaData",
                "mainCharacter",
                "previousActData",
                "chaptersInCurrentAct",
                "actPurpose",
            )

        val combinedContextJson = promptDataContext.toJsonFormatIncludingFields(includedFields)
        val actOutput =
            toJsonMap(
                Act::class.java,
                filteredFields = listOf("id", "sagaId", "currentChapterId", "emotionalReview", "introduction"),
            )

        return buildString {
            appendLine("CONTEXT:")
            appendLine(combinedContextJson)
            appendLine("TASK:")
            appendLine("You are an AI assistant tasked with writing a compelling summary for a completed Act in a saga.")
            appendLine("This summary consists of a 'title' for the Act and a 'content' (description) of the Act.")
            appendLine(
                "The `ACT_PURPOSE` provided in the CONTEXT is your primary guide for the tone, direction, and narrative goals of this Act's summary.",
            )
            appendLine(
                "Based primarily on the summaries of its constituent chapters (provided in `CHAPTERS_IN_CURRENT_ACT`), and considering the `PREVIOUS_ACT_DATA` for continuity, generate these two pieces of information.",
            )
            appendLine(
                "1.  Generate a fitting 'title' for this Act. The title should be evocative and encapsulate the Act's main theme or pivotal outcome (ideally 3-7 words).",
            )
            appendLine(
                "2.  Generate a concise 'content' for this Act (typically 2-3 paragraphs, around 150-250 words). This description should:",
            )
            appendLine(
                "a.  Summarize the main plot developments, character arcs, and key resolutions that occurred across all chapters in `CHAPTERS_IN_CURRENT_ACT`.",
            )
            appendLine("b.  Reflect the overall tone and significance of this Act within the larger saga (`SAGA_DATA`).")
            appendLine(
                "c.  If `PREVIOUS_ACT_DATA` is provided, ensure your description provides a sense of narrative flow from that previous Act.",
            )
            appendLine(
                "d.  Conclude with a sentence or two that creates a natural hook or sets the stage for the subsequent Act, hinting at unresolved threads or future directions.",
            )
            appendLine("")
            appendLine("Consider the `MAIN_CHARACTER`'s journey if their data is provided and relevant to the Act's core.")
            appendLine("")
            appendLine("OUTPUT_FORMAT_EXPECTED:")
            appendLine(actOutput)
        }.trimIndent()
    }

    @Suppress("ktlint:standard:max-line-length")
    fun actDirective(directive: String) =
        """
         ## SAGA ACT DIRECTIVE
        // This directive guides the narrative'''s overall progression and pacing based on the current act.
        // It dictates the specific tone, focus, and goal for your responses and the evolving plot.
        $directive
        """.trimIndent()

    fun actIntroductionPrompt(
        saga: Saga,
        previousAct: ActContent? = null,
    ): String =
        buildString {
            appendLine("CONTEXT:")
            appendLine("You are an AI assistant helping to write a saga.")
            appendLine("Saga Title: \"${saga.title}\"")
            appendLine("Saga Genre: ${saga.genre.title}")

            if (previousAct == null) {
                // First Act
                appendLine("Saga Description: \"${saga.description}\"")
                appendLine("\nTASK:")
                appendLine(
                    "Generate a single, compelling and concise introductory paragraph (around 50-70 words) for the very FIRST ACT of this saga.",
                )
                appendLine(
                    "The introduction must set the initial scene and tone based on the saga's overall description and genre, and end with a light hook.",
                )
            } else {
                // Subsequent Act
                val previousActSummary = previousAct.data.content.take(200)
                val previousActEmotionalReview = previousAct.data.emotionalReview ?: "neutral"
                appendLine("Previous Act ended with summary: \"$previousActSummary\"")
                appendLine("Previous Act emotional tone: \"$previousActEmotionalReview\"")
                appendLine("\nTASK:")
                appendLine(
                    "Generate a single, compelling and concise introductory paragraph (around 50-70 words) for the NEXT ACT of this saga.",
                )
                appendLine(
                    "The introduction must smoothly transition from the end of the previous act, using its tone/cliffhanger to create continuity, and engage the reader to continue.",
                )
            }
            appendLine("Output only the introduction paragraph, no titles, quotes, or extra text.")
        }
}
