package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.home.data.model.SagaContent

object ActPrompts {
    fun generateAct(
        content: SagaContent,
        purpose: String,
    ) = """
        You are the "Saga Act Chronicler", an AI specialized in summarizing major narrative arcs for text-based RPGs.
        Your task is to provide a compelling overview of a completed act, based on its constituent chapters and the overall saga timeline.
        ---
        ## SAGA CONTEXT
        // This section provides high-level information about the saga to help you contextualize the events.
        ${SagaPrompts.details(content.data)}
        ---
        ## FULL SAGA TIMELINE
        // This array contains a chronological list of ALL key events that have occurred throughout the entire saga up to this point.
        // Use this to understand the broader context and ensure the act summary is consistent with the entire narrative.
        [ ${content.timelines.formatToJsonArray()} ] 
        ---
        ## CHAPTERS IN THIS ACT
        // This array contains the full content of the 10 chapters that comprise the act you need to summarize.
        // Analyze these chapters to extract the main plot points, character developments, conflicts, and resolutions within this specific act.
        [ ${content.currentActInfo?.chapters?.formatToJsonArray()} ] 
        ---
        ## ACT DESCRIPTION PURPOSE
        // This instruction dictates the specific tone, focus, and goal for the 'content' you need to generate.
        // It tells you how to frame the summary based on which act it is.
        $purpose

        ---
        **GENERATE A JSON RESPONSE for the act.**
        // The 'content' should be a narrative summary (2-4 paragraphs) that captures the essence of the completed act.
        // Ensure consistency with the provided SAGA CONTEXT and FULL SAGA TIMELINE.
        **OUTPUT FORMAT (JSON):**
        ${toJsonMap(Act::class.java)}
        """

    fun actDirective(directive: String) =
        """
         ## SAGA ACT DIRECTIVE
        // This directive guides the narrative's overall progression and pacing based on the current act.
        // It dictates the specific tone, focus, and goal for your responses and the evolving plot.
        $directive
        """.trimIndent()

    fun actSummaries(saga: SagaContent): String =
        saga.acts
            .map { act ->
                val actIndex = saga.acts.indexOf(act) + 1
                val chapters = saga.chapters.filter { chapter -> chapter.actId == act.id }
                """
                - Act $actIndex ${act.title}: ${act.content}
                Chapters Summary:
                ${chapters.map { chapter ->
                    val chapterIndex = chapters.indexOf(chapter) + 1
                    """
                - Chapter $chapterIndex ${chapter.title}: ${chapter.overview}    
                """
                }.joinToString(separator = "\n") { it }}
                """.trimIndent()
            }.joinToString(separator = "\n") { it }
            .trimIndent()
}
