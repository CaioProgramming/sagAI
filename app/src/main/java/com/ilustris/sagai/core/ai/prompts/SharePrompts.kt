package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone

data class SharePlaystyleArgs(
    val characterPersonality: String,
    val emotionalRanking: String,
    val characterEvents: String,
    val sagaMainContext: String,
)

data class ShareEmotionalArgs(
    val emotionalReview: String,
    val historyEmotionalSummary: String,
    val characterArchetype: String,
)

data class ShareHistoryArgs(
    val sagaMainContext: String,
    val historyContext: String,
)

data class ShareRelationsArgs(
    val sagaMainContext: String,
    val relationshipContext: String,
)

data class ShareCharacterArgs(
    val characterName: String? = null,
    val sagaMainContext: String,
    val characterPersonality: String,
    val emotionalRanking: String,
    val characterEvents: String,
)

object SharePrompts {
    const val SHARE_CHARACTER_BLUEPRINT = "share_character_blueprint"
    const val SHARE_EMOTIONAL_BLUEPRINT = "share_emotional_blueprint"
    const val SHARE_HISTORY_BLUEPRINT = "share_history_blueprint"
    const val SHARE_PLAYSTYLE_BLUEPRINT = "share_playstyle_blueprint"
    const val SHARE_RELATIONS_BLUEPRINT = "share_relations_blueprint"

    val sagaExcludedFields =
        listOf(
            "id",
            "icon",
            "createdAt",
            "endedAt",
            "mainCharacterId",
            "currentActId",
            "isDebug",
            "emotionalReview",
            "topCharacters",
            "actsInsight",
            "conclusion",
            "introduction",
            "isEnded",
        )

    suspend fun playStylePrompt(
        promptService: PromptService,
        character: CharacterContent,
        sagaContent: SagaContent,
    ): String {
        val emotionalRanking =
            sagaContent
                .flatMessages()
                .filter { it.character?.id == character.data.id }
                .rankEmotionalTone()

        val args =
            SharePlaystyleArgs(
                characterPersonality = character.data.profile.toAINormalize(),
                emotionalRanking = emotionalRanking.joinToString("\n") { "${it.first.name} - ${it.second.size} messages" },
                characterEvents =
                    character.events.map { it.event }.normalizetoAIItems(
                        listOf("id", "characterId", "gameTimelineId", "createdAt"),
                    ),
                sagaMainContext = SagaPrompts.mainContext(sagaContent),
            )
        return promptService.buildRemotePrompt(SHARE_PLAYSTYLE_BLUEPRINT, args)
    }

    suspend fun emotionalPrompt(
        promptService: PromptService,
        saga: SagaContent,
    ): String {
        val args =
            ShareEmotionalArgs(
                emotionalReview = saga.data.emotionalReview ?: "",
                historyEmotionalSummary = saga.emotionalSummary(),
                characterArchetype =
                    saga.mainCharacter
                        ?.data
                        ?.profile
                        ?.toAINormalize() ?: "",
            )
        return promptService.buildRemotePrompt(SHARE_EMOTIONAL_BLUEPRINT, args)
    }

    suspend fun historyPrompt(
        promptService: PromptService,
        saga: SagaContent,
    ): String {
        val args =
            ShareHistoryArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                historyContext = saga.acts.joinToString(".\n") { it.actSummary(false) },
            )
        return promptService.buildRemotePrompt(SHARE_HISTORY_BLUEPRINT, args)
    }

    suspend fun relationsPrompt(
        promptService: PromptService,
        saga: SagaContent,
    ): String {
        val args =
            ShareRelationsArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                relationshipContext = saga.mainCharacter?.summarizeRelationships() ?: "",
            )
        return promptService.buildRemotePrompt(SHARE_RELATIONS_BLUEPRINT, args)
    }

    suspend fun characterPrompt(
        promptService: PromptService,
        characterContent: CharacterContent,
        sagaContent: SagaContent,
    ): String {
        val emotionalRanking =
            sagaContent
                .flatMessages()
                .filter { it.character?.id == characterContent.data.id }
                .rankEmotionalTone()

        val args =
            ShareCharacterArgs(
                characterName = characterContent.data.name,
                sagaMainContext = SagaPrompts.mainContext(sagaContent, characterContent),
                characterPersonality = characterContent.data.profile.toAINormalize(),
                emotionalRanking = emotionalRanking.joinToString("\n") { "${it.first.name} - ${it.second.size} messages" },
                characterEvents =
                    characterContent.events.map { it.event }.normalizetoAIItems(
                        listOf("id", "characterId", "gameTimelineId", "createdAt"),
                    ),
            )
        return promptService.buildRemotePrompt(SHARE_CHARACTER_BLUEPRINT, args)
    }
}
