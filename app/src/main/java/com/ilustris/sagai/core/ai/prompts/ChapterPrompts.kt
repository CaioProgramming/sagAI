package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ChapterConclusionContext
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts.CHAPTER_SYNTHESIS_BLUEPRINT
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.GeneratedChoiceCard
import com.ilustris.sagai.features.chapter.data.model.UnifiedChapterUpdate
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.buildContextualHistory
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.getDirectiveKey
import com.ilustris.sagai.features.home.data.model.historySummary

data class ChapterIntroductionArgs(
    val sagaMainContext: String,
    val narrativeStyle: String,
    val storyHistory: String,
    val volumeContext: String,
    val lastStateContext: String,
    val storyContext: String,
)

data class ChapterGenerationArgs(
    val chapterContext: String,
    val characterIndex: String,
    val narrativeStyle: String,
)

data class ChapterSynthesisArgs(
    val chapterContext: String,
    val characterIndex: String,
    val narrativeStyle: String,
)

data class PlayerSpectrumRewriteArgs(
    val previousSpectrum: String,
    val chapterSummary: String,
    val answeredChoices: String,
)

object ChapterPrompts {
    const val CHAPTER_GENERATION_BLUEPRINT = "chapter_generation_blueprint"
    const val CHAPTER_INTRODUCTION_BLUEPRINT = "chapter_introduction_blueprint"
    const val CHAPTER_SYNTHESIS_BLUEPRINT = "chapter_synthesis_blueprint"
    const val PLAYER_SPECTRUM_REWRITE_BLUEPRINT = "player_spectrum_rewrite_blueprint"
    const val CHAPTER_CHOICE_CARDS_BLUEPRINT = "chapter_choice_cards_blueprint"

    /**
     * [CHAPTER_SYNTHESIS_BLUEPRINT] must return `continuitySummary` in [UnifiedChapterUpdate]:
     * - `establishedFacts`: immutable canon from this chapter
     * - `openThreads`: unresolved setups
     * - `consequences`: cause-effect pairs
     * - `characterStates`: where key characters ended
     * - `persistentSetups`: long-range seeds that may pay off many chapters later
     */

    val CHAPTER_EXCLUSIONS =
        listOf(
            "id",
            "currentEventId",
            "coverImage",
            "artwork",
            "createdAt",
            "actId",
            "featuredCharacters",
            // Reach future prompts only through PlayerSpectrumPrompts.lensInstructions (text, never tags).
            "playerChoiceCards",
            "playerSpectrum",
            "playerChoiceAnswers",
        ) +
            LorePrompts.LORE_OUTPUT_ONLY_FIELDS

