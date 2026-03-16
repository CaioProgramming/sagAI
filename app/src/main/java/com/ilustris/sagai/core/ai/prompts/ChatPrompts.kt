package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentMessages
import com.ilustris.sagai.features.home.data.model.getDirective
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix

data class ReplyMessageArgs(
    val sceneStateBlock: String,
    val conversationHistory: String,
    val actDirective: String,
    val sagaMainContext: String,
    val externalCharactersBlock: String,
    val backgroundContinuityBlock: String,
    val conversationDirective: String,
    val latestMessageContent: String,
)

data class SceneStateArgs(
    val sceneSummary: String,
    val charactersInScene: String,
    val relationshipsBlock: String,
    val recentChanges: String,
    val narrativeGuidance: String,
)

data class ExternalCharactersArgs(
    val charactersSummary: String,
)

data class BackgroundContinuityArgs(
    val establishedFacts: String,
)

data class TypoFixArgs(
    val sagaMainContext: String,
    val genreName: String,
    val conversationDirective: String,
    val recentContext: String,
    val message: String,
    val outputStructure: String,
)

data class ReactionArgs(
    val sagaMainContext: String,
    val sceneSummary: String,
    val charactersPresent: String,
    val messageToReact: String,
    val relationshipsBlock: String,
    val conversationDirective: String,
    val genreName: String,
)

data class LatestMessageArgs(
    val messageContent: String,
)

data class SceneSummaryArgs(
    val sagaMainContext: String,
    val activeSegmentSummary: String,
    val historicalContext: String,
    val conversationHistory: String,
    val latestMessageBlock: String,
)

data class NotificationArgs(
    val sagaMainContext: String,
    val sceneSummaryBlock: String,
    val characterContext: String,
    val relationshipBlock: String,
    val conversationHistory: String,
    val characterName: String,
    val sagaMainCharName: String,
    val conversationDirective: String,
)

object ChatPrompts {
    val messageExclusions =
        listOf(
            "id",
            "timestamp",
            "sagaId",
            "characterId",
            "timelineId",
            "status",
            "playTimeMs",
            "audioPath",
            "audible",
            "status",
        )
    val sagaExclusions =
        listOf(
            "id",
            "icon",
            "review",
            "createdAt",
            "endedAt",
            "mainCharacterId",
            "currentActId",
            "isEnded",
            "isDebug",
            "endMessage",
            "review",
            "playTimeMs",
            "narratorVoice",
        )

    val characterExclusions =
        listOf(
            "id",
            "image",
            "sagaId",
            "joinedAt",
            "details",
            "emojified",
            "hexColor",
            "firstSceneId",
            "smartZoom",
            "events",
            "relationships",
        )

    @Suppress("ktlint:standard:max-line-length")
    suspend fun replyMessagePrompt(
        promptService: PromptService,
        promptRules: PromptRules,
        promptDirectives: PromptDirectives,
        narrativeRules: NarrativeRules,
        saga: SagaContent,
        message: Message,
        sceneSummary: SceneSummary?,
        config: GenreConfig,
    ): String {
        val charactersInScene =
            sceneSummary?.charactersPresent?.mapNotNull {
                saga.findCharacter(it)
            }
        val sceneStateBlock =
            sceneSummary?.let { summary ->
                assembleSceneStateBlock(promptService, summary, saga, charactersInScene)
            } ?: ""

        val externalCharactersBlock =
            assembleExternalCharactersBlock(promptService, saga, charactersInScene)

        val backgroundContinuityBlock =
            assembleBackgroundContinuityBlock(promptService, sceneSummary)

        val args =
            ReplyMessageArgs(
                sceneStateBlock = sceneStateBlock,
                conversationHistory = conversationHistory(promptDirectives, saga),
                actDirective = saga.getDirective(narrativeRules),
                sagaMainContext = SagaPrompts.mainContext(saga),
                externalCharactersBlock = externalCharactersBlock,
                backgroundContinuityBlock = backgroundContinuityBlock,
                conversationDirective = config.conversationDirective,
                latestMessageContent = message.toAINormalize(messageExclusions),
            )

        return promptService.buildRemotePrompt("reply_message_prompt", args)
    }

