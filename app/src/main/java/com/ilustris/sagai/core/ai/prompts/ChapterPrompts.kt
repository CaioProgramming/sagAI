package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ChapterConclusionContext
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.toJsonFormatIncludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterGeneration
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.getDirectiveKey

data class ChapterIntroductionArgs(
    val sagaMainContext: String,
    val actContext: String,
    val previousChapterContext: String,
    val latestEventsContext: String,
    val currentActTheme: String,
    val narrativeDirective: String,
    val conversationDirective: String,
)

data class ChapterGenerationArgs(
    val combinedContextJson: String,
    val expectedOutputFormat: String,
    val conversationDirective: String,
)

object ChapterPrompts {
    val CHAPTER_EXCLUSIONS =
        listOf("id", "currentEventId", "coverImage", "createdAt", "actId", "featuredCharacters")

    suspend fun chapterIntroductionPrompt(
        promptService: PromptService,
        sagaContent: SagaContent,
        currentChapter: Chapter,
        currentAct: ActContent,
        conversationDirective: String,
        rules: NarrativeRules,
    ): String {
        val allChapters = sagaContent.flatChapters()
        val currentChapterIndex = allChapters.indexOfFirst { it.data.id == currentChapter.id }
        val previousChapter =
            if (currentChapterIndex > 0) allChapters[currentChapterIndex - 1] else null
        val allEvents = sagaContent.flatEvents()

        val args =
            ChapterIntroductionArgs(
                sagaMainContext = SagaPrompts.mainContext(sagaContent),
                actContext = currentAct.data.introduction,
                previousChapterContext = previousChapter?.data?.overview ?: "",
                latestEventsContext =
                    allEvents.lastOrNull { it.isComplete(rules) }?.data?.content
                        ?: "No past events recorded.",
                currentActTheme =
                    promptService.buildRemotePrompt(
                        currentAct.getDirectiveKey(
                            sagaContent,
                        ),
                        emptyMap(),
                    ),
                narrativeDirective =
                    promptService.buildRemotePrompt(
                        sagaContent.getDirectiveKey(),
                        emptyMap(),
                    ),
                conversationDirective = conversationDirective,
            )

        return promptService.buildRemotePrompt("chapter_introduction_blueprint", args)
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
                sagaData = sagaContent.data,
                mainCharacter = sagaContent.mainCharacter?.data,
                eventsOfThisChapter =
                    currentChapterContent.events
                        .filter {
                            it.isComplete(
                                rules,
                            )
                        }.map { it.data },
                previousChaptersInCurrentAct = currentChapters.map { it.data },
                previousActData = previousAct?.data,
            )

        val includedFields =
            listOf(
                "sagaData",
                "mainCharacter",
                "previousActData",
                "previousChaptersInCurrentAct",
                "eventsOfThisChapter",
                "title",
                "description",
                "content",
                "genre",
                "name",
                "backstory",
            )

        val combinedContextJson = promptDataContext.toJsonFormatIncludingFields(includedFields)

        val args =
            ChapterGenerationArgs(
                combinedContextJson = combinedContextJson,
                expectedOutputFormat =
                    toJsonMap(
                        ChapterGeneration::class.java,
                    ),
                conversationDirective = conversationDirective,
            )

        return promptService.buildRemotePrompt("chapter_generation_blueprint", args)
    }
}
