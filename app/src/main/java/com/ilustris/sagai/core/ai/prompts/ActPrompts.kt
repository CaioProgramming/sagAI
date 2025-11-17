package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.models.ActConclusionContext
import com.ilustris.sagai.core.utils.listToAINormalize
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonFormatIncludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.timeline.data.model.Timeline

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
        saga: SagaContent,
        previousAct: ActContent? = null,
    ): String {
        val actNumber = if (previousAct == null) 1 else saga.actNumber(previousAct.data) + 1
        val actTitle = "Act $actNumber"

        val recentEvents = saga.flatEvents().map { it.data }.takeLast(6)
        val recentMsgs = saga.flatMessages().map { it.message }.takeLast(8)

        fun List<Timeline>.toBulletList(): String {
            if (this.isEmpty()) return ""
            return this.joinToString(separator = "\n") { t -> "- ${t.title}: ${t.content.take(120).replace('\n', ' ')}" }
        }

        fun List<Message>.toBulletList(): String {
            if (this.isEmpty()) return ""
            return this.joinToString(separator = "\n") { m -> "- ${m.speakerName ?: m.senderType}: ${m.text.take(140).replace('\n', ' ')}" }
        }

        val recentTimelineBullets = recentEvents.toBulletList()
        val recentMessagesBullets = recentMsgs.toBulletList()

        return buildString {
            appendLine("CONTEXT:")
            appendLine("You are a master storyteller and AI assistant crafting a saga.")
            appendLine(
                "Your current task is to write the introduction for '$actTitle'. This introduction should feel like the opening page of a new book in a series.",
            )
            appendLine("## Saga Overview:")
            appendLine(saga.data.toJsonFormatExcludingFields(ChatPrompts.sagaExclusions))

            appendLine("### Main Character:")
            appendLine(saga.mainCharacter?.data?.toJsonFormatExcludingFields(ChatPrompts.characterExclusions))

            if (recentTimelineBullets.isNotBlank()) {
                appendLine("## Latest Timeline (most recent events):")
                appendLine(recentTimelineBullets)
            }

            if (recentMessagesBullets.isNotBlank()) {
                appendLine("## Recent Messages (most recent player/narrative messages):")
                appendLine(recentMessagesBullets)
            }

            if (previousAct == null) {
                appendLine("## Task: Write the Opening Introduction")
                if (recentTimelineBullets.isNotBlank() || recentMessagesBullets.isNotBlank()) {
                    appendLine(
                        "This is the very beginning of the entire saga. Your introduction must immediately immerse the reader into the world described in the 'Saga Overview'. Use the 'Latest Timeline' and 'Recent Messages' sections to ground the opening and to reference the immediate cliffhanger or recent event that motivates the story when applicable.",
                    )
                } else {
                    appendLine(
                        "This is the very beginning of the entire saga. Your introduction must immediately immerse the reader into the world described in the 'Saga Overview'. Build your opening from the saga's overview and main character details; do not reference timeline or messages because none are available.",
                    )
                }
                appendLine(
                    "1.  **Establish the Atmosphere:** Open with descriptive language that sets the scene, tone, and genre defined in the saga's context.",
                )
                appendLine(
                    "2.  **Introduce the Initial State:** Hint at the current state of the world or the main character's initial situation without giving too much away.",
                )
                appendLine(
                    "3.  **Create a Hook:** Conclude with a compact, compelling hook that makes the reader eager to know what happens next.",
                )
            } else {
                appendLine("## Previous Act Summary:")
                appendLine(
                    previousAct.data.toJsonFormatExcludingFields(
                        listOf("id", "sagaId", "currentChapterId"),
                    ),
                )
                appendLine("")
                appendLine("## Task: Write a Transitional Introduction")
                if (recentTimelineBullets.isNotBlank() || recentMessagesBullets.isNotBlank()) {
                    appendLine(
                        "This introduction must serve as a bridge from the previous Act. Use the 'Latest Timeline' and 'Recent Messages' sections to reference the immediate cliffhanger or final beats from the previous act and set the stage for the new act.",
                    )
                } else {
                    appendLine(
                        "This introduction must serve as a bridge from the previous Act. Concisely reference the closing beats from the Previous Act Summary and set the stage for the new act; no recent timeline or message data is available.",
                    )
                }
                appendLine(
                    "1.  **Acknowledge the Past (brief):** Concisely reference the closing event or lingering mood from the 'Previous Act Summary'.",
                )
                appendLine("2.  **Re-establish the Scene (concise):** Set the new scene and indicate any tonal shift since the last act.")
                appendLine(
                    "3.  **Propel the Narrative Forward (compact hook):** End with a short forward-facing sentence that signals the new direction or conflict.",
                )
            }

            appendLine("")
            appendLine("## Style & Format Guidelines:")
            appendLine("- **Length:** Maximum of 2 paragraphs. Keep the introduction compact — aim for ~80-140 words total.")
            appendLine("- **Tone:** The tone must be literary and engaging, matching the saga's genre.")
            appendLine(
                "- **Use Context:** Explicitly leverage the 'Latest Timeline' and 'Recent Messages' to make the final sentence/hook connect naturally to the saga's most recent cliffhanger.",
            )
            appendLine(
                "- **Output:** Provide ONLY the introduction text. Do not include titles, quotation marks, or any other meta-commentary.",
            )
        }
    }

    fun actsOverview(saga: SagaContent) =
        buildString {
            val acts = saga.acts.filter { it.isComplete() }.map { it.data }
            if (saga.acts.isNotEmpty()) {
                appendLine("## SAGA ACTS OVERVIEW")
                appendLine("// This overview provides a summary of all acts in the saga to inform narrative consistency and progression.")
                appendLine("// Use this information to maintain continuity and reference past events as needed.")
                appendLine(
                    acts.listToAINormalize(
                        excludingFields = listOf("id", "sagaId", "currentChapterId", "emotionalReview"),
                    ),
                )
            }
        }
}
