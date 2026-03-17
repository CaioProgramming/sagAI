package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getPurpose
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.timeline.data.model.Timeline

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
    val actTitle: String,
    val sagaOverview: String,
    val mainCharacterData: String,
    val recentTimelineContext: String,
    val recentMessagesContext: String,
    val taskInstruction: String,
    val conversationDirective: String,
    val previousActSummary: String = "",
)

object ActPrompts {
    val ACT_EXCLUSIONS = listOf("id, sagaId, currentChapterId")

    @Suppress("ktlint:standard:max-line-length")
    suspend fun generateActConclusion(
        promptService: PromptService,
        sagaContent: SagaContent,
        currentActContent: ActContent,
        narrativeRules: NarrativeRules,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
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
                "act_closure_instruction_template",
                mapOf("actNumber" to sagaContent.actNumber(currentActContent.data)),
            )

        val args =
            ActConclusionArgs(
                sagaMainContext = SagaPrompts.mainContext(sagaContent),
                actPurposeRule = sagaContent.getPurpose(narrativeRules),
                currentActData = currentActContent.data.toAINormalize(ACT_EXCLUSIONS),
                chaptersInCurrentAct =
                    chapterSummariesInCurrentAct.normalizetoAIItems(
                        ChapterPrompts.CHAPTER_EXCLUSIONS,
                    ),
                previousActContext =
                    previousAct?.data?.toAINormalize(ACT_EXCLUSIONS)
                        ?: "This is the first act, no previous context available.",
                closureInstruction = closureInstruction,
                conversationDirective = config.conversationDirective,
            )

        return promptService.buildRemotePrompt("act_conclusion_blueprint", args)
    }

    suspend fun actIntroductionPrompt(
        promptService: PromptService,
        narrativeRules: NarrativeRules,
        saga: SagaContent,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
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
        val hasContext = recentTimelineBullets.isNotBlank() || recentMessagesBullets.isNotBlank()

        val taskInstruction =
            if (previousAct == null) {
                if (hasContext) narrativeRules.act1IntroWithContext else narrativeRules.act1IntroWithoutContext
            } else {
                if (hasContext) narrativeRules.transitionalActIntroWithContext else narrativeRules.transitionalActIntroWithoutContext
            }

        val args =
            ActIntroArgs(
                actTitle = actTitle,
                sagaOverview = saga.data.toJsonFormatExcludingFields(ChatPrompts.sagaExclusions),
                mainCharacterData =
                    saga.mainCharacter?.data?.toJsonFormatExcludingFields(ChatPrompts.characterExclusions)
                        ?: "No character info available",
                recentTimelineContext = recentTimelineBullets.ifBlank { "No recent timeline events available." },
                recentMessagesContext = recentMessagesBullets.ifBlank { "No recent messages available." },
                taskInstruction = taskInstruction,
                conversationDirective = config.conversationDirective,
                previousActSummary =
                    previousAct?.data?.toJsonFormatExcludingFields(
                        listOf("id", "sagaId", "currentChapterId"),
                    ) ?: "",
            )

        return promptService.buildRemotePrompt("act_introduction_blueprint", args)
    }

    suspend fun actsOverview(
        promptService: PromptService,
        saga: SagaContent,
    ): String {
        val acts = saga.acts.filter { it.isComplete() }.map { it.data }
        if (acts.isEmpty()) return ""

        val actsSummary =
            acts.normalizetoAIItems(
                excludingFields = listOf("id", "sagaId", "currentChapterId", "emotionalReview"),
            )

        return promptService.buildRemotePrompt(
            "acts_overview_blueprint",
            ActOverviewArgs(actsSummary),
        )
    }
}
