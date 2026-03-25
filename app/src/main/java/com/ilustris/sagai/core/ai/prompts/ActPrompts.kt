package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.getPurposeKey

data class ActConclusionArgs(
    val sagaMainContext: String,
    val actPurposeRule: String,
    val currentActData: String,
    val chaptersInCurrentAct: String,
    val previousActContext: String,
    val closureInstruction: String,
    val conversationDirective: String,
)

data class ActDirectiveArgs(
    val directive: String,
)

data class ActOverviewArgs(
    val actsSummary: String,
)

data class ActIntroArgs(
    val context: String,
    val pastEvents: String? = null,
)

object ActPrompts {
    const val ACT_CLOSURE_INSTRUCTION_TEMPLATE = "act_closure_instruction_template"
    const val ACT_CONCLUSION_BLUEPRINT = "act_conclusion_blueprint"

    val ACT_EXCLUSIONS = listOf("id, sagaId, currentChapterId")

    @Suppress("ktlint:standard:max-line-length")
    suspend fun generateActConclusion(
        promptService: PromptService,
        sagaContent: SagaContent,
        currentActContent: ActContent,
        narrativeRules: NarrativeRules,
        config: GenreConfig,
    ): String {
        val isFirstAct =
            sagaContent.acts
                .firstOrNull()
                ?.data
                ?.id == currentActContent.data.id

        val previousAct =
            if (isFirstAct) {
                null
            } else {
                val currentIndex =
                    sagaContent.acts.indexOfFirst { it.data.id == currentActContent.data.id }
                if (currentIndex > 0) sagaContent.acts[currentIndex - 1] else null
            }

        val chapterSummariesInCurrentAct = currentActContent.chapters.map { it.data }

        val closureInstruction =
            promptService.buildRemotePrompt(
                ACT_CLOSURE_INSTRUCTION_TEMPLATE,
                mapOf("actNumber" to sagaContent.actNumber(currentActContent.data)),
            )

        val args =
            ActConclusionArgs(
                sagaMainContext = SagaPrompts.mainContext(sagaContent),
                actPurposeRule =
                    promptService.buildRemotePrompt(
                        sagaContent.getPurposeKey(),
                        emptyMap(),
                    ),
                currentActData = currentActContent.data.toAINormalize(ACT_EXCLUSIONS),
                chaptersInCurrentAct =
                    chapterSummariesInCurrentAct.normalizetoAIItems(
                        ChapterPrompts.CHAPTER_EXCLUSIONS,
                    ),
                previousActContext =
                    previousAct?.data?.toAINormalize(ACT_EXCLUSIONS)
                        ?: "This is the first act, no previous context available.",
                closureInstruction = closureInstruction,
                conversationDirective = "",
            )

        return promptService.buildRemotePrompt(ACT_CONCLUSION_BLUEPRINT, args)
    }

    suspend fun actIntroductionPrompt(
        promptService: PromptService,
        saga: SagaContent,
        narrativeRules: NarrativeRules,
        conversationDirective: String,
    ): String {
        val blueprintKey =
            if (saga.currentActInfo == saga.acts.first()) "act_1_intro_blueprint" else "transitional_act_intro_blueprint"

        val isFirstAct = saga.currentActInfo == saga.acts.first()
        val historyEvents = saga.acts.filter { it.isComplete(narrativeRules) }

        val args =
            ActIntroArgs(
                context = SagaPrompts.mainContext(saga),
                pastEvents =
                    if (isFirstAct) {
                        null
                    } else {
                        historyEvents.joinToString("\n") {
                            it.actSummary(it == historyEvents.last())
                        }
                    },
            )

        return promptService.buildRemotePrompt(blueprintKey, args)
    }
}
