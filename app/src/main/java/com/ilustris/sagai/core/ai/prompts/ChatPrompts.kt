package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.model.historySummary
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
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
    ): SplitPrompt {
        val charactersInScene =
            sceneSummary?.charactersPresent?.mapNotNull {
                saga.findCharacter(it)
            } ?: emptyList()

        val sceneCharacterIds = charactersInScene.map { it.data.id }.toSet()
        val externalCharacters =
            saga.getCharacters(true).filter { it.id !in sceneCharacterIds }

        val messageSender = saga.findCharacter(message.speakerName)

        val mentionedWikis =
            saga.wikis.filter {
                it.title.contains(message.text, ignoreCase = true) ||
                    it.content.contains(message.text, ignoreCase = true)
            }

        val worldContext =
            mapOf(
                "worldContext" to
                    buildMap {
                        put(
                            "sagaContext",
                            saga.data.asMap(),
                        )
                        sceneSummary?.let {
                            put("currentStoryContext", sceneSummary.asMap())
                        }

                        put(
                            "storyCharacters",
                            saga.characters.joinToString { "${it.data.fullName()} - ${it.data.profile.occupation}" },
                        )

                        messageSender?.let {
                            put(
                                "messageSender",
                                buildMap {
                                    putAll(messageSender.data.asMap())
                                    val storyArcs = characterArcsById[it.data.id]
                                    storyArcs?.let {
                                        put(
                                            "CharacterArcs",
                                            storyArcs.takeLast(3).normalizetoAIItems(),
                                        )
                                    }
                                    put(
                                        "LatestCharacterEvents",
                                        it.events
                                            .map {
                                                "${it.character.name} - ${it.event.title}\n${it.event.summary}"
                                            }.takeLast(3)
                                            .normalizetoAIItems(),
                                    )
                                    put(
                                        "relationshipsWithPresentCharacters",
                                        charactersInScene
                                            .mapNotNull {
                                                messageSender
                                                    .findRelationship(it.data.id)
                                                    ?.summarizeRelation()
                                            }.normalizetoAIItems(),
                                    )
                                }.toAINormalize(CHARACTER_EXCLUSIONS),
                            )
                        }

                        if (mentionedWikis.isNotEmpty()) {
                            put("mentionedWikis", mentionedWikis.normalizetoAIItems())
                        }
                    }.toAINormalize(
                        buildList {
                            addAll(SagaPrompts.SAGA_EXCLUDED_FIELDS)
                            addAll(CHARACTER_EXCLUSIONS)
                        },
                    ),
            )

        val argsMap =
            mutableMapOf(
                "worldContext" to worldContext,
                "externalCharacters" to CharacterPrompts.offSceneCharacterNames(externalCharacters),
                "conversationHistory" to conversationHistory(updateLimit, saga),
                "latestMessage" to message.toAINormalize(messageExclusions),
                "userToneProtocol" to
                    """
                    Analyze the 'latestMessage' from the user and extract its EmotionalTone.
                    Valid tones: ${EmotionalTone.entries.joinToString { it.name }}.
                    Return the tone in the 'userTone' field.
                    """.trimIndent(),
                "userReactionProtocol" to
                    """
                    Based on the 'latestMessage' from the user and the 'sceneSummary', generate reactions from characters present in the scene.
                    Characters should react to what the user just said/did.
                    Return these in the 'userReactions' field as a list of AIReaction objects { "character": "Name", "reaction": "Emoji", "thought": "Brief thought" }.
                    Exclude the message sender from reacting to their own message.
                    """.trimIndent(),
            )

        return promptService
            .buildSplitBlueprint(REPLY_GENERATION_BLUEPRINT, argsMap)
    }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun checkForTypo(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
        updateLimit: Int,
        message: String,
    ): SplitPrompt {
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

        return promptService.buildSplitBlueprint(CHAT_WRITING_PAL_BLUEPRINT, args)
    }

    suspend fun generateReactionPrompt(
        promptService: PromptService,
        summary: SceneSummary,
        saga: SagaContent,
        messageToReact: Message,
        conversationDirective: String,
    ): SplitPrompt {
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

        val reactionArgs =
            ReactionArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                sceneSummary = summary.toAINormalize(),
                charactersPresent = summary.charactersPresent.joinToString(),
                messageToReact = messageToReact.text,
                relationshipsBlock = relationshipsBlock,
                conversationDirective = conversationDirective,
                genreName = saga.data.genre.name,
            )

        val messageSender = saga.findCharacter(messageToReact.speakerName)
        val charactersInScene =
            summary.charactersPresent.mapNotNull {
                saga.findCharacter(it)
            }

        val args =
            mapOf(
                "worldContext" to
                    buildMap {
                        put("sagaContext", saga.data)
                        summary.let {
                            put("currentStoryContext", summary.toAINormalize())
                        }

                        messageSender?.let {
                            put(
                                "messageSender",
                                buildMap {
                                    putAll(messageSender.data.asMap())
                                    put(
                                        "LatestCharacterEvents",
                                        it.events.takeLast(3).normalizetoAIItems(),
                                    )
                                    put(
                                        "relationshipsWithPresentCharacters",
                                        charactersInScene
                                            .mapNotNull {
                                                messageSender
                                                    .findRelationship(it.data.id)
                                                    ?.summarizeRelation()
                                            }.normalizetoAIItems(),
                                    )
                                },
                            )
                        }
                    }.toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS.plus(CHARACTER_EXCLUSIONS)),
            )

        return promptService.buildSplitBlueprint(CHAT_REACTION_BLUEPRINT, args.asMap())
    }

    suspend fun sceneSummarizationPrompt(
        promptService: PromptService,
        saga: SagaContent,
        rules: NarrativeRules,
    ): SplitPrompt {
        val latestMessage = saga.flatMessages().maxByOrNull { it.message.timestamp }?.message
        val latestMessageContent = latestMessage?.toAINormalize(messageExclusions) ?: ""

        val args =
            SceneSummaryArgs(
                sagaContext = SagaPrompts.mainContext(saga),
                recentActivity = saga.historySummary(),
                conversationHistory = conversationHistory(rules.loreUpdateLimit, saga),
                latestMessage = latestMessageContent,
            )

        return promptService.buildSplitBlueprint(SCENE_SUMMARIZATION_BLUEPRINT, args)
    }

    suspend fun scheduledNotificationPrompt(
        promptService: PromptService,
        saga: SagaContent,
        selectedCharacter: CharacterContent,
        sceneSummary: SceneSummary,
        conversationDirective: String,
    ): SplitPrompt {
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

        return promptService.buildSplitBlueprint(CHAT_NOTIFICATION_BLUEPRINT, args)
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
