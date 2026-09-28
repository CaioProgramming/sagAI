package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.prompts.ChatPrompts.CHAT_REACTION_BLUEPRINT
import com.ilustris.sagai.core.ai.prompts.ChatPrompts.REPLY_GENERATION_BLUEPRINT
import com.ilustris.sagai.core.ai.prompts.ChatPrompts.SCENE_SUMMARIZATION_BLUEPRINT
import com.ilustris.sagai.core.ai.rag.EmbeddingSourceType
import com.ilustris.sagai.core.ai.rag.RetrievalGroup
import com.ilustris.sagai.core.ai.rag.SemanticMatch
import com.ilustris.sagai.core.ai.rag.SemanticRetrievalService
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
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.narrative.domain.buildChatContinuityContext
import com.ilustris.sagai.features.saga.chat.data.model.InputMode
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary

data class TypoFixArgs(
    val sagaMainContext: String,
    val genreName: String,
    val conversationDirective: String,
    val recentContext: String,
    val message: String,
)

object ChatPrompts {
    const val CHAT_REACTION_BLUEPRINT = "chat_reaction_blueprint"
    const val CHAT_WRITING_PAL_BLUEPRINT = "chat_writing_pal_blueprint"
    const val REPLY_GENERATION_BLUEPRINT = "reply_generation_blueprint"
    const val SCENE_SUMMARIZATION_BLUEPRINT = "scene_summarization_blueprint"

    /**
     * Reactions and the notification hook, resolved after the reply is already on screen. Runs on a
     * cheaper tier — which is a different model, and therefore a different per-minute token quota
     * than the one the reply spends from.
     */
    const val REPLY_FALLOUT_BLUEPRINT = "reply_fallout_blueprint"

    /**
     * Rules for returning the player's corrected message (`AIReply.playerInput`). Not a prompt of
     * its own: its `instructions` hold a shared bucket plus one per [InputMode], merged into the
     * reply prompt by [playerInputInstructions].
     */
    const val PLAYER_INPUT_BLUEPRINT = "player_input_blueprint"
    private const val PLAYER_INPUT_SHARED_BUCKET = "PLAYER INPUT"

    /** Optional ceiling for voice-turn replies: shorter lines are faster to voice and easier to follow by ear. */
    const val LIVE_REPLY_LIMIT_KEY = "live_reply_limit"

    /**
     * The shared `PLAYER INPUT` bucket plus only the one for [mode], so a typed turn doesn't carry
     * the voice formatting rules. Empty when the blueprint isn't published yet — the reply still
     * works, it just won't correct the player's text.
     */
    fun playerInputInstructions(
        blueprint: PromptBlueprint?,
        mode: InputMode,
    ): Map<String, Any> {
        val buckets = blueprint?.instructions ?: return emptyMap()
        return buildMap {
            buckets[PLAYER_INPUT_SHARED_BUCKET]?.let { put(PLAYER_INPUT_SHARED_BUCKET, it) }
            val modeKey = "$PLAYER_INPUT_SHARED_BUCKET (${mode.name})"
            buckets[modeKey]?.let { put(modeKey, it) }
        }
    }

    /**
     * Character ceiling for a single chat message, shared by the composer and the AI reply so both
     * sides of the conversation obey the same limit. Measured on clean text — expressive tags
     * (`<action>`, `<think>`, `<narrator>`) don't count against it, matching `getCleanTextLength`.
     */
    const val CHAT_INPUT_LIMIT_KEY = "chat_input_limit"
    const val DEFAULT_CHAT_INPUT_LIMIT = 2000

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
     *   Also receives `maxMessageLimit`: the character ceiling for `message.text`, counted on prose
     *   only (expressive tags are markup and don't count). The blueprint must compose within it
     *   rather than write long and cut — a message ending mid-sentence or mid-tag is a failure.
     *   THIS is the blueprint that keeps [com.ilustris.sagai.features.saga.chat.data.model.SceneSummary]
     *   fresh in practice — it emits a new `sceneSummary` (via
     *   [com.ilustris.sagai.features.saga.chat.data.model.AIReply]) on every single reply, so its
     *   `sceneSummary.charactersPresent` must follow the same `{name, brief}` shape and brief-writing
     *   rules described under [SCENE_SUMMARIZATION_BLUEPRINT] below.
     *
     * - [SCENE_SUMMARIZATION_BLUEPRINT]: bootstrap-only fallback — only runs when a timeline has no
     *   scene summary yet (see `shouldEnsureSceneSummary`/`getSceneContext`), not on every turn.
     *   `sagaContext.narrativeContinuity` must inform scene facts without overwriting long-range
     *   canon. `charactersPresent` is a list of `{name, brief}`, not plain names: `name` must match
     *   a story character's display name, and `brief` is one or two self-contained sentences
     *   (identity/origin, current role, and stake in this scene — threats, goals, who they're at
     *   odds with) written so reply/reaction generation can place the character without re-deriving
     *   it from raw profile, relationship and arc data. Keep it tight — this rides along on every
     *   turn once it exists, so a bloated brief defeats the point. Since [REPLY_GENERATION_BLUEPRINT]
     *   is what actually keeps this fresh turn to turn, prioritize refining that blueprint's
     *   `sceneSummary` output first; this one only needs to get the *first* one right.
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
            // Read-receipt state for the UI. The model has no use for it and it rode along on
            // every message in the history.
            "viewed",
            // Live-mode bookkeeping. The latest player turn gets its inputMode explicitly in the
            // reply prompt; history doesn't need either field.
            "inputMode",
            "originalText",
        )

