package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.prompts.ChatPrompts.messageExclusions
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.model.Timeline

data class CharacterReplyArgs(
    val currentCharacterState: String,
    val sagaContext: String,
    val conversationHistory: String,
    val userInput: String,
)

data class CharacterIntroArgs(
    val sagaWorld: String,
    val sagaGenre: String,
    val genreName: String,
)

data class CharacterAdaptationArgs(
    val newGenre: String,
    val currentCharacterDraft: String,
)

data class CharacterGenerationArgs(
    val sagaMainContext: String,
    val themeColorContext: String,
    val discoverySeed: String,
    val bannedNamesContext: String,
    val conversationHistory: String,
    val sceneContext: String = "",
)

data class CharacterLoreArgs(
    val timelineContext: String,
    val charactersContext: String,
)

data class CharacterNicknamesArgs(
    val sagaContext: String,
    val timelineContext: String,
    val charactersList: String,
    val recentMessages: String,
)

data class CharacterRelationArgs(
    val timelineEvent: String,
    val charactersList: String,
)

data class CharacterResumeArgs(
    val sagaContext: String,
    val characterIdentity: String,
    val journeyEvents: String,
    val relationships: String,
    val toneStyle: String,
)

data class KnowledgeUpdateArgs(
    val eventContext: String,
    val charactersContext: String,
)

data class RefineDraftArgs(
    val userInput: String,
    val sagaContext: String,
)

@Suppress("ktlint:standard:max-line-length")
object CharacterPrompts {
    val ARCS_EXCLUSIONS =
        listOf(
            "id",
            "characterId",
            "createdAt",
            "sourceId",
            "sourceType",
        )
    const val CHARACTER_ADAPTATION_BLUEPRINT = "character_adaptation_blueprint"
    const val CHARACTER_GENERATION_BLUEPRINT = "character_generation_blueprint"
    const val CHARACTER_INTRO_BLUEPRINT = "character_intro_blueprint"
    const val CHARACTER_LORE_BLUEPRINT = "character_lore_blueprint"
    const val CHARACTER_NICKNAME_BLUEPRINT = "character_nickname_blueprint"
    const val CHARACTER_RELATION_BLUEPRINT = "character_relation_blueprint"
    const val CHARACTER_RESUME_BLUEPRINT = "character_resume_blueprint"
    const val CONVERSATIONAL_CHARACTER_REPLY_BLUEPRINT = "conversational_character_reply_blueprint"
    const val KNOWLEDGE_UPDATE_BLUEPRINT = "knowledge_update_blueprint"
    const val REFINE_CHARACTER_DRAFT_BLUEPRINT = "refine_character_draft_blueprint"
    const val CHARACTER_ENRICHMENT_BLUEPRINT = "character_enrichment_blueprint"

    suspend fun conversationalCharacterReply(
        promptService: PromptService,
        currentCharacterInfo: CharacterInfo,
        userInput: String,
        conversationHistory: List<com.ilustris.sagai.features.newsaga.data.model.ChatMessage>,
        sagaContext: SagaDraft?,
    ): SplitPrompt {
        val args =
            CharacterReplyArgs(
                currentCharacterState = currentCharacterInfo.toAINormalize(),
                sagaContext = sagaContext?.toAINormalize() ?: "",
                conversationHistory =
                    conversationHistory.takeLast(10).joinToString("\n") { msg ->
                        "${msg.sender.name}: ${msg.text}"
                    },
                userInput = userInput,
            )

        return promptService.buildSplitBlueprint(CONVERSATIONAL_CHARACTER_REPLY_BLUEPRINT, args)
    }

    suspend fun characterIntroPrompt(
        promptService: PromptService,
        sagaContext: SagaDraft?,
    ): SplitPrompt {
        val args =
            CharacterIntroArgs(
                sagaWorld = sagaContext?.description ?: "",
                sagaGenre = sagaContext?.genre?.name ?: "FANTASY",
                genreName = sagaContext?.genre?.name ?: "FANTASY",
            )

        return promptService.buildSplitBlueprint(CHARACTER_INTRO_BLUEPRINT, args)
    }

    suspend fun characterAdaptationPrompt(
        promptService: PromptService,
        currentCharacterInfo: CharacterInfo,
        newGenre: String,
    ): SplitPrompt {
        val args =
            CharacterAdaptationArgs(
                newGenre = newGenre,
                currentCharacterDraft = currentCharacterInfo.toAINormalize(),
            )

        return promptService.buildSplitBlueprint(CHARACTER_ADAPTATION_BLUEPRINT, args)
    }

    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    fun charactersOverview(characters: List<Character>): String =
        buildString {
            val characterExclusions =
                listOf(
                    "id",
                    "image",
                    "sagaId",
                    "joinedAt",
                    "details",
                    "events",
                    "relationshipEvents",
                    "relationshipsAsFirst",
                    "relationshipsAsSecond",
                    "physicalTraits",
                    "hexColor",
                    "firstSceneId",
                    "emojified",
                    "artwork",
                )
            appendLine("CURRENT SAGA CAST OVERVIEW:")
            characters.forEach { character ->
                appendLine(character.name)
                appendLine(character.toAINormalize(characterExclusions))
            }
        }

