package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ChapterConclusionContext
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.getDirectiveKey
import com.ilustris.sagai.features.home.data.model.historySummary

data class ChapterIntroductionArgs(
    val sagaMainContext: String,
    val narrativeStyle: String,
    val storyHistory: String,
    val volumeContext: String,
    val lastStateContext: String,
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

object ChapterPrompts {
    const val CHAPTER_GENERATION_BLUEPRINT = "chapter_generation_blueprint"
    const val CHAPTER_INTRODUCTION_BLUEPRINT = "chapter_introduction_blueprint"
    const val CHAPTER_SYNTHESIS_BLUEPRINT = "chapter_synthesis_blueprint"

    val CHAPTER_EXCLUSIONS =
        listOf("id", "currentEventId", "coverImage", "createdAt", "actId", "featuredCharacters")

    suspend fun chapterIntroductionPrompt(
        promptService: PromptService,
        sagaContent: SagaContent,
        currentChapter: Chapter,
        conversationDirective: String,
    ): String {
        val lastEvent = sagaContent.flatEvents().lastOrNull()?.data
        val lastState =
            lastEvent?.let {
                "The story last drew breath here: ${it.title} - ${it.content}"
            } ?: "The chapter begins as a direct continuation of the volume's opening."

        val args =
            ChapterIntroductionArgs(
                sagaMainContext = SagaPrompts.mainContext(sagaContent),
                narrativeStyle = conversationDirective,
                storyHistory = sagaContent.historySummary(),
                volumeContext =
                    promptService.buildRemotePrompt(
                        sagaContent.getDirectiveKey(
                            sagaContent.findAct(currentChapter.actId)?.data,
                        ),
                    ),
                lastStateContext = lastState,
            )

        return promptService.buildRemotePrompt(CHAPTER_INTRODUCTION_BLUEPRINT, args)
    }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun chapterGeneration(
        promptService: PromptService,
        sagaContent: SagaContent,
        currentChapterContent: ChapterContent,
        rules: NarrativeRules,
        conversationDirective: String,
    ): String {
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

        return promptService.buildRemotePrompt(CHAPTER_GENERATION_BLUEPRINT, args)
    }

    suspend fun chapterSynthesisPrompt(
        promptService: PromptService,
        saga: SagaContent,
        chapter: ChapterContent,
        narrativeRules: NarrativeRules,
        conversationDirective: String,
    ): String {
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

        val synthesisContext =
            ChapterConclusionContext(
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
            )

        val args =
            ChapterSynthesisArgs(
                chapterContext = synthesisContext.toJsonFormat(),
                characterIndex = SagaPrompts.charactersSummary(saga),
                narrativeStyle = conversationDirective,
            )

        return promptService.buildRemotePrompt(CHAPTER_SYNTHESIS_BLUEPRINT, args)
    }
}
