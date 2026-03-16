package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ChapterConclusionContext
import com.ilustris.sagai.core.utils.normalizetoAIItems
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
import com.ilustris.sagai.features.home.data.model.getDirective

data class ChapterIntroductionArgs(
    val sagaMainContext: String,
    val firstChapterContext: String,
    val storyProgressionContext: String,
    val currentActTheme: String,
    val narrativeDirective: String,
    val taskInstruction: String,
    val conversationDirective: String,
)

data class ChapterGenerationArgs(
    val combinedContextJson: String,
    val expectedOutputFormat: String,
    val conversationDirective: String,
)

data class FirstChapterContextArgs(
    val actIntroduction: String,
)

data class StoryProgressionContextArgs(
    val previousChapterTitle: String,
    val previousChapterOverview: String,
    val isPreviousActConclusion: Boolean,
    val latestEventSummary: String,
    val latestEventMessages: String,
)

data class ChapterTaskInstructionArgs(
    val isFirstChapter: Boolean,
)

object ChapterPrompts {
    val CHAPTER_EXCLUSIONS =
        listOf("id", "currentEventId", "coverImage", "createdAt", "actId", "featuredCharacters")

    fun chapterSummary(sagaContent: SagaContent) =
        buildString {
            sagaContent.currentActInfo
                ?.chapters
                ?.filter { it.isComplete() }
                ?.map { it.data }
                ?.let { chapters ->
                    if (chapters.isNotEmpty()) {
                        appendLine("**CURRENT ACT CHAPTERS Overview:**")
                        appendLine("This section provides the summaries of chapters already written in the current act")
                        appendLine("// Use this to understand the immediate narrative progression and context within the act.")
                        appendLine(
                            chapters.normalizetoAIItems(
                                listOf(
                                    "id",
                                    "actId",
                                    "currentEventId",
                                    "coverImage",
                                    "createdAt",
                                    "featuredCharacters",
                                ),
                            ),
                        )
                    }
                }
        }

    suspend fun chapterIntroductionPrompt(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        sagaContent: SagaContent,
        currentChapter: Chapter,
        currentAct: ActContent,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
    ): String {
        // Check if this is the very first chapter of the saga
        val isFirstChapter =
            sagaContent
                .flatChapters()
                .firstOrNull()
                ?.data
                ?.id == currentChapter.id

        val firstChapterContext =
            assembleFirstChapterContextBlock(promptService, isFirstChapter, currentAct)
        val storyProgressionContext =
            assembleStoryProgressionContextBlock(
                promptService,
                sagaContent,
                currentChapter,
                isFirstChapter,
                currentAct,
            )
        val taskInstruction = assembleChapterTaskInstructionBlock(promptService, isFirstChapter)

        val args =
            ChapterIntroductionArgs(
                sagaMainContext = SagaPrompts.mainContext(sagaContent),
                firstChapterContext = firstChapterContext,
                storyProgressionContext = storyProgressionContext,
                currentActTheme = currentAct.data.introduction,
                narrativeDirective = sagaContent.getDirective(),
                taskInstruction = taskInstruction,
                conversationDirective = config.conversationDirective,
            )

        return promptService.buildRemotePrompt("chapter_introduction_prompt", args)
    }

    private suspend fun assembleFirstChapterContextBlock(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        isFirstChapter: Boolean,
        currentAct: ActContent,
    ): String {
        if (!isFirstChapter) return ""

        val args = FirstChapterContextArgs(actIntroduction = currentAct.data.introduction)

        return promptService.buildRemotePrompt("first_chapter_context_template", args)
    }

    private suspend fun assembleStoryProgressionContextBlock(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        sagaContent: SagaContent,
        currentChapter: Chapter,
        isFirstChapter: Boolean,
        currentAct: ActContent,
    ): String {
        val allChapters = sagaContent.flatChapters()
        val currentChapterIndex = allChapters.indexOfFirst { it.data.id == currentChapter.id }
        val previousChapter =
            if (currentChapterIndex > 0) allChapters[currentChapterIndex - 1] else null

        val allEvents = sagaContent.flatEvents()
        val latestEvent = allEvents.lastOrNull { it.isComplete() }

        if (isFirstChapter || (previousChapter == null && latestEvent == null)) return ""

        val args =
            StoryProgressionContextArgs(
                previousChapterTitle = previousChapter?.data?.title ?: "",
                previousChapterOverview = previousChapter?.data?.overview ?: "",
                isPreviousActConclusion = previousChapter != null && previousChapter.data.actId != currentAct.data.id,
                latestEventSummary = latestEvent?.data?.content ?: "",
                latestEventMessages =
                    latestEvent
                        ?.messages
                        ?.reversed()
                        ?.map { it.message }
                        ?.normalizetoAIItems(ChatPrompts.messageExclusions) ?: "",
            )

        return promptService.buildRemotePrompt("story_progression_context_template", args)
    }

    private suspend fun assembleChapterTaskInstructionBlock(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        isFirstChapter: Boolean,
    ): String {
        val args = ChapterTaskInstructionArgs(isFirstChapter = isFirstChapter)

        return promptService.buildRemotePrompt("chapter_task_instruction_template", args)
    }

    @Suppress("ktlint:standard:max-line-length")
    suspend fun chapterGeneration(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        sagaContent: SagaContent,
        currentChapterContent: ChapterContent,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
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
                        .filter { it.isComplete() }
                        .map { it.data },
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
                conversationDirective = config.conversationDirective,
            )

        return promptService.buildRemotePrompt("chapter_generation_prompt", args)
    }
}