    private suspend fun assembleSceneStateBlock(
        promptService: PromptService,
        summary: SceneSummary,
        saga: SagaContent,
        charactersInScene: List<CharacterContent>?,
    ): String {
        val exclusions = listOf("establishedFacts", "worldStateChanges", "possibleOutcomes")
        val nonMainCharacters =
            charactersInScene
                ?.filter { it.data.id != saga.mainCharacter?.data?.id }
                ?.map { it.data }
                ?: emptyList()

        val relationshipsBlock =
            charactersInScene?.joinToString("\n") { characterContent ->
                "- ${characterContent.data.name} relationships: ${
                    characterContent.summarizeRelationships(
                        1,
                    )
                }"
            } ?: ""

        val recentChanges =
            summary.worldStateChanges?.joinToString("\n") { "- $it" } ?: ""

        val narrativeGuidance =
            summary.possibleOutcomes?.takeIf { it.isNotEmpty() }?.let { outcomes ->
                outcomes.joinToString("\n") { outcome -> "- $outcome" }
            } ?: ""

        val args =
            SceneStateArgs(
                sceneSummary = summary.toAINormalize(exclusions),
                charactersInScene = CharacterPrompts.charactersOverview(nonMainCharacters),
                relationshipsBlock = relationshipsBlock,
                recentChanges = recentChanges,
                narrativeGuidance = narrativeGuidance,
            )

