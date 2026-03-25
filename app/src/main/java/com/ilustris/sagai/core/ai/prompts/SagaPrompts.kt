package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.ChatPrompts.messageExclusions
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters

data class EndCreditsArgs(
    val sagaMainContext: String,
    val charactersBlock: String,
    val relationshipBlock: String,
    val historyBlock: String,
    val conversationDirective: String,
)

data class IconDescriptionArgs(
    val artStyle: String,
    val criticalRules: String,
    val context: String,
    val visualDirection: String,
    val characterHexColor: String?,
)

data class ReviewGenerationArgs(
    val relationshipBlock: String,
    val emotionalRanking: String,
    val emotionalSummary: String,
    val expressiveMessagesCount: String,
    val actionCount: String,
    val thinkCount: String,
    val narratorCount: String,
    val actsHistory: String,
    val charactersRanking: String,
    val conversationDirective: String,
)

data class StoryBriefingArgs(
    val sagaTitle: String,
    val genreName: String,
    val protagonistName: String,
    val actsHistory: String,
    val recentMessages: String,
    val conversationDirective: String,
)

data class SagaResumeArgs(
    val sagaTitle: String,
    val sagaContext: String,
    val sceneSummary: String,
    val genreName: String,
    val conversationDirective: String,
)

object SagaPrompts {
    const val REVIEW_GENERATION_BLUEPRINT = "review_generation_blueprint"
    const val SAGA_END_CREDITS_BLUEPRINT = "saga_end_credits_blueprint"
    const val SAGA_RESUME_BLUEPRINT = "saga_resume_blueprint"
    const val STORY_BRIEFING_BLUEPRINT = "story_briefing_blueprint"

    fun mainContext(
        saga: SagaContent,
        character: CharacterContent? = null,
        ommitCharacter: Boolean = false,
    ) = buildString {
        val selectedCharacter = character ?: saga.mainCharacter
        appendLine("Story: ")
        appendLine(saga.data.toAINormalize(ChatPrompts.sagaExclusions))
        if (ommitCharacter.not()) {
            selectedCharacter?.let {
                appendLine("Character context:")
                appendLine(it.data.toAINormalize(ChatPrompts.characterExclusions))
            }
        }
    }

    suspend fun endCredits(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): String {
        val args =
            EndCreditsArgs(
                sagaMainContext = mainContext(saga),
                charactersBlock =
                    saga.characters
                        .map { it.data }
                        .normalizetoAIItems(ChatPrompts.characterExclusions),
                relationshipBlock = saga.mainCharacter?.summarizeRelationships() ?: "",
                historyBlock = saga.acts.joinToString("\n") { it.actSummary(false) },
                conversationDirective = conversationDirective,
            )

        return promptService.buildRemotePrompt(SAGA_END_CREDITS_BLUEPRINT, args)
    }

    suspend fun reviewGeneration(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): String {
        val topInteractiveCharacters =
            saga.flatMessages().rankTopCharacters(saga.characters.map { it.data })
        val userMessages =
            saga.flatMessages().filter { it.character?.id == saga.mainCharacter?.data?.id }
        val actionCount = userMessages.count { it.message.text.contains("<action>") }
        val thinkCount = userMessages.count { it.message.text.contains("<think>") }
        val narratorCount = userMessages.count { it.message.text.contains("<narrator>") }
        val totalExpressive = actionCount + thinkCount + narratorCount

        val args =
            ReviewGenerationArgs(
                relationshipBlock = saga.mainCharacter?.summarizeRelationships() ?: "",
                emotionalRanking =
                    saga.flatMessages().rankEmotionalTone().joinToString("\n") {
                        "${it.first.name} - ${it.second.size} messages."
                    },
                emotionalSummary = saga.emotionalSummary(),
                expressiveMessagesCount = totalExpressive.toString(),
                actionCount = actionCount.toString(),
                thinkCount = thinkCount.toString(),
                narratorCount = narratorCount.toString(),
                actsHistory = saga.acts.joinToString("\n") { it.actSummary(false) },
                charactersRanking =
                    topInteractiveCharacters.joinToString(";\n") {
                        "name: ${it.first.name}, messageCount: ${it.second}"
                    },
                conversationDirective = conversationDirective,
            )

        return promptService.buildRemotePrompt(REVIEW_GENERATION_BLUEPRINT, args)
    }

    suspend fun generateStoryBriefing(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): String {
        val args =
            StoryBriefingArgs(
                sagaTitle = saga.data.title,
                genreName = saga.data.genre.name,
                protagonistName = saga.mainCharacter?.data?.name ?: "Unnamed Hero",
                actsHistory = saga.acts.joinToString("; ") { it.actSummary() },
                recentMessages =
                    saga
                        .flatMessages()
                        .takeLast(5)
                        .reversed()
                        .map { it.message }
                        .normalizetoAIItems(excludingFields = messageExclusions),
                conversationDirective = conversationDirective,
            )

        return promptService.buildRemotePrompt(STORY_BRIEFING_BLUEPRINT, args)
    }

    suspend fun sagaResume(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): String {
        val args =
            SagaResumeArgs(
                sagaTitle = saga.data.title,
                sagaContext = mainContext(saga),
                sceneSummary =
                    if (saga.acts.isEmpty()) {
                        ""
                    } else {
                        saga.acts.joinToString("\n") { it.actSummary() }
                    },
                genreName = saga.data.genre.name,
                conversationDirective = conversationDirective,
            )

        return promptService.buildRemotePrompt(SAGA_RESUME_BLUEPRINT, args)
    }
}