    const val SCENE_KNOWLEDGE_LIMIT = 6
    const val SCENE_ARC_LIMIT = 2
    const val SCENE_ARC_CONTENT_MAX_CHARS = 400
    const val SCENE_RELATION_THRESHOLD = 2

    fun offSceneCharacterNames(characters: List<Character>): String =
        if (characters.isEmpty()) {
            "No other characters in this saga."
        } else {
            characters.joinToString(", ") { it.fullName() }
        }

    fun sceneCharacterContext(
        character: CharacterContent,
        arcs: List<CharacterArc> = emptyList(),
        protagonist: CharacterContent? = null,
        knowledgeLimit: Int = SCENE_KNOWLEDGE_LIMIT,
        arcLimit: Int = SCENE_ARC_LIMIT,
        relationThreshold: Int = SCENE_RELATION_THRESHOLD,
    ): String =
        buildString {
            val data = character.data
            appendLine("### ${data.fullName()}")
            if (data.profile.occupation.isNotBlank()) {
                appendLine("Role: ${data.profile.occupation}")
            }
            if (data.profile.personality.isNotBlank()) {
                appendLine("Personality: ${data.profile.personality}")
            }
            data.knowledge
                ?.filter { it.isNotBlank() }
                ?.takeLast(knowledgeLimit)
                ?.takeIf { it.isNotEmpty() }
                ?.let { facts ->
                    appendLine("Known facts:")
                    facts.forEach { appendLine("  - $it") }
                }
            arcs
                .takeLast(arcLimit)
                .forEach { arc ->
                    val excerpt = arc.content.take(SCENE_ARC_CONTENT_MAX_CHARS)
                    val suffix = if (arc.content.length > SCENE_ARC_CONTENT_MAX_CHARS) "..." else ""
                    appendLine("Story beat [${arc.title}]: $excerpt$suffix")
                }
            protagonist
                ?.takeIf { it.data.id != data.id }
                ?.let { main ->
                    character.findRelationship(main.data.id)?.let { relation ->
                        appendLine(relation.summarizeRelation(relationThreshold))
                    }
                }
        }

