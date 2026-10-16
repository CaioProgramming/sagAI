package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterStatus
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.player.data.model.PlayerProfileData
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
    val journeyStats: String,
)

data class SharePlayerArgs(
    val playerProfile: String,
    val journeyStats: String,
)

data class ShareHistoryArgs(
    val sagaMainContext: String,
    val chapterTitles: String,
    val journeyStats: String,
)

data class ShareRelationsArgs(
    val sagaMainContext: String,
    val relationshipContext: String,
)

data class ShareCharacterArgs(
    val characterName: String? = null,
    val characterGender: String? = null,
    val characterNicknames: String? = null,
    val characterStatus: String? = null,
    val sagaMainContext: String,
    val characterPersonality: String,
    val characterArcs: String? = null,
    val emotionalRanking: String,
    val characterEvents: String,
)

object SharePrompts {
    const val SHARE_CHARACTER_BLUEPRINT = "share_character_blueprint"
    const val SHARE_EMOTIONAL_BLUEPRINT = "share_emotional_blueprint"
    const val SHARE_HISTORY_BLUEPRINT = "share_history_blueprint"
    const val SHARE_PLAYSTYLE_BLUEPRINT = "share_playstyle_blueprint"
    const val SHARE_RELATIONS_BLUEPRINT = "share_relations_blueprint"
    const val SHARE_PLAYER_BLUEPRINT = "share_player_blueprint"

    private const val MAX_CHAPTER_TITLES = 12
    private const val MAX_CHARACTER_ARCS = 3
    private const val MAX_ARC_CHARS = 280
    private const val MAX_EMOTIONS = 3
    private const val MAX_CHARACTER_EVENTS = 6
    private const val MS_PER_DAY = 86_400_000L
    private const val MS_PER_HOUR = 3_600_000L

    /**
     * Plain numbers about how long and how much the player lived the saga, so a share can speak in
     * concrete terms (days, chapters, messages) instead of abstractions.
     */
    private fun journeyStats(saga: SagaContent): String {
        val end = if (saga.data.endedAt > 0L) saga.data.endedAt else System.currentTimeMillis()
        val days = ((end - saga.data.createdAt) / MS_PER_DAY).coerceAtLeast(0L) + 1
        return buildString {
            appendLine("messages: ${saga.flatMessages().size}")
            appendLine("chapters: ${saga.flatChapters().size}")
            appendLine("characters: ${saga.characters.size}")
            if (saga.data.playTimeMs > 0L) appendLine("hoursPlayed: ${saga.data.playTimeMs / MS_PER_HOUR}")
            append("daysSinceStart: $days")
        }
    }

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
    ): SplitPrompt {
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
        return promptService.buildSplitBlueprint(SHARE_PLAYSTYLE_BLUEPRINT, args)
    }

    suspend fun emotionalPrompt(
        promptService: PromptService,
        saga: SagaContent,
    ): SplitPrompt {
        val args =
            ShareEmotionalArgs(
                emotionalReview = saga.data.emotionalReview ?: "",
                historyEmotionalSummary = saga.emotionalSummary(),
                characterArchetype =
                    saga.mainCharacter
                        ?.data
                        ?.profile
                        ?.toAINormalize() ?: "",
                journeyStats = journeyStats(saga),
            )
        return promptService.buildSplitBlueprint(SHARE_EMOTIONAL_BLUEPRINT, args)
    }

    suspend fun historyPrompt(
        promptService: PromptService,
        saga: SagaContent,
    ): SplitPrompt {
        val args =
            ShareHistoryArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                // Titles only: the full act summaries run to tens of thousands of characters, far
                // more than a one-line teaser can use, and slow the call down for nothing.
                chapterTitles =
                    saga
                        .flatChapters()
                        .map { it.data.title }
                        .filter { it.isNotBlank() }
                        .takeLast(MAX_CHAPTER_TITLES)
                        .joinToString("; "),
                journeyStats = journeyStats(saga),
            )
        return promptService.buildSplitBlueprint(SHARE_HISTORY_BLUEPRINT, args)
    }

    /**
     * The player as a storyteller, across every saga: the profile topics the app has built about how
     * they play, plus plain numbers on how much they have played. No saga is involved.
     */
    suspend fun playerPrompt(
        promptService: PromptService,
        profile: PlayerProfileData?,
        sagas: List<Saga>,
    ): SplitPrompt {
        val args =
            SharePlayerArgs(
                playerProfile =
                    profile
                        ?.topics
                        .orEmpty()
                        .filter { it.content.isNotBlank() }
                        .joinToString("\n") { topic ->
                            buildString {
                                if (topic.title.isNotBlank()) append("${topic.title}: ")
                                append(topic.content)
                                // The short aside is the sharpest line the profile has.
                                topic.comment?.takeIf { it.isNotBlank() }?.let { append(" (aside: $it)") }
                            }
                        },
                journeyStats =
                    buildString {
                        appendLine("sagas: ${sagas.size}")
                        appendLine("finishedSagas: ${sagas.count { it.isEnded }}")
                        append("hoursPlayed: ${sagas.sumOf { it.playTimeMs } / MS_PER_HOUR}")
                    },
            )
        return promptService.buildSplitBlueprint(SHARE_PLAYER_BLUEPRINT, args)
    }

    suspend fun relationsPrompt(
        promptService: PromptService,
        saga: SagaContent,
    ): SplitPrompt {
        val args =
            ShareRelationsArgs(
                sagaMainContext = SagaPrompts.mainContext(saga),
                relationshipContext = saga.mainCharacter?.summarizeRelationships() ?: "",
            )
        return promptService.buildSplitBlueprint(SHARE_RELATIONS_BLUEPRINT, args)
    }

    suspend fun characterPrompt(
        promptService: PromptService,
        characterContent: CharacterContent,
        sagaContent: SagaContent,
    ): SplitPrompt {
        val emotionalRanking =
            sagaContent
                .flatMessages()
                .filter { it.character?.id == characterContent.data.id }
                .rankEmotionalTone()

        val character = characterContent.data
        val args =
            ShareCharacterArgs(
                characterName = character.name,
                characterGender = character.details.physicalTraits.gender.takeIf { it.isNotBlank() },
                characterNicknames = character.nicknames?.takeIf { it.isNotEmpty() }?.joinToString(", "),
                characterStatus =
                    character.status
                        ?.takeIf { it != CharacterStatus.ACTIVE }
                        ?.let { listOfNotNull(it.name, character.statusNote).joinToString(": ") },
                sagaMainContext = SagaPrompts.mainContext(sagaContent, characterContent),
                characterPersonality = character.profile.toAINormalize(),
                characterArcs =
                    characterContent.arcs
                        .sortedBy { it.createdAt }
                        .takeLast(MAX_CHARACTER_ARCS)
                        .joinToString("\n") { "${it.title}: ${it.content.take(MAX_ARC_CHARS)}" }
                        .takeIf { it.isNotBlank() },
                // The few dominant tones say how the character felt; the long tail is noise.
                emotionalRanking =
                    emotionalRanking
                        .take(MAX_EMOTIONS)
                        .joinToString("\n") { "${it.first.name} - ${it.second.size} messages" },
                // The latest events, not all of them: the list grows without bound and the early
                // ones are the least of what the character is by now.
                characterEvents =
                    characterContent.events
                        .map { it.event }
                        .takeLast(MAX_CHARACTER_EVENTS)
                        .normalizetoAIItems(
                            listOf("id", "characterId", "gameTimelineId", "createdAt"),
                        ),
            )
        return promptService.buildSplitBlueprint(SHARE_CHARACTER_BLUEPRINT, args)
    }
}
