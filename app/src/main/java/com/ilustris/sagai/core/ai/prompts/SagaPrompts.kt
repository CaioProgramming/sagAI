package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.prompts.ChatPrompts.messageExclusions
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.rankTimelineEmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters

data class EndCreditsArgs(
    val sagaContext: String,
    val mainCharacter: String,
    val sagaCast: String,
    val sagaHistory: String,
    val conversationDirective: String,
)

data class SagaEndingArgs(
    val sagaContext: String,
    val sagaHistory: String,
    val sagaCast: String,
    val emotionalJourney: String,
    val emotionalFlow: String,
    val dominantTones: String,
    val dominantTone: String,
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
    val characterName: String,
    val actSummaries: String,
    val recentMessages: String,
    val conversationDirective: String,
)

data class SagaResumeArgs(
    val sagaContext: String,
    val sceneSummary: String,
    val originalIntro: String,
)

data class CharacterInsightArgs(
    val sagaContext: String,
    val characterName: String,
    val characterPersonality: String,
    val characterVisualProfile: String,
    val characterStyle: String,
    val characterEvents: String,
    val characterRelationships: String,
    val conversationDirective: String,
)

data class WikiInsightArgs(
    val sagaMainContext: String,
    val storyContext: String,
    val recentPagesSummary: String,
    val existingWikis: String,
    val existingRelationships: String,
    val newDialogueBurst: String,
    val narrativeStyle: String,
)

data class TimelineInsightArgs(
    val chapterTimeline: String,
)

object SagaPrompts {
    const val REVIEW_GENERATION_BLUEPRINT = "review_generation_blueprint"
    const val SAGA_END_CREDITS_BLUEPRINT = "saga_end_credits_blueprint"
    const val SAGA_RESUME_BLUEPRINT = "saga_resume_blueprint"
    const val STORY_BRIEFING_BLUEPRINT = "story_briefing_blueprint"
    const val CHARACTER_INSIGHT_BLUEPRINT = "character_resume_blueprint"
    const val WIKI_INSIGHT_BLUEPRINT = "unified_lore_generation_blueprint"
    const val TIMELINE_INSIGHT_BLUEPRINT = "timeline_context_blueprint"
    const val SAGA_ENDING_BLUEPRINT = "saga_ending_blueprint"

    val SAGA_EXCLUDED_FIELDS =
        listOf(
            "id",
            "icon",
            "artwork",
            "createdAt",
            "mainCharacterId",
            "isDebug",
            "endMessage",
            "currentActId",
            "endedAt",
            "review",
            "emotionalReview",
            "isEnded",
            "playTimeMs",
            "emotionalProfile",
        )