        return promptService.buildRemotePrompt("scene_state_block_template", args)
    }

    private suspend fun assembleExternalCharactersBlock(
        promptService: PromptService,
        saga: SagaContent,
        charactersInScene: List<CharacterContent>?,
    ): String {
        val charactersInSceneIds = charactersInScene?.map { it.data.id } ?: emptyList()
        val externalCharacters = saga.getCharacters(true).filter { it.id !in charactersInSceneIds }

        if (externalCharacters.isEmpty()) return ""

        val charactersSummary =
            externalCharacters
                .map {
                    it.copy(
                        backstory = if (it.backstory.length > 150) it.backstory.take(150) + "..." else it.backstory,
                        knowledge = emptyList(),
                    )
                }.normalizetoAIItems(characterExclusions)

        val args = ExternalCharactersArgs(charactersSummary = charactersSummary)

        return promptService.buildRemotePrompt("external_characters_block_template", args)
    }

    private suspend fun assembleBackgroundContinuityBlock(
        promptService: PromptService,
        sceneSummary: SceneSummary?,
    ): String {
        val facts = sceneSummary?.establishedFacts
        if (facts.isNullOrEmpty()) return ""

        val args = BackgroundContinuityArgs(establishedFacts = facts.joinToString("\n") { "- $it" })

        return promptService.buildRemotePrompt("background_continuity_block_template", args)
    }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun checkForTypo(
        promptService: PromptService,
        promptRules: PromptRules,
        promptDirectives: PromptDirectives,
        saga: SagaContent,
        config: GenreConfig,
        message: String,
        lastMessage: Message? = null,
    ): String {
        val recentContext =
            lastMessage?.let {
                promptService.buildPrompt(
                    promptDirectives.recentContext.ifBlank { StorytellingDirective.RECENT_CONTEXT },
                    mapOf("message" to it.text),
                )
            } ?: ""

        val args =
            TypoFixArgs(
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                genreName = saga.data.genre.name,
                conversationDirective = config.conversationDirective,
                recentContext = recentContext,
                message = message,
                outputStructure = toJsonMap(TypoFix::class.java),
            )

        return promptService.buildRemotePrompt("check_for_typo_prompt", args)
    }

    suspend fun generateReactionPrompt(
        promptService: PromptService,
        promptDirectives: PromptDirectives,
        summary: SceneSummary,
        saga: SagaContent,
        messageToReact: Message,
        config: GenreConfig,
    ): String {
        val mainCharacter = saga.mainCharacter!!
        val characters = summary.charactersPresent.mapNotNull { saga.findCharacter(it)?.data }
        val relationshipsBlock =
            buildString {
                characters.forEach {
                    mainCharacter.findRelationship(it.id)?.let { relation ->
                        appendLine(relation.summarizeRelation(1))
                    }
                }
            }

        val args =
            ReactionArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                sceneSummary = summary.toAINormalize(),
                charactersPresent = summary.charactersPresent.joinToString(),
                messageToReact = messageToReact.text,
                relationshipsBlock = relationshipsBlock,
                conversationDirective = config.conversationDirective,
                genreName = saga.data.genre.name,
            )

        return promptService.buildRemotePrompt("reaction_generation_prompt", args)
    }

    suspend fun sceneSummarizationPrompt(
        promptService: PromptService,
        promptRules: PromptRules,
        promptDirectives: PromptDirectives,
        saga: SagaContent,
    ): String {
        val historicalContext =
            buildString {
                saga.acts.forEach {
                    appendLine(it.actSummary(saga, false))
                }
            }

        val latestMessage = saga.flatMessages().maxByOrNull { it.message.timestamp }?.message
        val latestMessageBlock = assembleLatestMessageBlock(promptService, latestMessage)

        val args =
            SceneSummaryArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                activeSegmentSummary = saga.currentActInfo?.actSummary(saga, true) ?: "",
                historicalContext = historicalContext,
                conversationHistory = conversationHistory(promptDirectives, saga),
                latestMessageBlock = latestMessageBlock,
            )

        return promptService.buildRemotePrompt("scene_summarization_prompt", args)
    }

    private suspend fun assembleLatestMessageBlock(
        promptService: PromptService,
        latestMessage: Message?,
    ): String {
        if (latestMessage == null) return ""

        val args =
            LatestMessageArgs(messageContent = latestMessage.toAINormalize(messageExclusions))

        return promptService.buildRemotePrompt("latest_message_block_template", args)
    }

    suspend fun scheduledNotificationPrompt(
        promptService: PromptService,
        promptDirectives: PromptDirectives,
        saga: SagaContent,
        selectedCharacter: CharacterContent,
        sceneSummary: SceneSummary,
        config: GenreConfig,
    ): String {
        val relationWithCharacter = selectedCharacter.findRelationship(saga.mainCharacter!!.data.id)
        val relationshipBlock = relationWithCharacter?.summarizeRelation(1) ?: ""

        val args =
            NotificationArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                sceneSummaryBlock = sceneSummary.toAINormalize(),
                characterContext = selectedCharacter.data.toAINormalize(characterExclusions),
                relationshipBlock = relationshipBlock,
                conversationHistory = conversationHistory(promptDirectives, saga),
                characterName = selectedCharacter.data.name,
                sagaMainCharName = saga.mainCharacter.data.name,
                conversationDirective = config.conversationDirective,
            )

        return promptService.buildRemotePrompt("notification_prompt", args)
    }

    fun conversationHistory(
        promptDirectives: PromptDirectives,
        saga: SagaContent,
        threshold: Int = UpdateRules.LORE_UPDATE_LIMIT,
    ) = buildString {
        val currentMessages =
            saga
                .getCurrentMessages()
                ?.sortedBy { it.timestamp }
                ?: emptyList()

        val header =
            promptDirectives.conversationHistory.ifBlank {
                StorytellingDirective.CONVERSATION_HISTORY
            }

        if (currentMessages.size >= 5) {
            appendLine(header)
            appendLine(
                currentMessages
                    .takeLast(threshold)
                    .normalizetoAIItems(excludingFields = messageExclusions),
            )
        } else {
            val recentGlobalMessages =
                saga
                    .flatMessages()
                    .map { it.message }
                    .sortedBy { it.timestamp }
                    .takeLast(threshold)

            appendLine(header)
            appendLine(
                recentGlobalMessages
                    .normalizetoAIItems(excludingFields = messageExclusions),
            )
        }
    }
}
