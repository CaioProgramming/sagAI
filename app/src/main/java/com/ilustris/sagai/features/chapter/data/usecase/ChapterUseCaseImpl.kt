package com.ilustris.sagai.features.chapter.data.usecase

import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts
import com.ilustris.sagai.core.ai.prompts.ImageGuidelines
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.analytics.AnalyticsConstants
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.GenreReferenceHelper
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterGeneration
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChapterUseCaseImpl
    @Inject
    constructor(
        private val chapterRepository: ChapterRepository,
        private val timelineRepository: TimelineRepository,
        private val emotionalUseCase: EmotionalUseCase,
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
            val genChapter =
                gemmaClient
                    .generate<ChapterGeneration>(
                        prompt =
                            generateChapterPrompt(
                                saga = saga,
                                currentChapter = chapterContent,
                            ),
                        requireTranslation = true,
                        useCore = true,
                        requirement = GemmaClient.ModelRequirement.HIGH,
                    )!!

            val genChapterCharacters =
                genChapter.featuredCharacters.mapNotNull { charactersNames ->
                    saga.characters
                        .find {
                            it.data.name.equals(
                                charactersNames,
                                ignoreCase = true,
                            )
                        }?.data
                        ?.id
                }

            val featuredCharacters =
                chapterContent
                    .fetchChapterMessages()
                    .rankTopCharacters(saga.getCharacters())
                    .take(3)
                    .map { it.first.id }

            val chapterUpdate =
                updateChapter(
                    chapterContent.data.copy(
                        title = genChapter.title,
                        overview = genChapter.overview,
                        featuredCharacters = genChapterCharacters.ifEmpty { featuredCharacters },
                        currentEventId = null,
                    ),
                )
            withContext(Dispatchers.IO) {
                generateEmotionalReview(
                    saga,
                    chapterContent.copy(
                        data = chapterUpdate,
                    ),
                )
            }

            chapterUpdate
        }

        override suspend fun generateEmotionalReview(
            saga: SagaContent,
            chapterContent: ChapterContent,
        ) = executeRequest {
            val emotionalReview =
                emotionalUseCase
                    .generateEmotionalReview(
                        saga,
                        buildString {
                            appendLine("Chapter Title: ${chapterContent.data.title}")
                            appendLine("Chapter Introduction: ${chapterContent.data.introduction}")
                            chapterContent.events.filter { it.isComplete() }.forEach { event ->
                                appendLine("Event Title: ${event.data.title}")
                                appendLine("Event description: ${event.data.content}")
                                appendLine("Event emotional review: ${event.data.emotionalReview}")
                                appendLine("Event emotional ranking: ${event.emotionalRanking(saga.mainCharacter?.data)}")
                            }
                        },
                    ).getSuccess()!!

            val newData = chapterContent.data.copy(emotionalReview = emotionalReview)
            updateChapter(newData)
        }

        override suspend fun reviewChapter(
            saga: SagaContent,
            chapterContent: ChapterContent,
        ) = executeRequest {
            cleanUpEmptyTimeLines(chapterContent)
            val chapterWikis = chapterContent.events.map { it.updatedWikis }.flatten()
            if (chapterWikis.size > 15) {
                wikiUseCase.mergeWikis(saga, chapterWikis)
            }
            var chapterUpdates = chapterContent.data
            if (chapterContent.data.emotionalReview.isNullOrEmpty()) {
                generateEmotionalReview(saga, chapterContent).onSuccess {
                    chapterUpdates =
                        chapterUpdates.copy(
                            emotionalReview = it.emotionalReview,
                        )
                }
            }
            if (chapterContent.data.introduction.isEmpty()) {
                generateChapterIntroduction(
                    saga,
                    chapterContent.data,
                    saga.findChapterAct(chapterContent.data)!!,
                ).onSuccess {
                    chapterUpdates =
                        it.copy(
                            introduction = it.introduction,
                        )
                }
            }

            chapterUpdates
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
                val coverBitmap =
                    genreReferenceHelper.getRandomCompositionReference(saga.data.genre).getSuccess()
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
                        characterHexColor = null,
                    )
                val promptGeneration =
                    gemmaClient.generate<String>(
                        coverPrompt,
                        references = charactersIcons,
                        requireTranslation = false,
                        requirement = GemmaClient.ModelRequirement.HIGH,
                    )!!

                // Review the generated description before image generation
                val reviewedPrompt =
                    imagenClient
                        .reviewAndCorrectPrompt(
                            imageType = AnalyticsConstants.ImageType.COVER,
                            genre = saga.data.genre,
                            visualDirection = visualComposition,
                            finalPrompt = promptGeneration,
                        ).getSuccess()

                // Use the reviewed prompt, or fallback to original if review failed
                val finalPromptForGeneration =
                    reviewedPrompt?.correctedPrompt ?: run {
                        Log.w(
                            "ChapterUseCase",
                            "Review failed or returned null, using original description",
                        )
                        promptGeneration
                    }

                val genCover =
                    imagenClient
                        .generateImage(
                            finalPromptForGeneration,
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
                val prompt =
                    ChapterPrompts.chapterIntroductionPrompt(saga, chapter, act)
                val intro =
                    gemmaClient.generate<String>(
                        prompt,
                        requireTranslation = true,
                        useCore = true,
                        requirement = GemmaClient.ModelRequirement.HIGH,
                    )!!
                val updated = chapter.copy(introduction = intro)
                chapterRepository.updateChapter(updated)
            }
    }