    /**
     * Applied to the *output* schema only, never to the context we send in.
     *
     * `emotionalTone` has to stay in the input: the history showing that the player was DETERMINED
     * three turns ago is real signal. The notification fields are the opposite — they are described
     * to the model purely so it can fill them, and now that [REPLY_FALLOUT_BLUEPRINT] owns them,
     * describing them here would be asking twice for the same thing.
     */
    val messageOutputExclusions =
        messageExclusions +
            listOf(
                "notificationHook",
                "notificationCharacterName",
            )

    val sagaExclusions =
        listOf(
            "id",
            "icon",
            "artwork",
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
            "events",
            "relationships",
            "artwork",
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
        maxMessageLimit: Int = DEFAULT_CHAT_INPUT_LIMIT,
        /**
         * Null when RAG isn't wired at a call site (tests, other callers) — every use below has a
         * recency- or string-based fallback, so this stays fully optional.
         */
        semanticRetrievalService: SemanticRetrievalService? = null,
    ): SplitPrompt {
        val charactersInScene =
            sceneSummary?.charactersPresent?.mapNotNull {
                saga.findCharacter(it.name)
            } ?: emptyList()

        val messageSender = saga.findCharacter(message.speakerName)

        // One embedding call for message.text, shared by both groups below — mentionedWikis and
        // relevantMemories used to each call search() independently and embed the same player
        // message twice per chat turn. Retrieval, not indexing, is RAG's per-message cost surface,
        // so that redundant call mattered.
        val (semanticWikiMatches, relevantMemories) =
            semanticRetrievalService
                ?.searchGroups(
                    sagaId = saga.data.id,
                    query = message.text,
                    groups =
                        listOf(
                            RetrievalGroup(listOf(EmbeddingSourceType.WIKI), topK = 3),
                            RetrievalGroup(
                                listOf(EmbeddingSourceType.CHARACTER_EVENT, EmbeddingSourceType.CONTINUITY_FACT),
                                topK = 5,
                            ),
                        ),
                )?.let { (wikis, memories) -> wikis to memories }
                ?: (emptyList<SemanticMatch>() to emptyList<SemanticMatch>())

        // Wiki titles/content are rarely quoted verbatim by the player, so the old .contains()
        // filter mostly missed — it stays only as a fallback for when RAG has nothing indexed yet
        // (a fresh saga, or the key/flag being unavailable) rather than the primary path.
        val mentionedWikis =
            semanticWikiMatches
                .mapNotNull { match -> match.sourceKey.removePrefix("wiki:").toIntOrNull() }
                .mapNotNull { wikiId -> saga.wikis.find { it.id == wikiId } }
                .ifEmpty {
                    // A voice turn reaches here with no text (the model hears the audio), and
                    // "".contains matches every wiki — so no text, no fallback mentions.
                    message.text.takeIf { it.isNotBlank() }?.let { text ->
                        saga.wikis.filter {
                            it.title.contains(text, ignoreCase = true) ||
                                it.content.contains(text, ignoreCase = true)
                        }
                    }.orEmpty()
                }

        // relevantMemories: facts and character-history beats pulled by relevance to [message],
        // not by recency. `LatestCharacterEvents` below and the continuity rollups baked into
        // `narrativeContinuity` are both cut by "most recent N" — a callback to something
        // established many chapters back falls out of both. This is the section that actually
        // catches that case: it searches the *whole* saga's indexed events and continuity facts
        // and surfaces whatever is semantically closest to what the player just said, however old.

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

                put(
                    "storyCharacters",
                    saga.characters.joinToString { "${it.data.fullName()} - ${it.data.profile.occupation}" },
                )

                sceneSummary?.charactersPresent?.takeIf { it.isNotEmpty() }?.let {
                    put("charactersPresent", it.normalizetoAIItems())
                }

                messageSender?.let {
                    put(
                        "messageSender",
                        buildMap {
                            putAll(messageSender.data.asMap())
                            messageSender.data.details.physicalTraits.age
                                .takeIf { age -> age > 0 }
                                ?.let { age -> put("age", age) }
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
                                    .filter { inScene -> inScene.data.id != it.data.id }
                                    .mapNotNull { inScene ->
                                        messageSender
                                            .findRelationship(inScene.data.id)
                                            ?.summarizeRelation()
                                    }.distinct()
                                    .normalizetoAIItems(),
                            )
                        }.toAINormalize(CHARACTER_EXCLUSIONS),
                    )
                }