    fun sceneCharactersContext(
        characters: List<CharacterContent>,
        arcsByCharacterId: Map<Int, List<CharacterArc>> = emptyMap(),
        protagonist: CharacterContent? = null,
    ): String =
        if (characters.isEmpty()) {
            "No characters identified in the current scene."
        } else {
            characters.joinToString("\n\n") { character ->
                sceneCharacterContext(
                    character = character,
                    arcs = arcsByCharacterId[character.data.id].orEmpty(),
                    protagonist = protagonist,
                )
            }
        }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun characterGeneration(
        promptService: PromptService,
        saga: SagaContent,
        description: String,
        themeColor: String? = null,
        sceneSummary: SceneSummary? = null,
        aesthetic: String? = null,
    ): SplitPrompt {
        val latestMessages =
            if (saga.flatMessages().isEmpty()) {
                emptyList()
            } else {
                saga
                    .flatMessages()
                    .sortedByDescending { it.message.timestamp }
                    .take(5)
            }

        return promptService.buildSplitBlueprint(
            CHARACTER_GENERATION_BLUEPRINT,
            mapOf(
                "context" to
                    buildMap {
                        put(
                            "SagaContext",
                            saga.data
                                .toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS)
                                .replace(saga.data.genre.name, aesthetic ?: saga.data.genre.name),
                        )
                        put(
                            "CharactersCast",
                            saga.characters.joinToString { it.data.fullName() },
                        )
                        sceneSummary?.let {
                            put("CurrentStoryState", it.toAINormalize())
                        }
                        if (latestMessages.isNotEmpty()) {
                            put(
                                "LatestMessages",
                                latestMessages
                                    .map { it.message }
                                    .normalizetoAIItems(excludingFields = messageExclusions),
                            )
                        }
                        put("NewCharacterContext", description)
                        put("NewCharacterFavoriteColor", themeColor)
                    },
            ),
        )
    }

    suspend fun characterLoreGeneration(
        promptService: PromptService,
        timeline: Timeline,
        characters: List<Character>,
    ): SplitPrompt {
        val args =
            CharacterLoreArgs(
                timelineContext =
                    timeline.toAINormalize(
                        listOf("id", "emotionalReview", "chapterId"),
                    ),
                charactersContext =
                    characters.toAINormalize(
                        fieldsToExclude = ChatPrompts.CHARACTER_EXCLUSIONS,
                    ),
            )

        return promptService.buildSplitBlueprint(CHARACTER_LORE_BLUEPRINT, args)
    }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun findNickNames(
        promptService: PromptService,
        characters: List<Character>,
        messages: List<Message>,
        timeline: Timeline,
        saga: Saga,
    ): SplitPrompt {
        val args =
            CharacterNicknamesArgs(
                sagaContext = saga.toAINormalize(ChatPrompts.sagaExclusions),
                timelineContext =
                    timeline.toAINormalize(
                        listOf(
                            "id",
                            "emotionalReview",
                            "chapterId",
                        ),
                    ),
                charactersList = characters.normalizetoAIItems(ChatPrompts.CHARACTER_EXCLUSIONS),
                recentMessages = messages.normalizetoAIItems(messageExclusions),
            )

        return promptService.buildSplitBlueprint(CHARACTER_NICKNAME_BLUEPRINT, args)
    }

    suspend fun generateCharacterRelation(
        promptService: PromptService,
        timeline: Timeline,
        saga: SagaContent,
    ): SplitPrompt {
        val args =
            CharacterRelationArgs(
                timelineEvent =
                    timeline.toAINormalize(
                        listOf(
                            "id",
                            "emotionalReview",
                            "chapterId",
                        ),
                    ),
                charactersList =
                    saga.getCharacters().toAINormalize(
                        ChatPrompts.CHARACTER_EXCLUSIONS,
                    ),
            )

        return promptService.buildSplitBlueprint(CHARACTER_RELATION_BLUEPRINT, args)
    }

    suspend fun characterResume(
        promptService: PromptService,
        promptDirectives: PromptDirectives,
        character: CharacterContent,
        saga: SagaContent,
        config: GenreConfig,
    ): SplitPrompt {
        val characterData = character.data
        val journeyEvents =
            if (character.events.isEmpty()) {
                promptDirectives.characterResumeNoEvents
            } else {
                character.events
                    .sortedByDescending { it.event.createdAt }
                    .take(15)
                    .joinToString("\n") { event ->
                        "- ${event.event.title}: ${event.event.summary}"
                    }
            }

        val relationshipsBlock =
            if (character.relationships.isEmpty()) {
                promptDirectives.characterResumeNoRelationships
            } else {
                character.relationships.joinToString("\n") { relation ->
                    val other = relation.getCharacterExcluding(character.data)
                    "- ${relation.data.title} with ${other.name} ${relation.data.emoji}: ${relation.data.description}"
                }
            }

        val args =
            CharacterResumeArgs(
                sagaContext = SagaPrompts.mainContext(saga, character),
                characterIdentity =
                    """
                    Name: ${characterData.name} ${characterData.lastName ?: ""}
                    Age: ${characterData.details.physicalTraits.age
                        .takeIf { it > 0 } ?: "Unknown"}
                    Personality: ${characterData.profile.personality}
                    Visual Profile: ${characterData.details.physicalTraits.ethnicity} ${characterData.details.physicalTraits.gender}, ${characterData.details.physicalTraits.race}. ${characterData.details.physicalTraits.facialDetails.hair} hair, ${characterData.details.physicalTraits.facialDetails.eyes} eyes. ${characterData.details.physicalTraits.bodyFeatures.buildAndPosture}.
                    Style: ${characterData.details.clothing.outfitDescription}
                    """.trimIndent(),
                journeyEvents = journeyEvents,
                relationships = relationshipsBlock,
                toneStyle = config.aesthetic,
            )

        return promptService.buildSplitBlueprint(CHARACTER_RESUME_BLUEPRINT, args)
    }

    suspend fun knowledgeUpdatePrompt(
        promptService: PromptService,
        event: Timeline,
        characters: List<Character>,
    ): SplitPrompt {
        val args =
            KnowledgeUpdateArgs(
                eventContext = event.toAINormalize(listOf("id", "chapterId")),
                charactersContext = characters.normalizetoAIItems(ChatPrompts.CHARACTER_EXCLUSIONS),
            )

        return promptService.buildSplitBlueprint(KNOWLEDGE_UPDATE_BLUEPRINT, args)
    }

    suspend fun refineCharacterDraftPrompt(
        promptService: PromptService,
        rawInput: String,
        sagaContext: SagaDraft?,
    ): SplitPrompt {
        val args =
            RefineDraftArgs(
                userInput = rawInput,
                sagaContext = sagaContext?.toAINormalize() ?: "",
            )

        return promptService.buildSplitBlueprint(REFINE_CHARACTER_DRAFT_BLUEPRINT, args)
    }

    suspend fun characterEnrichmentPrompt(
        promptService: PromptService,
        character: CharacterContent,
        saga: SagaContent,
        config: GenreConfig,
    ): SplitPrompt {
        val args =
            CharacterResumeArgs(
                sagaContext = SagaPrompts.mainContext(saga, character),
                characterIdentity = character.data.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS),
                journeyEvents =
                    character.events
                        .takeLast(10)
                        .joinToString("\n") { "- ${it.event.summary}" },
                relationships = character.summarizeRelationships(),
                toneStyle = config.aesthetic,
            )
        return promptService.buildSplitBlueprint(CHARACTER_ENRICHMENT_BLUEPRINT, args)
    }
}
