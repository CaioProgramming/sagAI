package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.model.getDirectiveKey
import com.ilustris.sagai.features.home.data.model.historySummary
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary

data class TypoFixArgs(
    val sagaMainContext: String,
    val genreName: String,
    val conversationDirective: String,
    val recentContext: String,
    val message: String,
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

data class ReplyMessageArgs(
    val sceneSummary: String,
    val charactersInScene: String,
    val relationshipsBlock: String,
    val recentChanges: String,
    val narrativeGuidance: String,
    val conversationHistory: String,
    val actDirective: String,
    val sagaMainContext: String,
    val externalCharactersContent: String,
    val backgroundContinuityContent: String,
    val conversationDirective: String,
    val latestMessageContent: String,
    val genreConversationSoul: String,
    val reactionProtocol: String,
)

data class SceneSummaryArgs(
    val sagaContext: String,
    val recentActivity: String,
    val conversationHistory: String,
    val latestMessage: String,
)

data class NotificationArgs(
    val sagaMainContext: String,
    val sceneSummaryContent: String,
    val characterContext: String,
    val relationshipBlock: String,
    val conversationHistory: String,
    val characterName: String,
    val sagaMainCharName: String,
    val conversationDirective: String,
)

object ChatPrompts {
    const val CHAT_NOTIFICATION_BLUEPRINT = "chat_notification_blueprint"
    const val CHAT_REACTION_BLUEPRINT = "chat_reaction_blueprint"
    const val CHAT_WRITING_PAL_BLUEPRINT = "chat_writing_pal_blueprint"
    const val REPLY_GENERATION_BLUEPRINT = "reply_generation_blueprint"
    const val SCENE_SUMMARIZATION_BLUEPRINT = "scene_summarization_blueprint"

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
            "reasoning",
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
            "playTimeMs",
            "narratorVoice",
        )

    private fun characterResolutionProtocol(): String =
        buildString {
            appendLine("### Character Resolution Hierarchy")
            appendLine("1. LOCAL: If the player addresses someone in CHARACTERS PRESENT, that character MUST respond.")
            appendLine(
                "2. GLOBAL: If the addressed name is in KNOWN CAST (NOT PRESENT) or the saga cast, use that character — do NOT set newCharacter.",
            )
            appendLine(
                "3. DISCOVERY: Only set optional newCharacter when a genuinely NEW character enters and speaks for the first time.",
            )
            appendLine(
                "4. CONTINUITY: Identify message.speakerName from the player turn. Do NOT respond as that same character.",
            )
            appendLine()
            appendLine("newCharacter rules (when DISCOVERY applies):")
            appendLine("- Omit newCharacter (null) in most replies.")
            appendLine("- name MUST match message.speakerName.")
            appendLine("- discoveryContext: voice, role, how they entered, dialogue seed for profile generation.")
            appendLine("- Never for NARRATOR senderType.")
            appendLine("- Never if the speaker already exists in the cast.")
        }

    val CHARACTER_EXCLUSIONS =
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
        saga: SagaContent,
        message: Message,
        sceneSummary: SceneSummary?,
        conversationDirective: String,
        updateLimit: Int,
        characterArcsById: Map<Int, List<CharacterArc>> = emptyMap(),
    ): String {
        val charactersInScene =
            sceneSummary?.charactersPresent?.mapNotNull {
                saga.findCharacter(it)
            } ?: emptyList()

        val sceneCharacterIds = charactersInScene.map { it.data.id }.toSet()
        val externalCharacters =
            saga.getCharacters(true).filter { it.id !in sceneCharacterIds }

        val argsMap =
            mutableMapOf(
                "sceneContext" to (sceneSummary?.toAINormalize() ?: "No context founded"),
                "charactersPresent" to
                    CharacterPrompts.sceneCharactersContext(
                        characters = charactersInScene,
                        arcsByCharacterId = characterArcsById,
                        protagonist = saga.mainCharacter,
                    ),
                "relationshipsBlock" to "",
                "externalCharacters" to CharacterPrompts.offSceneCharacterNames(externalCharacters),
                "conversationHistory" to conversationHistory(updateLimit, saga),
                "actDirective" to
                    promptService.buildRemotePrompt(
                        saga.getDirectiveKey(),
                        emptyMap(),
                    ),
                "sagaMainContext" to SagaPrompts.mainContext(saga),
                "conversationDirective" to conversationDirective,
                "latestMessage" to message.toAINormalize(messageExclusions),
                "genreConversationSoul" to conversationDirective,
                "characterResolutionProtocol" to characterResolutionProtocol(),
                "reactionProtocol" to
                    buildString {
                        appendLine("### 🎭 EMOTIONAL REACTION PROTOCOL 🎭")
                        appendLine(
                            "Along with the message, you MUST generate emotional reactions for ALL other characters present in the scene.",
                        )
                        if (charactersInScene.isNotEmpty()) {
                            appendLine("Characters present to react: ${charactersInScene.joinToString { it.data.name }}")
                            appendLine("Each character reaction must contain their NAME, an Emoji, and a hidden Thought.")
                        } else {
                            appendLine(
                                "No specific characters present to react, but you can still generate reactions for the narrator if applicable.",
                            )
                        }
                    },
            )

        return promptService.buildRemotePrompt(REPLY_GENERATION_BLUEPRINT, argsMap)
    }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun checkForTypo(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
        updateLimit: Int,
        message: String,
    ): String {
        val recentContext =
            conversationHistory(
                updateLimit,
                saga,
                1,
            )

        val args =
            TypoFixArgs(
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                genreName = saga.data.genre.name,
                conversationDirective = conversationDirective,
                recentContext = recentContext,
                message = message,
            )

        return promptService.buildRemotePrompt(CHAT_WRITING_PAL_BLUEPRINT, args)
    }

    suspend fun generateReactionPrompt(
        promptService: PromptService,
        summary: SceneSummary,
        saga: SagaContent,
        messageToReact: Message,
        conversationDirective: String,
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
                conversationDirective = conversationDirective,
                genreName = saga.data.genre.name,
            )

        return promptService.buildRemotePrompt(CHAT_REACTION_BLUEPRINT, args)
    }

    suspend fun sceneSummarizationPrompt(
        promptService: PromptService,
        saga: SagaContent,
        rules: NarrativeRules,
    ): String {
        val latestMessage = saga.flatMessages().maxByOrNull { it.message.timestamp }?.message
        val latestMessageContent = latestMessage?.toAINormalize(messageExclusions) ?: ""

        val args =
            SceneSummaryArgs(
                sagaContext = SagaPrompts.mainContext(saga),
                recentActivity = saga.historySummary(),
                conversationHistory = conversationHistory(rules.loreUpdateLimit, saga),
                latestMessage = latestMessageContent,
            )

        return promptService.buildRemotePrompt(SCENE_SUMMARIZATION_BLUEPRINT, args)
    }

    suspend fun scheduledNotificationPrompt(
        promptService: PromptService,
        saga: SagaContent,
        selectedCharacter: CharacterContent,
        sceneSummary: SceneSummary,
        conversationDirective: String,
    ): String {
        val relationWithCharacter = selectedCharacter.findRelationship(saga.mainCharacter!!.data.id)
        val relationshipBlock = relationWithCharacter?.summarizeRelation(1) ?: ""

        val args =
            NotificationArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                sceneSummaryContent = sceneSummary.toAINormalize(),
                characterContext = selectedCharacter.data.toAINormalize(CHARACTER_EXCLUSIONS),
                relationshipBlock = relationshipBlock,
                conversationHistory = conversationHistory(10, saga),
                characterName = selectedCharacter.data.name,
                sagaMainCharName = saga.mainCharacter.data.name,
                conversationDirective = conversationDirective,
            )

        return promptService.buildRemotePrompt(CHAT_NOTIFICATION_BLUEPRINT, args)
    }

    fun conversationHistory(
        loreUpdateLimit: Int,
        saga: SagaContent,
        threshold: Int = loreUpdateLimit,
    ) = buildString {
        val currentTimeline = saga.getCurrentTimeLine()
        val currentMessages =
            currentTimeline?.let {
                if (it.messages.size >= loreUpdateLimit / 2) {
                    it.messages.map { it.message }.sortedBy { it.timestamp }
                } else {
                    saga.flatMessages().map { it.message }.sortedBy { it.timestamp }
                }
            } ?: run {
                saga.flatMessages().map { it.message }.sortedBy { it.timestamp }
            }
        appendLine(
            currentMessages
                .takeLast(threshold)
                .normalizetoAIItems(excludingFields = messageExclusions),
        )
    }
}
