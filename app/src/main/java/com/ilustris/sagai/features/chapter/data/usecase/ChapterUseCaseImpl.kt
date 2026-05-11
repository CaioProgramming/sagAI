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
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.UnifiedChapterUpdate
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.characters.data.model.ArcSourceType
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        private val characterUseCase: CharacterUseCase,
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        private val imagenClient: ImagenClient,
        private val fileHelper: FileHelper,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val remoteConfigService: RemoteConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
        private val actRepository: com.ilustris.sagai.features.act.data.repository.ActRepository,
    ) : ChapterUseCase {
        private suspend fun fetchContext(chapterId: Int): Pair<SagaContent, ChapterContent> {
            val chapterContent =
                chapterRepository.getChapterContentById(chapterId) ?: error("Chapter not found")
            val act = actRepository.getActById(chapterContent.data.actId) ?: error("Act not found")
            val saga = sagaRepository.getSagaById(act.sagaId ?: 0).first() ?: error("Saga not found")
            return saga to chapterContent
        }

        override suspend fun saveChapter(chapter: Chapter): Chapter = chapterRepository.saveChapter(chapter)

        override suspend fun deleteChapter(chapter: Chapter) = chapterRepository.deleteChapter(chapter)

        override suspend fun updateChapter(chapter: Chapter) = chapterRepository.updateChapter(chapter)

        override suspend fun deleteChapterById(chapterId: Int) = chapterRepository.deleteChapterById(chapterId)

        override suspend fun deleteAllChapters() = chapterRepository.deleteAllChapters()

        override fun getChaptersInfoBySaga(sagaId: Int) = chapterRepository.getChaptersInfoBySaga(sagaId)

        override suspend fun generateChapter(chapterId: Int) =
            executeRequest {
                val (saga, chapterContent) = fetchContext(chapterId)
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
                    generateChapterCover(chapterId)
                }
                updatedChapter
            }

        override suspend fun generateChapterStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>>> =
            flow {
                try {
                    val (saga, chapterContent) = fetchContext(chapterId)
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
                                        genChapter.copy(
                                            id = chapterContent.data.id,
                                            currentEventId = chapterContent.data.currentEventId,
                                            actId = chapterContent.data.actId,
                                            coverImage = chapterContent.data.coverImage,
                                            introduction = chapterContent.data.introduction,
                                            createdAt = chapterContent.data.createdAt,
                                        ),
                                    )
                                CoroutineScope(Dispatchers.IO).launch {
                                    generateChapterCover(updatedChapter.id)
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

        override suspend fun reviewChapter(chapterId: Int) =
            executeRequest {
                val (saga, chapterContent) = fetchContext(chapterId)
                cleanUpEmptyTimeLines(chapterContent)
                val chapterWikis = chapterContent.events.map { it.updatedWikis }.flatten()
                wikiUseCase.mergeWikis(saga, chapterWikis)

                generateChapter(
                    chapterId = chapterId,
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
        override suspend fun generateChapterCover(chapterId: Int): RequestResult<Chapter> =
            executeRequest {
                val (saga, chapter) = fetchContext(chapterId)
                val characters =
                    chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter!!) }

                val genCover =
                    imagenClient
                        .generateIntegratedImage(
                            genre = saga.data.genre,
                            imageReference = null,
                            context =
                                buildCoverPromptContext(
                                    chapter.data.narrativeGuide,
                                    characters,
                                    saga,
                                ),
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
        override suspend fun generateChapterCoverStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>>> =
            flow {
                try {
                    val (saga, chapter) = fetchContext(chapterId)
                    val characters =
                        chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter!!) }

                    imagenClient
                        .generateIntegratedImageStream(
                            genre = saga.data.genre,
                            imageReference = null,
                            context =
                                buildCoverPromptContext(
                                    chapter.data.narrativeGuide,
                                    characters,
                                    saga,
                                ),
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
            narrativeContext: String?,
            characters: List<CharacterContent?>,
            saga: SagaContent,
        ): String =
            buildString {
                val duo = characters.filterNotNull().take(2)
                appendLine("### THE ARTBOOK DUO")
                appendLine("This is a focused character study. Capture the intimate tension and presence of exactly these two individuals.")
                append("[")
                appendLine(
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

                narrativeContext?.let {
                    appendLine("Narrative moment: ")
                    appendLine(narrativeContext)
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
            sagaId: Int,
            chapterContent: Chapter,
        ) = executeRequest {
            val saga = sagaRepository.getSagaById(sagaId).first() ?: error("Saga not found")
            genreConfigService.getGenreConfig(saga.data.genre)
            val prompt =
                ChapterPrompts.chapterIntroductionPrompt(
                    promptService = promptService,
                    sagaContent = saga,
                    currentChapter = chapterContent,
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
            val updated = chapterContent.copy(introduction = intro.data)
            val updatedChapter = chapterRepository.updateChapter(updated)
            GeneratedContent(updatedChapter, intro.finalMessage)
        }

        override suspend fun generateChapterIntroductionStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>>> =
            flow {
                try {
                    val (saga, chapter) = fetchContext(chapterId)
                    val chapterContent = chapter.data
                    val prompt =
                        ChapterPrompts.chapterIntroductionPrompt(
                            promptService = promptService,
                            sagaContent = saga,
                            currentChapter = chapterContent,
                            conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                        )

                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            gemmaClient
                                .generateStreaming<GeneratedContent<String>>(
                                    prompt = prompt,
                                    requireTranslation = true,
                                    useCore = true,
                                    requirement = GemmaClient.ModelRequirement.HIGH,
                                    blueprintKey = ChapterPrompts.CHAPTER_INTRODUCTION_BLUEPRINT,
                                ),
                            "Generating chapter introduction...",
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

        override fun synthesizeChapterEvolutionStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>>> =
            flow {
                try {
                    val (saga, chapterContent) = fetchContext(chapterId)
                    val conversationDirective =
                        genreConfigService.conversationBlueprint(saga.data.genre)
                    val prompt =
                        ChapterPrompts.chapterSynthesisPrompt(
                            promptService = promptService,
                            saga = saga,
                            chapter = chapterContent,
                            narrativeRules = remoteConfigService.getNarrativeRules(),
                            conversationDirective = conversationDirective,
                        )

                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            gemmaClient
                                .generateStreaming<GeneratedContent<UnifiedChapterUpdate>>(
                                    prompt = prompt,
                                    blueprintKey = ChapterPrompts.CHAPTER_SYNTHESIS_BLUEPRINT,
                                    requirement = GemmaClient.ModelRequirement.HIGH,
                                ),
                            "Generating new chapter...",
                        ).collect { state ->
                            when (state) {
                                is StreamingState.Success -> {
                                    val synthesis = state.data.data

                                    // 1. Update Chapter details & Narrative Guide
                                    val updatedChapter =
                                        updateChapter(
                                            synthesis.chapter.copy(
                                                id = chapterContent.data.id,
                                                currentEventId = chapterContent.data.currentEventId,
                                                actId = chapterContent.data.actId,
                                                coverImage = chapterContent.data.coverImage,
                                                createdAt = chapterContent.data.createdAt,
                                            ),
                                        )

                                    // 2. Save Landmark Wikis
                                    synthesis.landmarkWikis.forEach { wikiUpdate ->
                                        val existingWiki =
                                            saga.wikis.find { it.title.equals(wikiUpdate.title, true) }
                                        val wikiToSave =
                                            Wiki(
                                                id = existingWiki?.id ?: 0,
                                                title = wikiUpdate.title,
                                                content = wikiUpdate.content,
                                                type = wikiUpdate.type,
                                                emojiTag = wikiUpdate.emojiTag,
                                                sagaId = saga.data.id,
                                                chapterId = chapterContent.data.id,
                                                isFeatured = true,
                                            )
                                        if (existingWiki != null) {
                                            wikiUseCase.updateWiki(wikiToSave)
                                        } else {
                                            wikiUseCase.saveWiki(wikiToSave)
                                        }
                                    }

                                    // 3. Save Character Arcs
                                    synthesis.characterArcs.forEach { arcUpdate ->
                                        val character = saga.findCharacter(arcUpdate.characterName)
                                        character?.let {
                                            characterUseCase.insertCharacterArc(
                                                CharacterArc(
                                                    characterId = it.data.id,
                                                    sourceId = chapterContent.data.id,
                                                    sourceType = ArcSourceType.CHAPTER,
                                                    title = arcUpdate.arcTitle,
                                                    content = arcUpdate.arcContent,
                                                ),
                                            )
                                        }
                                    }

                                    // 4. Update Global World State
                                    synthesis.worldStateUpdate?.let {
                                        sagaRepository.updateSaga(saga.data.copy(worldState = it))
                                    }

                                    emit(
                                        StreamingState.Success(
                                            GeneratedContent(
                                                updatedChapter,
                                                state.data.finalMessage,
                                            ),
                                        ),
                                    )
                                }

                                is StreamingState.Error -> {
                                    emit(StreamingState.Error(state.message, state.throwable))
                                }

                                is StreamingState.Reasoning -> {
                                    emit(StreamingState.Reasoning(state.chunk))
                                }
                            }
                        }
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Chapter synthesis failed", e))
                }
            }
    }
