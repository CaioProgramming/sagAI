package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.prompts.ChatPrompts.CHAT_REACTION_BLUEPRINT
import com.ilustris.sagai.core.ai.prompts.ChatPrompts.REPLY_GENERATION_BLUEPRINT
import com.ilustris.sagai.core.ai.prompts.ChatPrompts.SCENE_SUMMARIZATION_BLUEPRINT
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.narrative.domain.buildChatContinuityContext
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

object ChatPrompts {
    const val CHAT_REACTION_BLUEPRINT = "chat_reaction_blueprint"
    const val CHAT_WRITING_PAL_BLUEPRINT = "chat_writing_pal_blueprint"
    const val REPLY_GENERATION_BLUEPRINT = "reply_generation_blueprint"
    const val SCENE_SUMMARIZATION_BLUEPRINT = "scene_summarization_blueprint"

    /**
     * Remote Config blueprint expectations for hierarchical narrative memory:
     *
     * - [REPLY_GENERATION_BLUEPRINT]: `worldContext.narrativeContinuity` carries layered canon
     *   (currentChapterRollup, recentChapterCanon, distantCanon, actContinuity, globalWorldState).
     *   Never contradict `establishedFacts`; weave `openThreads` and `persistentSetups` subtly.
     *   Must also fill `sceneSummary.notificationHook`: a short, character-voiced line teasing
     *   what happens next, written as if the character is reaching out after the player stepped
     *   away (not mid-scene dialogue). Used verbatim as a push notification, so it must stand
     *   alone without any other scene context. Pair it with `sceneSummary.notificationCharacterName`
     *   (must match a name in `charactersPresent`, or be omitted for a narrator-voiced hook) so the
     *   app can attribute the correct avatar/name — never leave the hook set without it when a
     *   specific character is speaking.
     *
     * - [SCENE_SUMMARIZATION_BLUEPRINT]: `sagaContext.narrativeContinuity` must inform scene facts
     *   without overwriting long-range canon.
     *
     * - [CHAT_REACTION_BLUEPRINT]: same continuity block as reply generation for off-thread reactions.
     */

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
        updateLimit: Int,
        narrativeRules: NarrativeRules,
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

        val narrativeContinuity =
            saga.buildChatContinuityContext(narrativeRules).toAINormalize(
                buildList {
                    addAll(SagaPrompts.SAGA_EXCLUDED_FIELDS)
                    addAll(ChatPrompts.messageExclusions)
                    addAll(ChatPrompts.CHARACTER_EXCLUSIONS)
                    addAll(TimelinePrompts.timelineExclusions)
                    addAll(ActPrompts.ACT_EXCLUSIONS)
                    addAll(CharacterPrompts.ARCS_EXCLUSIONS)
                    addAll(SagaPrompts.summaryExclusions)
                },
            )

        val worldContext =
            buildMap {
                put(
                    "sagaContext",
                    saga.data.toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS),
                )

                if (narrativeContinuity.isNotEmpty()) {
                    put("narrativeContinuity", narrativeContinuity)
                }
                saga.data.worldState?.takeIf { it.isNotBlank() }?.let {
                    put("globalWorldState", it)
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
                                    storyArcs.takeLast(1).normalizetoAIItems(CharacterPrompts.ARCS_EXCLUSIONS),
                                )
                            }
                            put(
                                "LatestCharacterEvents",
                                it.events
                                    .map {
                                        "${it.character.name} - ${it.event.title}\n${it.event.summary}"
                                    }.takeLast(3)
                                    .normalizetoAIItems(CHARACTER_EXCLUSIONS),
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
            }

        val argsMap =
            mutableMapOf(
                "worldContext" to worldContext,
                "conversationHistory" to conversationHistory(updateLimit, saga),
                "latestMessage" to message.toAINormalize(messageExclusions),
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
        narrativeRules: NarrativeRules,
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

        val narrativeContinuity = saga.buildChatContinuityContext(narrativeRules).toContextMap()

        val args =
            mapOf(
                "worldContext" to
                    buildMap {
                        put("sagaContext", saga.data)
                        summary.let {
                            put("currentStoryContext", summary.toAINormalize())
                        }
                        if (narrativeContinuity.isNotEmpty()) {
                            put("narrativeContinuity", narrativeContinuity)
                        }
                        saga.data.worldState?.takeIf { it.isNotBlank() }?.let {
                            put("globalWorldState", it)
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
        val currentAct = saga.currentActInfo
        val currentChapter = saga.currentActInfo?.currentChapterInfo
        val lastEvent = saga.flatEvents().lastOrNull { it.data.id != currentChapter?.data?.id }
        val latestMessages = saga.flatMessages().takeLast(rules.loreUpdateLimit)
        val narrativeContinuity = saga.buildChatContinuityContext(rules).toContextMap()
        val storyContext =
            buildMap {
                put("sagaContext", saga.data.toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS))
                if (narrativeContinuity.isNotEmpty()) {
                    put("narrativeContinuity", narrativeContinuity)
                }
                saga.data.worldState?.takeIf { it.isNotBlank() }?.let {
                    put("globalWorldState", it)
                }
                saga.mainCharacter?.let {
                    put("mainCharacter", it.data.toAINormalize(CHARACTER_EXCLUSIONS))
                }
                put(
                    "storyCharacters",
                    saga.characters.joinToString { "${it.data.fullName()} - ${it.data.profile.occupation}\n${it.data.backstory}" },
                )
                currentAct?.let {
                    put("ActualArc", it.data.toAINormalize(ActPrompts.ACT_EXCLUSIONS))
                }
                currentChapter?.let {
                    put("ActualChapter", it.data.toAINormalize())
                }

                lastEvent?.let {
                    put(
                        "LastEvent",
                        it.data.toAINormalize(TimelinePrompts.timelineExclusions),
                    )
                }
                if (latestMessages.isNotEmpty()) {
                    put(
                        "LatestMessages",
                        latestMessages.map { it.message }.normalizetoAIItems(messageExclusions),
                    )
                }
            }.toAINormalize()

        return promptService.buildSplitBlueprint(
            SCENE_SUMMARIZATION_BLUEPRINT,
            mapOf(
                "sagaContext" to storyContext,
            ),
        )
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