                if (mentionedWikis.isNotEmpty()) {
                    put("mentionedWikis", mentionedWikis.normalizetoAIItems())
                }

                if (relevantMemories.isNotEmpty()) {
                    put("relevantMemories", relevantMemories.map { it.text }.normalizetoAIItems())
                }
            }

        val argsMap =
            mutableMapOf(
                "worldContext" to worldContext,
                "conversationHistory" to
                    conversationHistory(updateLimit, saga, excludingMessageId = message.id),
                // inputMode tells the model which PLAYER INPUT bucket applies (typed fixes vs. voice
                // formatting); it's excluded from history but kept on the latest turn.
                "latestMessage" to
                    message
                        .copy(inputMode = message.inputMode ?: InputMode.TYPED)
                        .toAINormalize(messageExclusions - "inputMode"),
                "maxMessageLimit" to maxMessageLimit,
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

    /**
     * Prompt for [REPLY_FALLOUT_BLUEPRINT]: the reactions and the notification hook for a turn that
     * has already been written and persisted.
     *
     * Deliberately narrower than [replyMessagePrompt] — no continuity layers, no distant canon, no
     * conversation history beyond the two messages being reacted to. What it does carry is per
     * character stake, sourced from the scene summary's [CharacterPresence.brief] rather than raw
     * profile fields, because without that the reactions come back interchangeable, which is the
     * failure REACTION_NOT_TRANSFERABLE exists to catch. [playerCompass] rides along too, thin as it
     * is: this is the only live path that voices *other* characters' reactions to a turn, and without
     * it a bystander's reaction has no way to know the player is mid-vulnerability rather than mid-plot.
     */
    suspend fun replyFalloutPrompt(
        promptService: PromptService,
        userMessage: Message,
        replyMessage: Message,
        sceneSummary: SceneSummary?,
        playerCompass: String? = null,
    ): SplitPrompt {
        val speakerNames =
            listOfNotNull(userMessage.speakerName, replyMessage.speakerName)
                .map { it.trim().lowercase() }
                .toSet()

        // Whoever just spoke doesn't react to themselves, so they don't need a stake block either.
        val castWithStake =
            sceneSummary
                ?.charactersPresent
                ?.filterNot { it.name.trim().lowercase() in speakerNames }
                ?.joinToString("\n") { "${it.name} — ${it.brief}" }
                .orEmpty()

        return promptService.buildSplitBlueprint(
            REPLY_FALLOUT_BLUEPRINT,
            mapOf(
                "sceneContext" to (sceneSummary?.toAINormalize().orEmpty()),
                "reactingCast" to castWithStake,
                "playerMessage" to userMessage.toAINormalize(messageExclusions),
                "characterReply" to replyMessage.toAINormalize(messageExclusions),
                "playerCompass" to playerCompass.orEmpty(),
            ),
        )
    }

    suspend fun generateReactionPrompt(
        promptService: PromptService,
        summary: SceneSummary,
        saga: SagaContent,
        messageToReact: Message,
        narrativeRules: NarrativeRules,
    ): SplitPrompt {
        val messageSender = saga.findCharacter(messageToReact.speakerName)
        val charactersInScene =
            summary.charactersPresent.mapNotNull {
                saga.findCharacter(it.name)
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
                                    messageSender.data.details.physicalTraits.age
                                        .takeIf { age -> age > 0 }
                                        ?.let { age -> put("age", age) }
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
                    it.data.details.physicalTraits.age
                        .takeIf { age -> age > 0 }
                        ?.let { age -> put("mainCharacterAge", age) }
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
        excludingMessageId: Int? = null,
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
                .filterNot { excludingMessageId != null && it.id == excludingMessageId }
                .takeLast(threshold)
                .normalizetoAIItems(excludingFields = messageExclusions),
        )
    }
}
