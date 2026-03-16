package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.ChatPrompts.messageExclusions
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.saga.chat.data.model.Message
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
    val appearanceGuidelines: String,
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
    suspend fun conversationalCharacterReply(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        currentCharacterInfo: CharacterInfo,
        userInput: String,
        conversationHistory: List<com.ilustris.sagai.features.newsaga.data.model.ChatMessage>,
        sagaContext: SagaDraft?,
    ): String {
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

        return promptService.buildRemotePrompt("character_reply_prompt", args)
    }

    suspend fun characterIntroPrompt(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        sagaContext: SagaDraft?,
    ): String {
        val args =
            CharacterIntroArgs(
                sagaWorld = sagaContext?.description ?: "",
                sagaGenre = sagaContext?.genre?.name ?: "FANTASY",
                genreName = sagaContext?.genre?.name ?: "FANTASY",
            )

        return promptService.buildRemotePrompt("character_intro_prompt", args)
    }

    suspend fun characterAdaptationPrompt(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        currentCharacterInfo: CharacterInfo,
        newGenre: String,
    ): String {
        val args =
            CharacterAdaptationArgs(
                newGenre = newGenre,
                currentCharacterDraft = currentCharacterInfo.toAINormalize(),
            )

        return promptService.buildRemotePrompt("character_adaptation_prompt", args)
    }

    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    fun charactersOverview(characters: List<com.ilustris.sagai.features.characters.data.model.Character>): String =
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
                    "smartZoom",
                )
            appendLine("CURRENT SAGA CAST OVERVIEW:")
            characters.forEach { character ->
                appendLine(character.name)
                appendLine(character.toAINormalize(characterExclusions))
            }
        }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun characterGeneration(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        saga: SagaContent,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
        description: String,
        bannedNames: List<String> = emptyList(),
        themeColor: String? = null,
    ): String {
        val themeColorContext =
            themeColor?.let {
                buildString {
                    appendLine("## 🎨 CHARACTER THEME COLOR: $it 🎨")
                    appendLine("This character has a signature theme color that should influence their visual identity.")
                    appendLine("**USE THIS COLOR AS A GUIDE** for hair, eyes, outfit accents, or accessories.")
                }
            } ?: ""

        val bannedNamesContext =
            if (bannedNames.isNotEmpty()) {
                buildString {
                    appendLine("## 🚫 BANNED NAMES (CREATIVITY CHALLENGE) 🚫")
                    appendLine(
                        "Avoid these names unless explicitly requested in the discovery seed: ${
                            bannedNames.joinToString(
                                ", ",
                            )
                        }",
                    )
                }
            } else {
                ""
            }

        val args =
            CharacterGenerationArgs(
                sagaMainContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                themeColorContext = themeColorContext,
                discoverySeed = description,
                bannedNamesContext = bannedNamesContext,
                conversationHistory =
                    saga
                        .flatMessages()
                        .sortedByDescending { it.message.timestamp }
                        .take(5)
                        .map { it.message }
                        .normalizetoAIItems(excludingFields = messageExclusions),
                appearanceGuidelines = config.appearanceGuidelines,
            )

        return promptService.buildRemotePrompt("character_generation_prompt", args)
    }

    suspend fun characterLoreGeneration(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        timeline: Timeline,
        characters: List<com.ilustris.sagai.features.characters.data.model.Character>,
    ): String {
        val args =
            CharacterLoreArgs(
                timelineContext =
                    timeline.toAINormalize(
                        listOf("id", "emotionalReview", "chapterId"),
                    ),
                charactersContext =
                    characters.toAINormalize(
                        fieldsToExclude = ChatPrompts.characterExclusions,
                    ),
            )

        return promptService.buildRemotePrompt("character_lore_prompt", args)
    }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun findNickNames(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        characters: List<com.ilustris.sagai.features.characters.data.model.Character>,
        messages: List<Message>,
        timeline: Timeline,
        saga: Saga,
    ): String {
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
                charactersList = characters.normalizetoAIItems(ChatPrompts.characterExclusions),
                recentMessages = messages.normalizetoAIItems(messageExclusions),
            )

        return promptService.buildRemotePrompt("character_nicknames_prompt", args)
    }

    suspend fun generateCharacterRelation(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        timeline: Timeline,
        saga: SagaContent,
    ): String {
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
                        ChatPrompts.characterExclusions,
                    ),
            )

        return promptService.buildRemotePrompt("character_relation_prompt", args)
    }

    suspend fun characterResume(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        promptDirectives: PromptDirectives,
        character: CharacterContent,
        saga: SagaContent,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
    ): String {
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
                    Personality: ${characterData.profile.personality}
                    Visual Profile: ${characterData.details.physicalTraits.ethnicity} ${characterData.details.physicalTraits.gender}, ${characterData.details.physicalTraits.race}. ${characterData.details.physicalTraits.facialDetails.hair} hair, ${characterData.details.physicalTraits.facialDetails.eyes} eyes. ${characterData.details.physicalTraits.bodyFeatures.buildAndPosture}.
                    Style: ${characterData.details.clothing.outfitDescription}
                    """.trimIndent(),
                journeyEvents = journeyEvents,
                relationships = relationshipsBlock,
                toneStyle = config.conversationDirective,
            )

        return promptService.buildRemotePrompt("character_resume_prompt", args)
    }

    suspend fun knowledgeUpdatePrompt(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        event: Timeline,
        characters: List<com.ilustris.sagai.features.characters.data.model.Character>,
    ): String {
        val args =
            KnowledgeUpdateArgs(
                eventContext = event.toAINormalize(listOf("id", "chapterId")),
                charactersContext = characters.normalizetoAIItems(ChatPrompts.characterExclusions),
            )

        return promptService.buildRemotePrompt("knowledge_update_prompt", args)
    }

    suspend fun refineCharacterDraftPrompt(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        rawInput: String,
        sagaContext: SagaDraft?,
    ): String {
        val args =
            RefineDraftArgs(
                userInput = rawInput,
                sagaContext = sagaContext?.toAINormalize() ?: "",
            )

        return promptService.buildRemotePrompt("refine_character_draft_prompt", args)
    }
}