    val summaryExclusions =
        listOf(
            "notificationHook",
            "notificationCharacterName",
            "quote",
        )

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
                appendLine(it.data.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS))
            }
        }
    }

    suspend fun endCredits(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): SplitPrompt {
        val args =
            EndCreditsArgs(
                sagaContext = mainContext(saga),
                sagaCast =
                    saga.characters
                        .map { it.data }
                        .normalizetoAIItems(ChatPrompts.CHARACTER_EXCLUSIONS),
                mainCharacter =
                    saga.mainCharacter?.data?.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS)
                        ?: "",
                sagaHistory = saga.acts.joinToString("\n") { it.actSummary(false) },
                conversationDirective = conversationDirective,
            )

        return promptService.buildSplitBlueprint(SAGA_END_CREDITS_BLUEPRINT, args)
    }

    suspend fun generateSagaEnding(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): SplitPrompt {
        val emotionalRanking = saga.flatEvents().rankTimelineEmotionalTone()
        val charactersMap =
            saga.characters.joinToString { "${it.data.fullName()}(${it.data.nicknames?.joinToString()}): ${it.data.backstory}" }
        val args =
            mapOf(
                "sagaContext" to
                    buildMap {
                        put("description", saga.data.toAINormalize(SAGA_EXCLUDED_FIELDS))
                        put("charactersCast", charactersMap)
                        put(
                            "sagaStory",
                            saga.acts
                                .map { it.data }
                                .normalizetoAIItems(ActPrompts.ACT_EXCLUSIONS),
                        )
                        put("emotionalJourney", saga.emotionalSummary())
                    }.toAINormalize(),
            )

        return promptService.buildSplitBlueprint(SAGA_ENDING_BLUEPRINT, args)
    }

    suspend fun reviewGeneration(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): SplitPrompt {
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

        return promptService.buildSplitBlueprint(REVIEW_GENERATION_BLUEPRINT, args)
    }

    suspend fun generateStoryBriefing(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): SplitPrompt {
        val args =
            StoryBriefingArgs(
                sagaTitle = saga.data.title,
                genreName = saga.data.genre.name,
                characterName = saga.mainCharacter?.data?.name ?: "Unnamed Hero",
                actSummaries = saga.acts.joinToString("; ") { it.actSummary() },
                recentMessages =
                    saga
                        .flatMessages()
                        .takeLast(5)
                        .reversed()
                        .map { it.message }
                        .normalizetoAIItems(excludingFields = messageExclusions),
                conversationDirective = conversationDirective,
            )

        return promptService.buildSplitBlueprint(STORY_BRIEFING_BLUEPRINT, args)
    }

    suspend fun sagaResume(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): SplitPrompt {
        val args =
            SagaResumeArgs(
                sagaContext = mainContext(saga),
                sceneSummary =
                    if (saga.acts.isEmpty()) {
                        "The story is just beginning."
                    } else {
                        saga.acts.joinToString("\n") { it.actSummary() }
                    },
                originalIntro = saga.data.description,
            )

        return promptService.buildSplitBlueprint(SAGA_RESUME_BLUEPRINT, args)
    }

    suspend fun charactersInsight(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): SplitPrompt {
        val mainChar = saga.mainCharacter
        val args =
            CharacterInsightArgs(
                sagaContext = mainContext(saga),
                characterName = mainChar?.data?.name ?: "The Protagonist",
                characterPersonality = mainChar?.data?.profile?.personality ?: "Unknown",
                characterVisualProfile =
                    mainChar?.data?.details?.toAINormalize()
                        ?: "Not described",
                characterStyle = mainChar?.data?.backstory ?: "Mysterious",
                characterEvents =
                    saga.acts.joinToString("\n") { it.actSummary(false) },
                characterRelationships =
                    mainChar?.summarizeRelationships()
                        ?: "No significant relationships yet.",
                conversationDirective = conversationDirective,
            )

        return promptService.buildSplitBlueprint(CHARACTER_INSIGHT_BLUEPRINT, args)
    }

    suspend fun wikiInsight(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): SplitPrompt {
        val args =
            WikiInsightArgs(
                sagaMainContext = mainContext(saga),
                storyContext =
                    saga.acts.lastOrNull()?.actSummary(false)
                        ?: "The story is unfolding.",
                recentPagesSummary = saga.acts.joinToString("\n") { it.actSummary(false) },
                existingWikis = saga.wikis.joinToString("\n") { "- ${it.title}: ${it.content}" },
                existingRelationships =
                    saga.mainCharacter?.summarizeRelationships()
                        ?: "No connections yet.",
                newDialogueBurst =
                    saga
                        .flatMessages()
                        .takeLast(10)
                        .joinToString("\n") { "${it.character?.name}: ${it.message.text}" },
                narrativeStyle = conversationDirective,
            )

        return promptService.buildSplitBlueprint(WIKI_INSIGHT_BLUEPRINT, args)
    }

    suspend fun timelineInsight(
        promptService: PromptService,
        saga: SagaContent,
        conversationDirective: String,
    ): SplitPrompt {
        val args =
            TimelineInsightArgs(
                chapterTimeline = saga.acts.joinToString("\n") { it.actSummary(false) },
            )

        return promptService.buildSplitBlueprint(TIMELINE_INSIGHT_BLUEPRINT, args)
    }

    fun charactersSummary(saga: SagaContent): String =
        saga.characters.joinToString("\n") {
            "- ${it.data.id}: ${it.data.name}"
        }
}
