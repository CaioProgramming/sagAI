package com.ilustris.sagai.features.chapter.data.usecase

import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.ImageGuidelines
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.GenreReferenceHelper
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterGeneration
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ChapterUseCaseImpl
    @Inject
    constructor(
        private val chapterRepository: ChapterRepository,
        private val timelineRepository: TimelineRepository,
        private val wikiUseCase: WikiUseCase,
        private val gemmaClient: GemmaClient,
        private val imagenClient: ImagenClient,
        private val fileHelper: FileHelper,
        private val genreReferenceHelper: GenreReferenceHelper,
    ) : ChapterUseCase {
        override suspend fun saveChapter(chapter: Chapter): Chapter = chapterRepository.saveChapter(chapter)

        override suspend fun deleteChapter(chapter: Chapter) = chapterRepository.deleteChapter(chapter)

        override suspend fun updateChapter(chapter: Chapter) = chapterRepository.updateChapter(chapter)

        override suspend fun deleteChapterById(chapterId: Int) = chapterRepository.deleteChapterById(chapterId)

        override suspend fun deleteAllChapters() = chapterRepository.deleteAllChapters()

        override suspend fun generateChapter(
            saga: SagaContent,
            chapterContent: ChapterContent,
        ) = executeRequest {
            gemmaClient
                .generate<ChapterGeneration>(
                    prompt =
                        generateChapterPrompt(
                            saga = saga,
                            currentChapter = chapterContent,
                        ),
                    requireTranslation = true,
                )!!
        }

        override suspend fun reviewChapter(
            saga: SagaContent,
            chapterContent: ChapterContent,
        ) {
            delay(3.seconds)
            if (chapterContent.data.introduction.isEmpty()) {
                generateChapterIntroduction(saga, chapterContent.data, saga.findChapterAct(chapterContent.data)!!)
            }
            cleanUpEmptyTimeLines(chapterContent)
            wikiUseCase.mergeWikis(saga, chapterContent.events.map { it.updatedWikis }.flatten())
        }

        private suspend fun cleanUpEmptyTimeLines(chapter: ChapterContent) {
            val emptyEvents = chapter.events.filter { it.isComplete().not() }.map { it.data }
            if (emptyEvents.isEmpty()) {
                Log.w(javaClass.simpleName, "cleanUpEmptyTimeLines: No timelines to clean up")
                return
            }
            emptyEvents.forEach { timeline ->
                timelineRepository.deleteTimeline(timeline)
            }
            Log.w(javaClass.simpleName, "cleanUpEmptyTimeLines: Removed ${emptyEvents.size} timelines")
        }

        @OptIn(PublicPreviewAPI::class)
        override suspend fun generateChapterCover(
            chapter: ChapterContent,
            saga: SagaContent,
        ): RequestResult<Chapter> =
            executeRequest {
                val characters =
                    chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter!!.data) }
                val coverBitmap = genreReferenceHelper.getCoverReference(saga.data.genre).getSuccess()
                val coverReference =
                    coverBitmap?.let {
                        ImageReference(
                            it,
                            ImageGuidelines.compositionReferenceGuidance,
                        )
                    }
                val charactersIcons =
                    characters.mapNotNull { character ->

                        val characterBitmap =
                            fileHelper
                                .readFile(character.image)
                                ?.decodeToImageBitmap()
                                ?.asAndroidBitmap()

                        characterBitmap?.let {
                            ImageReference(
                                it,
                                ImageGuidelines.characterVisualReferenceGuidance(character.name),
                            )
                        }
                    }

                val visualComposition =
                    imagenClient
                        .extractComposition(
                            listOfNotNull(coverReference),
                        ).getSuccess()
                val coverContext =
                    mapOf(
                        "featuredCharacters" to
                            characters.formatToJsonArray(
                                listOf(
                                    "id",
                                    "image",
                                    "sagaId",
                                    "joinedAt",
                                    "emojified",
                                    "hexColor",
                                    "firstSceneId",
                                    "abilities",
                                    "carriedItems",
                                    "backstory",
                                ),
                            ),
                    )

                val coverContextJson = coverContext.toJsonFormat()
                val coverPrompt =
                    SagaPrompts.iconDescription(
                        saga.data.genre,
                        coverContextJson,
                        visualComposition,
                    )
                val promptGeneration =
                    gemmaClient.generate<String>(
                        coverPrompt,
                        references = charactersIcons,
                        requireTranslation = false,
                    )!!
                val genCover =
                    imagenClient
                        .generateImage(
                            promptGeneration.plus(ImagePrompts.criticalGenerationRule()),
                        )!!

                val coverFile =
                    fileHelper.saveFile(
                        chapter.data.title,
                        genCover,
                        path = "${saga.data.id}/chapters/",
                    )!!
                val newChapter =
                    chapter.data.copy(
                        coverImage = coverFile.path,
                    )

                chapterRepository.updateChapter(newChapter)
            }

        private fun generateChapterPrompt(
            saga: SagaContent,
            currentChapter: ChapterContent,
        ) = ChapterPrompts.chapterGeneration(saga, currentChapter)

        override suspend fun generateChapterIntroduction(
            saga: SagaContent,
            chapter: Chapter,
            act: ActContent,
        ): RequestResult<Chapter> =
            executeRequest {
                val contextSummary =
                    gemmaClient.generate<SceneSummary>(
                        ChatPrompts.sceneSummarizationPrompt(
                            saga,
                            saga
                                .flatMessages()
                                .map { it.message }
                                .sortedByDescending { it.timestamp }
                                .take(UpdateRules.LORE_UPDATE_LIMIT),
                        ),
                    )
                val prompt = ChapterPrompts.chapterIntroductionPrompt(saga, chapter, act, contextSummary)
                delay(2.seconds)
                val intro =
                    gemmaClient.generate<String>(
                        prompt,
                        requireTranslation = true,
                    )!!
                val updated = chapter.copy(introduction = intro)
                chapterRepository.updateChapter(updated)
            }
    }
