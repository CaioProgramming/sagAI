package com.ilustris.sagai.core.ai.prompts

// Ensure necessary imports are present for Saga and ActContent if not already
import com.ilustris.sagai.features.home.data.model.Saga // For ActGenerationPromptContext.sagaGlobalContext
import com.ilustris.sagai.features.act.data.model.ActContent // For ActGenerationPromptContext fields

import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.formatToJsonArray // May not be needed if direct serialization of list works as expected with toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonFormat // For parts of the context if needed, or if toJsonFormatExcludingFields is not used on the whole context object
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields // Crucial for this new approach
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters

object ActPrompts {

    private data class ActGenerationPromptContext(
        val sagaGlobalContext: Saga,
        val fullSagaTimeline: List<ActContent>,
        val chaptersForThisActSummary: ActContent?,
        val actDescriptionPurpose: String,
        val charactersCast: List<Character>,
    )

    fun generateAct(
        content: SagaContent,
        purpose: String,
    ): String {
        val promptDataContext = ActGenerationPromptContext(
            sagaGlobalContext = content.data,
            fullSagaTimeline = content.acts,
            chaptersForThisActSummary = content.currentActInfo,
            actDescriptionPurpose = purpose,
            charactersCast = content.getCharacters()
        )


        val combinedContextJson = promptDataContext
            .toJsonFormatExcludingFields(
                listOf(
                    "id",
                    "messages",
                    "icon",
                    "coverImage",
                    "isEnded",
                    "endedAt",
                    "isDebug",
                    "endMessage",
                    "review",
                    "createdAt",
                    "currentActId",
                    "sagaId",
                    "chapterId",
                    "visualDescription",
                    "featuredCharacters",
                    "details",
                    "hexColor",
                    "image",
                    "sagaId",
                    "joinedAt"
                )
            )

        return """
        You are the "Saga Act Chronicler", an AI specialized in summarizing major narrative arcs for text-based RPGs.
        Your task is to provide a compelling overview of a completed act, based on the following comprehensive context:
        $combinedContextJson

        ---
        **GENERATE A JSON RESPONSE for the act.**
        // The '''content''' should be a narrative summary (2-4 paragraphs) that captures the essence of the completed act.
        // Ensure consistency with the provided SAGA CONTEXT and FULL SAGA TIMELINE (which are now part of the JSON above under their respective keys).
        **OUTPUT FORMAT (JSON):**
        ${toJsonMap(Act::class.java)}
        """
            .trimIndent()
    }

    fun actDirective(directive: String) =
        """
         ## SAGA ACT DIRECTIVE
        // This directive guides the narrative'''s overall progression and pacing based on the current act.
        // It dictates the specific tone, focus, and goal for your responses and the evolving plot.
        $directive
        """.trimIndent()

}
