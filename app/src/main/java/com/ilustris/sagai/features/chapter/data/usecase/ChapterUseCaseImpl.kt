package com.ilustris.sagai.features.chapter.data.usecase

import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ChapterUseCaseImpl
    @Inject
    constructor(
        private val chapterRepository: ChapterRepository,
        private val timelineRepository: TimelineRepository,
        private val wikiUseCase: WikiUseCase,
        private val gemmaClient: GemmaClient,
        private val imagenClient: ImagenClient,
        private val fileHelper: FileHelper,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val remoteConfigService: RemoteConfigService,
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
                    .generate<Chapter>(
                        prompt =
                            generateChapterPrompt(
                                saga = saga,
                                currentChapter = chapterContent,
                            ),
                        filterOutputFields =
                            listOf(
                                "id",
                                "currentEventId",
                                "coverImage",
                                "createdAt",
                                "actId",
                            ),
                        requireTranslation = true,
                        useCore = true,
                        requirement = GemmaClient.ModelRequirement.HIGH,
                        blueprintKey = ChapterPrompts.CHAPTER_GENERATION_BLUEPRINT,
                    )!!

            val updatedChapter =
                updateChapter(
                    chapterContent.data.copy(
                        title = genChapter.title,
                        overview = genChapter.overview,
                        introduction = chapterContent.data.introduction, // Keep existing introduction
                        featuredCharacters = genChapter.featuredCharacters.take(2),
                        emotionalReview = genChapter.emotionalReview,
                        currentEventId = null,
                    ),
                )
            CoroutineScope(Dispatchers.IO).launch {
                generateChapterCover(chapterContent.copy(data = updatedChapter), saga)
            }
            updatedChapter
        }

        override suspend fun generateChapterStream(
            saga: SagaContent,
            chapterContent: ChapterContent,
        ): Flow<StreamingState<GeneratedContent<Chapter>>> =
            flow {
                try {
                    gemmaClient
                        .generateStreaming<GeneratedContent<Chapter>>(
                            prompt =
                                generateChapterPrompt(
                                    saga = saga,
                                    currentChapter = chapterContent,
                                ),
                            filterOutputFields =
                                listOf(
                                    "id",
                                    "currentEventId",
                                    "coverImage",
                                    "createdAt",
                                    "actId",
                                ),
                            requireTranslation = true,
                            useCore = true,
                            requirement = GemmaClient.ModelRequirement.HIGH,
                            blueprintKey = ChapterPrompts.CHAPTER_GENERATION_BLUEPRINT,
                        ).collect { state ->
                            if (state is StreamingState.Success) {
                                val genChapter = state.data.data
                                val updatedChapter =
                                    updateChapter(
                                        chapterContent.data.copy(
                                            title = genChapter.title,
                                            overview = genChapter.overview,
                                            introduction = chapterContent.data.introduction, // Keep existing introduction
                                            featuredCharacters =
                                                genChapter.featuredCharacters.take(
                                                    2,
                                                ),
                                            emotionalReview = genChapter.emotionalReview,
                                            currentEventId = null,
                                        ),
                                    )
                                CoroutineScope(Dispatchers.IO).launch {
                                    generateChapterCover(
                                        chapterContent.copy(data = updatedChapter),
                                        saga,
                                )
                            }
                                emit(
                                    StreamingState.Success(
                                        GeneratedContent(
                                            updatedChapter,
                                            state.data.finalMessage,
                                        ),
                                    ),
                                )
                            } else {
                            emit(state)
                        }
                    }
            } catch (e: Exception) {
                emit(StreamingState.Error(e.message ?: "Unknown error"))
            }
        }

        override suspend fun reviewChapter(
            saga: SagaContent,
            chapterContent: ChapterContent,
        ) = executeRequest {
            cleanUpEmptyTimeLines(chapterContent)
            val chapterWikis = chapterContent.events.map { it.updatedWikis }.flatten()
            wikiUseCase.mergeWikis(saga, chapterWikis)

            generateChapter(
                saga = saga,
                chapterContent = chapterContent,
            ).getSuccess()!!
        }

        private suspend fun cleanUpEmptyTimeLines(chapter: ChapterContent) {
            val rules = remoteConfigService.getJson<NarrativeRules>("narrative_rules")!!
            val emptyEvents = chapter.events.filter { it.isComplete(rules).not() }.map { it.data }
            if (emptyEvents.isEmpty()) {
                Timber.w("cleanUpEmptyTimeLines: No timelines to clean up")
                return
            }
            emptyEvents.forEach { timeline ->
                timelineRepository.deleteTimeline(timeline)
            }
            Timber.w("cleanUpEmptyTimeLines: Removed ${emptyEvents.size} timelines")
        }

        @OptIn(PublicPreviewAPI::class)
        override suspend fun generateChapterCover(
            chapter: ChapterContent,
            saga: SagaContent,
        ): RequestResult<Chapter> =
            executeRequest {
                val characters =
                    chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter!!) }

                val genCover =
                    imagenClient
                        .generateIntegratedImage(
                            genre = saga.data.genre,
                            imageReference = null,
                            context = buildCoverPromptContext(characters, saga),
                            imageType = ImageType.COVER,
                            variationId = saga.data.variationId,
                        )

                if (genCover.isFailure) {
                    throw genCover.error.value
                }

                val coverFile =
                    fileHelper.saveFile(
                        chapter.data.title,
                        genCover.getSuccess(),
                        path = "${saga.data.id}/chapters/",
                    )!!
                val newChapter =
                    chapter.data.copy(
                        coverImage = coverFile.path,
                    )

                chapterRepository.updateChapter(newChapter)
            }

        @OptIn(PublicPreviewAPI::class)
        override suspend fun generateChapterCoverStream(
            chapter: ChapterContent,
            saga: SagaContent,
        ): Flow<StreamingState<GeneratedContent<Chapter>>> =
            flow {
                try {
                    val characters =
                        chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter!!) }

                    imagenClient
                        .generateIntegratedImageStream(
                            genre = saga.data.genre,
                            imageReference = null,
                            context = buildCoverPromptContext(characters, saga),
                            imageType = ImageType.COVER,
                            variationId = saga.data.variationId,
                        ).collect { state ->
                            if (state is StreamingState.Success) {
                                val bitmap = state.data.data
                                val coverFile =
                                    fileHelper.saveFile(
                                        chapter.data.title,
                                        bitmap,
                                        path = "${saga.data.id}/chapters/",
                                    ) ?: error("Failed to save chapter cover")

                                val newChapter = chapter.data.copy(coverImage = coverFile.path)
                                val updated = chapterRepository.updateChapter(newChapter)
                                emit(
                                    StreamingState.Success(
                                        GeneratedContent(
                                            updated,
                                            state.data.finalMessage,
                                        ),
                                    ),
                                )
                            } else {
                                emit(state as StreamingState<GeneratedContent<Chapter>>)
                            }
                        }
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Error generating chapter cover stream"))
                }
            }

        private fun buildCoverPromptContext(
            characters: List<CharacterContent?>,
            saga: SagaContent,
        ): String =
            buildString {
                val duo = characters.filterNotNull().take(2)
                appendLine("### THE ARTBOOK DUO")
                appendLine("This is a focused character study. Capture the intimate tension and presence of exactly these two individuals.")
                append("[")
                append(
                    duo.joinToString {
                        buildString {
                            appendLine(it.data.fullName())
                            appendLine(it.data.profile.toAINormalize())
                            appendLine(
                                it.data.details.physicalTraits
                                    .toAINormalize(),
                            )
                            appendLine(
                                it.data.details.clothing
                                    .toAINormalize(),
                            )
                        }
                    },
                )
                append("]")

                if (duo.size > 1) {
                    appendLine()
                    appendLine("#### CORE RELATIONSHIP (COMPOSITIONAL FOCUS):")
                    val char1 = duo[0]
                    val char2 = duo[1]
                    appendLine(
                        "• ${char1.data.name} & ${char2.data.name}: ${
                            char1.findRelationship(char2.data.id)?.summarizeRelation(1)
                            ?: "Complex dynamic connection."
                    }",
                )
            }

            appendLine()
            appendLine(
                "FINAL ARTBOOK MANDATE: Prioritize the emotional interaction or shared silence between these subjects. No crowded compositions. Focus on character fidelity and the art style.",
            )
        }

        private suspend fun generateChapterPrompt(
            saga: SagaContent,
            currentChapter: ChapterContent,
        ): String {
            val conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre)
            return ChapterPrompts.chapterGeneration(
                promptService,
                saga,
                currentChapter,
                remoteConfigService.getNarrativeRules(),
                conversationDirective,
            )
        }

        override suspend fun generateChapterIntroduction(
            saga: SagaContent,
            chapter: Chapter,
            act: ActContent,
        ) = executeRequest {
            genreConfigService.getGenreConfig(saga.data.genre)
            val prompt =
                ChapterPrompts.chapterIntroductionPrompt(
                    promptService = promptService,
                    sagaContent = saga,
                    currentChapter = chapter,
                    conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                )
            val intro =
                gemmaClient.generate<GeneratedContent<String>>(
                    prompt,
                    requireTranslation = true,
                    useCore = true,
                    requirement = GemmaClient.ModelRequirement.HIGH,
                    blueprintKey = ChapterPrompts.CHAPTER_INTRODUCTION_BLUEPRINT,
                )!!
            val updated = chapter.copy(introduction = intro.data)
            val updatedChapter = chapterRepository.updateChapter(updated)
            GeneratedContent(updatedChapter, intro.finalMessage)
        }

        override suspend fun generateChapterIntroductionStream(
            saga: SagaContent,
            chapterContent: Chapter,
            act: ActContent,
    ): Flow<StreamingState<GeneratedContent<Chapter>>> =
        flow {
                try {
                    val prompt =
                        ChapterPrompts.chapterIntroductionPrompt(
                            promptService = promptService,
                            sagaContent = saga,
                        currentChapter = chapterContent,
                        conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                        )

                    gemmaClient
                        .generateStreaming<GeneratedContent<String>>(
                        prompt = prompt,
                        requireTranslation = true,
                        useCore = true,
                        requirement = GemmaClient.ModelRequirement.HIGH,
                        blueprintKey = ChapterPrompts.CHAPTER_INTRODUCTION_BLUEPRINT,
                        ).collect { state ->
                            if (state is StreamingState.Success) {
                                val introContent = state.data
                                val updatedChapter =
                                    updateChapter(chapterContent.copy(introduction = introContent.data))
                                emit(
                                    StreamingState.Success(
                                        GeneratedContent(
                                            updatedChapter,
                                            introContent.finalMessage,
                                        ),
                                    ),
                                )
                        } else {
                            emit(state as StreamingState<GeneratedContent<Chapter>>)
                        }
                    }
            } catch (e: Exception) {
                emit(StreamingState.Error(e.message ?: "Unknown error"))
            }
        }
    }
