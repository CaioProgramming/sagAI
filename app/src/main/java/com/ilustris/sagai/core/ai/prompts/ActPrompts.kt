package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getDirectiveKey

/**
 * Arguments for the Act Conclusion prompt.
 * Focuses on narrative continuity and thematic closure for a literary "Volume".
 */
data class ActConclusionArgs(
    val sagaTitle: String,
    val actTitle: String,
    val sagaMainContext: String,
    val chaptersInCurrentAct: String,
    val actPurposeRule: String,
    val conversationDirective: String,
    val previousActContext: String,
)

data class ActIntroductionArgs(
    val sagaMainContext: String,
    val lastStateContext: String,
    val narrativeStyle: String,
)

data class ActSynthesisArgs(
    val actContext: String,
    val characterIndex: String,
    val narrativeStyle: String,
)

/**
 * Prompts for Act-level operations, such as creating conclusions for story volumes.
 */
object ActPrompts {
    /**
     * The blueprint used to wrap the act conclusion logic.
     */
    const val ACT_CONCLUSION_BLUEPRINT = "act_conclusion_blueprint"
    const val ACT_INTRODUCTION_BLUEPRINT = "act_introduction_blueprint"
    const val ACT_SYNTHESIS_BLUEPRINT = "act_synthesis_blueprint"

    /**
     * Fields to exclude when normalizing Act data for the AI.
     */
    val ACT_EXCLUSIONS =
        listOf(
            "id",
            "sagaId",
            "currentChapterId",
            "isComplete",
            "introduction",
        )

    /**
     * Generates a prompt to conclude an Act (Volume) of the saga.
     *
     * @param promptService The service used to build prompts from blueprints.
     * @param sagaContent The full content of the saga.
     * @param currentActContent The content of the act being concluded.
     * @param narrativeRules The rules governing the narrative flow.
     * @param genreConfig The configuration for the saga's genre.
     */
    suspend fun generateActConclusion(
        promptService: PromptService,
        sagaContent: SagaContent,
        currentActContent: ActContent,
        conversationDirective: String,
    ): String {
        val isFirst = sagaContent.acts.indexOfFirst { it.data.id == currentActContent.data.id } == 0
        val previousAct =
            if (isFirst) {
                null
            } else {
                sagaContent.acts[
                    sagaContent.acts.indexOfFirst { it.data.id == currentActContent.data.id } -
                        1,
                ]
            }

        val actPurposeRule =
            promptService.buildRemotePrompt(
                currentActContent.getDirectiveKey(sagaContent),
                emptyMap(),
            )

        val args =
            ActConclusionArgs(
                sagaTitle = sagaContent.data.title,
                actTitle = currentActContent.data.title,
                sagaMainContext = SagaPrompts.mainContext(sagaContent),
                chaptersInCurrentAct =
                    currentActContent.chapters.map { it.data }.normalizetoAIItems(
                        ChapterPrompts.CHAPTER_EXCLUSIONS,
                    ),
                actPurposeRule = actPurposeRule,
                conversationDirective = conversationDirective,
                previousActContext =
                    previousAct?.data?.toAINormalize(ACT_EXCLUSIONS)
                        ?: "Initial Volume: The saga begins here, with no prior history recorded.",
            )

        return promptService.buildRemotePrompt(ACT_CONCLUSION_BLUEPRINT, args)
    }

    suspend fun actIntroductionPrompt(
        promptService: PromptService,
        saga: SagaContent,
        narrativeRules: NarrativeRules,
        conversationDirective: String,
    ): String {
        val isFirst = saga.acts.isEmpty()
        val lastState =
            if (isFirst) {
                "THE BEGINNING: The saga starts here. Ground your introduction in the following backstory: ${saga.data.description}"
            } else {
                "THE CONTINUATION: Bridge the story from the previous volume's conclusion: ${saga.acts.lastOrNull()?.data?.content}"
            }

        val args =
            ActIntroductionArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                lastStateContext = lastState,
                narrativeStyle = conversationDirective,
            )
        return promptService.buildRemotePrompt(ACT_INTRODUCTION_BLUEPRINT, args)
    }

    suspend fun actSynthesisPrompt(
        promptService: PromptService,
        saga: SagaContent,
        act: ActContent,
        narrativeRules: NarrativeRules,
        conversationDirective: String,
    ): String {
        val isFirst = saga.acts.indexOfFirst { it.data.id == act.data.id } == 0
        val previousAct =
            if (isFirst) {
                null
            } else {
                val index = saga.acts.indexOfFirst { it.data.id == act.data.id }
                if (index > 0) saga.acts[index - 1] else null
            }

        val actContext =
            buildMap {
                put("sagaData", saga.data.toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS))
                put(
                    "mainCharacter",
                    saga.mainCharacter?.data?.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS),
                )
                put(
                    "chaptersInThisAct",
                    act.chapters
                        .map { it.data }
                        .normalizetoAIItems(ChapterPrompts.CHAPTER_EXCLUSIONS),
                )
                put("previousActData", previousAct?.data?.toAINormalize(ACT_EXCLUSIONS) ?: "None")
            }

        val args =
            ActSynthesisArgs(
                actContext = actContext.toJsonFormat(),
                characterIndex = SagaPrompts.charactersSummary(saga),
                narrativeStyle = conversationDirective,
            )

        return promptService.buildRemotePrompt(ACT_SYNTHESIS_BLUEPRINT, args)
    }
}
