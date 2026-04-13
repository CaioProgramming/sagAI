package com.ilustris.sagai.features.chapter.data.usecase

import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
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
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
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
                    )!!

            updateChapter(
                chapterContent.data.copy(
                    title = genChapter.title,
                    overview = genChapter.overview,
                    introduction = genChapter.introduction,
                    featuredCharacters = genChapter.featuredCharacters.take(3),
                    emotionalReview = genChapter.emotionalReview,
                    currentEventId = null,
                ),
            )
        }

        override fun generateChapterStream(
            saga: SagaContent,
            chapterContent: ChapterContent,
        ) = kotlinx.coroutines.flow.flow {
            try {
                gemmaClient
                    .generateStreaming<com.ilustris.sagai.core.ai.model.GeneratedContent<Chapter>>(
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
                    ).collect { state ->
                        when (state) {
                            is com.ilustris.sagai.core.ai.StreamingState.Success -> {
                                val genChapter = state.data.data
                                val updatedChapter =
                                    updateChapter(
                                        chapterContent.data.copy(
                                            title = genChapter.title,
                                            overview = genChapter.overview,
                                            introduction = genChapter.introduction,
                                            featuredCharacters = genChapter.featuredCharacters.take(3),
                                            emotionalReview = genChapter.emotionalReview,
                                            currentEventId = null,
                                        ),
                                    )
                                emit(
                                    com.ilustris.sagai.core.ai.StreamingState.Success(
                                        com.ilustris.sagai.core.ai.model.GeneratedContent(
                                            updatedChapter,
                                            state.data.finalMessage,
                                        ),
                                    ),
                                )
                            }

                            is com.ilustris.sagai.core.ai.StreamingState.Error -> {
                                emit(
                                    com.ilustris.sagai.core.ai.StreamingState.Error(
                            state.message,
                        ),
                    )
                    }

                    is com.ilustris.sagai.core.ai.StreamingState.Reasoning -> {
                        emit(
                        com.ilustris.sagai.core.ai.StreamingState.Reasoning(
                            state.chunk,
                        ),
                    )
                    }
                }
            }
        } catch (e: Exception) {
            emit(com.ilustris.sagai.core.ai.StreamingState.Error(e.message ?: "Unknown error"))
        }
    }

        override suspend fun reviewChapter(
            saga: SagaContent,
            chapterContent: ChapterContent,
        ) = executeRequest {
            cleanUpEmptyTimeLines(chapterContent)
            val chapterWikis = chapterContent.events.map { it.updatedWikis }.flatten()
            if (chapterWikis.size > 10) {
                wikiUseCase.mergeWikis(saga, chapterWikis)
            }

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
                            context =
                                buildString {
                                    appendLine("### MANDATORY MULTI-CHARACTER SCENE")
                                    appendLine("The following characters are ESSENTIAL to this scene.")
                                    append("[")
                                    append(characters.filterNotNull().joinToString { it.data.name })
                                    append("]")
                                    appendLine("You MUST integrate ALL of them into the artwork.")
                                    appendLine("Strictly respect their descriptions, physical traits, and relative positions.")
                                    appendLine()
                                    appendLine("#### SUBJECTS DETAILS:")
                                    appendLine(
                                        characters.mapNotNull { it?.data }.toAINormalize(
                                            listOf(
                                                "id",
                                                "image",
                                                "sagaId",
                                                "joinedAt",
                                                "emojified",
                                                "hexColor",
                                                "firstSceneId",
                                                "carriedItems",
                                                "smartZoom",
                                                "knowledge",
                                                "nicknames",
                                            ),
                                        ),
                                    )
                                    appendLine()
                                    appendLine("#### CHARACTER RELATIONSHIPS (COMPOSITIONAL HINTS):")
                                    characters
                                        .filter {
                                            it?.data?.id != saga.mainCharacter?.data?.id
                                        }.forEach {
                                            val character = it ?: return@forEach
                                            appendLine(
                                                "• ${saga.mainCharacter?.data?.name} & ${character.data.name}: ${
                                                    saga.mainCharacter
                                                        ?.findRelationship(character.data.id)
                                                        ?.summarizeRelation(1)
                                                }",
                                            )
                                        }
                                    appendLine()
                                    appendLine(
                                        "FINAL MANDATE: Do not focus on just one character. Balance the composition to show the interaction and presence of EVERY subject listed.",
                                    )
                                },
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
        ): RequestResult<Chapter> =
            executeRequest {
                genreConfigService.getGenreConfig(saga.data.genre)
                val prompt =
                    ChapterPrompts.chapterIntroductionPrompt(
                        promptService = promptService,
                        sagaContent = saga,
                        currentChapter = chapter,
                        conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                    )
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