    suspend fun chapterIntroductionPrompt(
        promptService: PromptService,
        sagaContent: SagaContent,
        narrativeRules: NarrativeRules,
    ): SplitPrompt {
        val storyContext = sagaContent.buildContextualHistory(narrativeRules)

        return promptService.buildSplitBlueprint(
            CHAPTER_INTRODUCTION_BLUEPRINT,
            buildMap {
                put("storyContext", storyContext.toAINormalize())
            },
        )
    }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun chapterGeneration(
        promptService: PromptService,
        sagaContent: SagaContent,
        currentChapterContent: ChapterContent,
        rules: NarrativeRules,
        conversationDirective: String,
    ): SplitPrompt {
        val chapterAct = sagaContent.findChapterAct(currentChapterContent.data)
        val isFirstAct =
            sagaContent.acts
                .firstOrNull()
                ?.data
                ?.id == chapterAct?.data?.id
        val currentChapters = chapterAct?.chapters?.filter { it.data.id != currentChapterContent.data.id } ?: emptyList()

        val previousAct =
            if (isFirstAct) {
                null
            } else {
                val currentIndex =
                    sagaContent.acts.indexOfFirst { it.data.id == chapterAct?.data?.id }
                if (currentIndex > 0) sagaContent.acts[currentIndex - 1] else null
            }

        val promptDataContext =
            ChapterConclusionContext(
                sagaData = sagaContent.data.toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS),
                mainCharacter = sagaContent.mainCharacter?.data?.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS),
                eventsOfThisChapter =
                    currentChapterContent.events
                        .map { it.data }
                        .normalizetoAIItems(LorePrompts.TIMELINE_EXCLUDED_FIELDS),
                previousChaptersInCurrentAct =
                    currentChapters
                        .map { it.data }
                        .normalizetoAIItems(CHAPTER_EXCLUSIONS),
                previousActData = previousAct?.data.toAINormalize(ActPrompts.ACT_EXCLUSIONS),
            )

        val chapterContext = promptDataContext.toJsonFormat()

        val args =
            ChapterGenerationArgs(
                chapterContext = chapterContext,
                characterIndex = SagaPrompts.charactersSummary(sagaContent),
                narrativeStyle = conversationDirective,
            )

        return promptService.buildSplitBlueprint(CHAPTER_GENERATION_BLUEPRINT, args)
    }

    /**
     * What a closing chapter looks like to the model — shared by the choice cards (written first,
     * from the same events) and the synthesis that follows once they're answered.
     */
    private fun chapterClosingContext(
        saga: SagaContent,
        chapter: ChapterContent,
        includePlayerChoices: Boolean = false,
    ): ChapterConclusionContext {
        val chapterAct = saga.findChapterAct(chapter.data)
        val isFirstAct =
            saga.acts
                .firstOrNull()
                ?.data
                ?.id == chapterAct?.data?.id

        val previousAct =
            if (isFirstAct) {
                null
            } else {
                val currentIndex = saga.acts.indexOfFirst { it.data.id == chapterAct?.data?.id }
                if (currentIndex > 0) saga.acts[currentIndex - 1] else null
            }

        return ChapterConclusionContext(
            sagaData = saga.data.toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS),
            mainCharacter = saga.mainCharacter?.data?.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS),
            eventsOfThisChapter =
                chapter.events
                    .map { it.data }
                    .normalizetoAIItems(LorePrompts.TIMELINE_EXCLUDED_FIELDS),
            previousChaptersInCurrentAct =
                (chapterAct?.chapters ?: emptyList())
                    .filter { it.data.id != chapter.data.id }
                    .map { it.data }
                    .normalizetoAIItems(CHAPTER_EXCLUSIONS),
            previousActData = previousAct?.data.toAINormalize(ActPrompts.ACT_EXCLUSIONS),
            playerChoicesOfThisChapter = if (includePlayerChoices) chapter.data.answeredChoicesSummary() else null,
        )
    }

    /**
     * What the player picked on each card, as they read it — the option's text and its emotional
     * weight. The hidden tags stay out: they're a clinical read of the player, and the prose must
     * never start sounding like one.
     */
    private fun Chapter.answeredChoicesSummary(): String? {
        val answers = playerChoiceAnswers ?: return null
        return playerChoiceCards
            .orEmpty()
            .zip(answers)
            .mapNotNull { (card, tag) ->
                val picked = card.options.find { it.tag == tag } ?: return@mapNotNull null
                "- ${card.choiceTitle} -> ${picked.text}" + (picked.emotionalTone?.let { " (tone: ${it.name})" } ?: "")
            }.takeIf { it.isNotEmpty() }
            ?.joinToString("\n")
    }

    suspend fun chapterSynthesisPrompt(
        promptService: PromptService,
        saga: SagaContent,
        chapter: ChapterContent,
        narrativeRules: NarrativeRules,
        conversationDirective: String,
    ): SplitPrompt {
        val synthesisContext = chapterClosingContext(saga, chapter, includePlayerChoices = true)

        val args =
            ChapterSynthesisArgs(
                chapterContext = synthesisContext.toAINormalize(),
                characterIndex = SagaPrompts.charactersSummary(saga),
                narrativeStyle = emptyString(),
            )

        return promptService.buildSplitBlueprint(CHAPTER_SYNTHESIS_BLUEPRINT, args.asMap())
    }

    /**
     * The chapter-closure dilemmas, asked for on their own before the chapter is synthesized —
     * built from the same closing context the synthesis reads, so the cards are about what the
     * player actually lived through, and their answers can then shape the synthesis itself.
     */
    suspend fun choiceCardsPrompt(
        promptService: PromptService,
        saga: SagaContent,
        chapter: ChapterContent,
    ): SplitPrompt {
        val args =
            ChapterSynthesisArgs(
                chapterContext = chapterClosingContext(saga, chapter).toAINormalize(),
                characterIndex = SagaPrompts.charactersSummary(saga),
                narrativeStyle = emptyString(),
            )
        return promptService.buildSplitBlueprint(CHAPTER_CHOICE_CARDS_BLUEPRINT, args.asMap())
    }

    /**
     * Small non-streaming rewrite of the player spectrum. The model must reassess
     * [previousSpectrum] against the latest picks (reinforced, complicated or contradicted) and
     * return a fresh read, never a concatenation. Each pick is paired with its dilemma so the
     * hidden tag is read in context; the tags never reach the player.
     */
    suspend fun playerSpectrumRewritePrompt(
        promptService: PromptService,
        chapter: ChapterContent,
        previousSpectrum: String?,
        cards: List<GeneratedChoiceCard>,
        answers: List<String>,
    ): SplitPrompt {
        val answeredChoices =
            cards
                .zip(answers)
                .joinToString("\n") { (card, tag) ->
                    val picked = card.options.find { it.tag == tag }
                    val tone = picked?.emotionalTone?.let { ", tone: ${it.name}" }.orEmpty()
                    "- ${card.choiceTitle} => picked \"${picked?.text ?: tag}\" (read: $tag$tone)"
                }

        val args =
            PlayerSpectrumRewriteArgs(
                previousSpectrum = previousSpectrum?.takeIf { it.isNotBlank() } ?: "None yet. This is the first read.",
                // Answered before the synthesis, so the chapter has no prose of its own yet —
                // its events are what the player just lived through.
                chapterSummary =
                    chapter.events.joinToString("\n") { "- ${it.data.title}: ${it.data.content}" },
                answeredChoices = answeredChoices,
            )

        return promptService.buildSplitBlueprint(PLAYER_SPECTRUM_REWRITE_BLUEPRINT, args)
    }
}
