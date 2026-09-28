package com.ilustris.sagai.features.chapter.data.usecase

import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.model.GeneratedContentWithLore
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts
import com.ilustris.sagai.core.ai.prompts.PlayerSpectrumPrompts
import com.ilustris.sagai.core.ai.services.ArtworkConceptService
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChoiceOption
import com.ilustris.sagai.features.chapter.data.model.GeneratedChoiceCard
import com.ilustris.sagai.features.chapter.data.model.GeneratedPlayerChoices
import com.ilustris.sagai.features.chapter.data.model.UnifiedChapterUpdate
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.characters.data.model.ArcSourceType
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.geography.data.usecase.WorldLocationUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getDirectiveKey
import com.ilustris.sagai.features.imagegeneration.ImageGenerationService
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationRequest
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
        private val imageGenerationService: ImageGenerationService,
        private val fileHelper: FileHelper,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val remoteConfigService: RemoteConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
        private val actRepository: com.ilustris.sagai.features.act.data.repository.ActRepository,
        private val artworkConceptService: ArtworkConceptService,
        private val worldLocationUseCase: WorldLocationUseCase,
        private val semanticIndexService: com.ilustris.sagai.core.ai.rag.SemanticIndexService,
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

        override suspend fun updateChapter(chapter: Chapter) =
            chapterRepository.updateChapter(chapter).also { updated ->
                updated.continuitySummary?.let { summary ->
                    // Chapter carries no sagaId of its own — resolved through its act, same as
                    // fetchContext() above.
                    val sagaId = actRepository.getActById(updated.actId)?.sagaId ?: return@also
                    semanticIndexService.indexContinuitySummary(sagaId, "chapter:${updated.id}:continuity", summary)
                }
            }

        override suspend fun deleteChapterById(chapterId: Int) = chapterRepository.deleteChapterById(chapterId)

        override suspend fun deleteAllChapters() = chapterRepository.deleteAllChapters()

        override fun getChaptersInfoBySaga(sagaId: Int) = chapterRepository.getChaptersInfoBySaga(sagaId)

        override suspend fun generateChapter(chapterId: Int) =
            executeRequest {
                val (saga, chapterContent) = fetchContext(chapterId)
                val prompt =
                    generateChapterPrompt(
                        saga = saga,
                        currentChapter = chapterContent,
                    )
                val genChapter =
                    gemmaClient
                        .generate<Chapter>(
                            promptSplit =
                                prompt.mergeInstructions(
                                    genreConfigService.conversationInstructions(saga.data.genre),
                                    artworkConceptService.artworkInstructions(ImageType.COVER),
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
                            requirement = ModelRequirement.HIGH,
                        )!!

                val updatedChapter =
                    updateChapter(
                        chapterContent.data.copy(
                            title = genChapter.title,
                            content = genChapter.content,
                            introduction = chapterContent.data.introduction, // Keep existing introduction
                            featuredCharacters = genChapter.featuredCharacters.take(2),
                            emotionalReview = genChapter.emotionalReview,
                            artwork = genChapter.artwork ?: chapterContent.data.artwork,
                            currentEventId = null,
                        ),
                    )
                CoroutineScope(Dispatchers.IO).launch {
                    generateChapterCover(chapterId)
                }
                updatedChapter
            }

        override suspend fun generateChapterStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>?>> =
            flow {
                try {
                    val (saga, chapterContent) = fetchContext(chapterId)
                    val prompt =
                        generateChapterPrompt(
                            saga = saga,
                            currentChapter = chapterContent,
                        )
                    gemmaClient
                        .generateStreaming<GeneratedContent<Chapter>>(
                            promptSplit =
                                prompt.mergeInstructions(
                                    genreConfigService.conversationInstructions(saga.data.genre),
                                    artworkConceptService.artworkInstructions(ImageType.COVER),
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
                            requirement = ModelRequirement.HIGH,
                        ).collect { state ->
                            if (state is StreamingState.Success) {
                                val genChapter = state.data!!.data
                                val updatedChapter =
                                    updateChapter(
                                        genChapter.copy(
                                            id = chapterContent.data.id,
                                            currentEventId = chapterContent.data.currentEventId,
                                            actId = chapterContent.data.actId,
                                            coverImage = chapterContent.data.coverImage,
                                            introduction = chapterContent.data.introduction,
                                            createdAt = chapterContent.data.createdAt,
                                            artwork =
                                                genChapter.artwork
                                                    ?: chapterContent.data.artwork,
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
                val metadata =
                    sagaRepository.getSagaMetadata(saga.data.id).first()
                        ?: error("Metadata not found")
                wikiUseCase.mergeWikis(metadata, chapterWikis)

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
                // Now also kicked off automatically as soon as the chapter synthesis finishes
                // (see SagaContentManagerImpl's NarrativeAction.GenerateChapter handling), instead
                // of only from the Milestone screen's "continue" tap — this call stays as a
                // fallback for that tap, so the guard keeps it from re-enqueuing (and re-billing)
                // a cover the background trigger already generated.
                if (chapter.data.coverImage.isNotBlank()) return@executeRequest chapter.data
                val chapterWithArtwork = ensureChapterArtwork(chapter.data, saga.data)
                val characters =
                    chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter!!) }
                val context =
                    buildCoverPromptContext(
                        chapterWithArtwork.narrativeGuide,
                        chapterWithArtwork.artwork,
                        characters,
                        saga,
                    )

                imageGenerationService
                    .enqueue(
                        ImageGenerationRequest(
                            genre = saga.data.genre,
                            imageReference = null,
                            context = context,
                            imageType = ImageType.COVER,
                            variationId = saga.data.variationId,
                            label = chapterWithArtwork.title,
                            showReveal = true,
                        ),
                    ) { bitmap ->
                        val coverFile =
                            fileHelper.saveFile(
                                chapterWithArtwork.title,
                                bitmap,
                                path = "${saga.data.id}/chapters/",
                            ) ?: error("Failed to save chapter cover")
                        persistCover(chapterWithArtwork, coverFile.path)
                    }.getOrThrow()
            }

        @OptIn(PublicPreviewAPI::class)
        override suspend fun generateChapterCoverStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>>> =
            flow {
                try {
                    val (saga, chapter) = fetchContext(chapterId)
                    val chapterWithArtwork = ensureChapterArtwork(chapter.data, saga.data)
                    val characters =
                        chapter.fetchCharacters(saga).ifEmpty { listOf(saga.mainCharacter!!) }
                    val context =
                        buildCoverPromptContext(
                            chapterWithArtwork.narrativeGuide,
                            chapterWithArtwork.artwork,
                            characters,
                            saga,
                        )

                    imageGenerationService
                        .enqueue(
                            ImageGenerationRequest(
                                genre = saga.data.genre,
                                imageReference = null,
                                context = context,
                                imageType = ImageType.COVER,
                                variationId = saga.data.variationId,
                                label = chapterWithArtwork.title,
                                showReveal = true,
                            ),
                        ) { bitmap ->
                            val coverFile =
                                fileHelper.saveFile(
                                    chapterWithArtwork.title,
                                    bitmap,
                                    path = "${saga.data.id}/chapters/",
                                ) ?: error("Failed to save chapter cover")
                            persistCover(chapterWithArtwork, coverFile.path)
                        }.fold(
                            onSuccess = { updated ->
                                emit(
                                    StreamingState.Success(
                                        GeneratedContent(
                                            updated,
                                            "Image generation complete!",
                                        ),
                                    ),
                                )
                            },
                            onFailure = { error ->
                                emit(
                                    StreamingState.Error(
                                        error.message ?: "Error generating chapter cover stream",
                                        error,
                                    ),
                                )
                            },
                        )
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Error generating chapter cover stream"))
                }
            }

        /**
         * Cover generation runs for a long while off a snapshot of the chapter, so writing that
         * snapshot back would erase whatever changed meanwhile (e.g. the player's spectrum answers).
         * Only the cover is layered onto the freshest row.
         */
        private suspend fun persistCover(
            snapshot: Chapter,
            coverPath: String,
        ): Chapter {
            val latest = chapterRepository.getChapterById(snapshot.id) ?: snapshot
            return chapterRepository.updateChapter(
                latest.copy(coverImage = coverPath, artwork = latest.artwork ?: snapshot.artwork),
            )
        }

        private suspend fun ensureChapterArtwork(
            chapter: Chapter,
            saga: Saga,
        ): Chapter {
            if (!chapter.artwork.isNullOrBlank()) return chapter
            val artwork =
                artworkConceptService
                    .ensureArtwork(
                        imageType = ImageType.COVER,
                        contentType = "Chapter",
                        genre = saga.genre,
                        context = chapterArtworkContext(chapter),
                        currentArtwork = chapter.artwork,
                    ).onFailure {
                        Timber.w(
                            it,
                            "ensureChapterArtwork: failed to generate artwork for chapter ${chapter.id}",
                        )
                    }.getSuccess()
                    ?.takeIf { it.isNotBlank() }
                    ?: return chapter
            val updatedChapter = chapter.copy(artwork = artwork)
            chapterRepository.updateChapter(updatedChapter)
            return updatedChapter
        }

        private fun chapterArtworkContext(chapter: Chapter): String =
            buildString {
                appendLine(chapter.content)
                chapter.emotionalReview?.takeIf { it.isNotBlank() }?.let {
                appendLine()
                appendLine("EMOTIONAL ARC: $it")
            }
        }

        private fun buildCoverPromptContext(
            narrativeContext: String?,
            artwork: String?,
            characters: List<CharacterContent?>,
            saga: SagaContent,
        ): String =
            buildString {
                artwork?.takeIf { it.isNotBlank() }?.let {
                    appendLine("### CONCEPT ART DIRECTION")
                    appendLine(it)
                    appendLine()
                }

                val duo = characters.filterNotNull().take(2)
                appendLine("### CHARACTERS")
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
                    appendLine("### RELATIONSHIP")
                    val char1 = duo[0]
                    val char2 = duo[1]
                    appendLine(
                        "${char1.data.name} & ${char2.data.name}: ${
                            char1.findRelationship(char2.data.id)?.summarizeRelation(1)
                                ?: "Complex dynamic connection."
                        }",
                    )
                }

                narrativeContext?.let {
                    appendLine()
                    appendLine("### NARRATIVE MOMENT")
                    appendLine(it)
                }
            }

        private suspend fun generateChapterPrompt(
            saga: SagaContent,
            currentChapter: ChapterContent,
        ): SplitPrompt =
            ChapterPrompts.chapterGeneration(
                promptService,
                saga,
                currentChapter,
                remoteConfigService.getNarrativeRules(),
                emptyString(),
            )

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
                    narrativeRules = remoteConfigService.getNarrativeRules(),
                )
            val intro =
                gemmaClient.generate<GeneratedContent<String>>(
                    promptSplit =
                        prompt.mergeInstructions(
                            genreConfigService.conversationInstructions(saga.data.genre),
                        ),
                    requireTranslation = true,
                    requirement = ModelRequirement.HIGH,
                )!!
            val updated = chapterContent.copy(introduction = intro.data)
            val updatedChapter = chapterRepository.updateChapter(updated)
            GeneratedContent(updatedChapter, (intro.finalMessage as String?).orEmpty())
        }

        override suspend fun generateChapterIntroductionStream(chapterId: Int): Flow<StreamingState<GeneratedContent<Chapter>?>> =
            flow {
                try {
                    val (saga, chapter) = fetchContext(chapterId)
                    val chapterContent = chapter.data
                    val prompt =
                        ChapterPrompts.chapterIntroductionPrompt(
                            promptService = promptService,
                            sagaContent = saga,
                            remoteConfigService.getNarrativeRules(),
                        )

                    // Deliberately the sync call, not generateStreaming — see MessageUseCaseImpl's
                    // own generateMessage for why: the reply is one JSON object, so streaming
                    // never rendered partial text, and reasoningSynthesizerService.synthesizeReasoning
                    // already ignores the source flow's real Reasoning states in favor of its own
                    // loading-line rotation (the whole reason this used to stream — summarising
                    // the model's live thought stream — stopped applying once that moved to a
                    // synthesized pool instead). Wrapped in a single-emission flow purely so the
                    // synthesizer keeps working the same way for every other caller.
                    val generateFlow =
                        flow {
                            val intro =
                                gemmaClient.generate<GeneratedContent<String>>(
                                    promptSplit =
                                        prompt.mergeInstructions(
                                            genreConfigService.conversationInstructions(saga.data.genre),
                                            // First thing written after the player answers the
                                            // previous chapter's cards — carry that read in.
                                            PlayerSpectrumPrompts.lensInstructions(
                                                PlayerSpectrumPrompts.latestSpectrum(saga),
                                            ),
                                        ),
                                    requireTranslation = true,
                                    requirement = ModelRequirement.HIGH,
                                )
                            if (intro == null) {
                                emit(
                                    StreamingState.Error(
                                        message = "Chapter introduction generation returned no result",
                                        throwable = IllegalStateException("Chapter introduction generation failed"),
                                    ),
                                )
                            } else {
                                emit(StreamingState.Success(intro))
                            }
                        }
                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            generateFlow,
                            "Generating chapter introduction...",
                            genre = saga.data.genre,
                        ).collect { state ->
                            when (state) {
                                is StreamingState.Success -> {
                                    val introContent = state.data!!
                                    val updatedChapter =
                                        updateChapter(chapterContent.copy(introduction = introContent.data))
                                    emit(
                                        StreamingState.Success(
                                            GeneratedContent(
                                                updatedChapter,
                                                // Gson leaves a field the model omitted as null despite the
                                                // non-null type; passing that on NPEs *after* the intro
                                                // was already saved, failing an action that succeeded.
                                                (introContent.finalMessage as String?).orEmpty(),
                                            ),
                                        ),
                                    )
                                }

                                is StreamingState.Reasoning -> {
                                    emit(state)
                                }

                                is StreamingState.Error -> {
                                    emit(state)
                                }
                            }
                        }
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Unknown error"))
                }
            }

        override suspend fun recordPlayerChoiceAnswers(
            chapterId: Int,
            answers: List<String>,
        ): RequestResult<Chapter> =
            executeRequest {
                val (saga, chapterContent) = fetchContext(chapterId)
                val chapter = chapterContent.data
                val cards = chapter.playerChoiceCards.orEmpty()
                require(cards.isNotEmpty() && answers.size == cards.size) {
                    "Expected ${cards.size} answers for chapter $chapterId, got ${answers.size}"
                }

                // The answers land first, on their own: they're what moves the chain on to the
                // chapter synthesis (NarrativeCheck stops asking for cards once they exist), so a
                // failed rewrite below must never leave the player re-answering the same cards.
                val answered = chapterRepository.updateChapter(chapter.copy(playerChoiceAnswers = answers))

                // Re-answering a chapter reassesses its own read; otherwise it inherits the last one.
                val previousSpectrum =
                    chapter.playerSpectrum?.takeIf { it.isNotBlank() }
                        ?: PlayerSpectrumPrompts.previousSpectrum(saga, chapterId)

                val spectrum =
                    try {
                        val prompt =
                            ChapterPrompts.playerSpectrumRewritePrompt(
                                promptService = promptService,
                                chapter = chapterContent,
                                previousSpectrum = previousSpectrum,
                                cards = cards,
                                answers = answers,
                            )
                        gemmaClient
                            .generate<GeneratedContent<String>>(
                                promptSplit = prompt,
                                requireTranslation = false,
                                requirement = ModelRequirement.LOW,
                            )?.let { it.data as String? }
                            ?.takeIf { it.isNotBlank() }
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        Timber.e(e, "Player spectrum rewrite failed for chapter $chapterId")
                        null
                    } ?: return@executeRequest answered

                chapterRepository.updateChapter(answered.copy(playerSpectrum = spectrum))
            }

        override fun generateChoiceCardsStream(chapterId: Int): Flow<StreamingState<ChoiceCardsReveal?>> =
            flow {
                try {
                    val (saga, chapterContent) = fetchContext(chapterId)
                    val chapter = chapterContent.data
                    // Already attempted (a resume after the app closed with cards pending, or a
                    // retry): hand back what's there instead of dealing a different hand. The
                    // framing wasn't persisted, so the screen falls back to a static one.
                    if (chapter.playerChoiceCards != null) {
                        emit(StreamingState.Success(ChoiceCardsReveal(chapter, screenTitle = "", screenSubtitle = "")))
                        return@flow
                    }

                    val prompt =
                        ChapterPrompts.choiceCardsPrompt(
                            promptService = promptService,
                            saga = saga,
                            chapter = chapterContent,
                        )
                    val generateFlow =
                        flow {
                            // Null means a final failure (quota, missing blueprint, exhausted
                            // retries) — carried as an empty hand below rather than an error.
                            emit(
                                StreamingState.Success(
                                    gemmaClient.generate<GeneratedContent<GeneratedPlayerChoices>>(
                                        promptSplit =
                                            prompt.mergeInstructions(
                                                genreConfigService.conversationInstructions(saga.data.genre),
                                            ),
                                        requirement = ModelRequirement.MEDIUM,
                                    ),
                                ),
                            )
                        }
                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            generateFlow,
                            "Preparing the choices that close this chapter",
                            genre = saga.data.genre,
                        ).collect { state ->
                            when (state) {
                                is StreamingState.Success -> {
                                    val dealt = state.data?.data
                                    // Gson leaves an omitted list null despite the type. A partial
                                    // hand (pairs dropped by isAnswerable()) is worse than none: the
                                    // step is mandatory, and three forced choices read as a deliberate
                                    // beat while one or two read as a bug. An empty list records "tried,
                                    // nothing usable" so the chapter moves on instead of asking again.
                                    val validCards =
                                        (dealt?.cards as List<GeneratedChoiceCard>?)
                                            .orEmpty()
                                            .filter { it.isAnswerable() }
                                    val hand =
                                        if (validCards.size >= PLAYER_CHOICE_CARD_COUNT) {
                                            validCards.take(PLAYER_CHOICE_CARD_COUNT)
                                        } else {
                                            emptyList()
                                        }
                                    // Layered onto the freshest row, not the pre-generation snapshot.
                                    val latest = chapterRepository.getChapterById(chapterId) ?: chapter
                                    val updated = updateChapter(latest.copy(playerChoiceCards = hand, playerChoiceAnswers = null))
                                    emit(
                                        StreamingState.Success(
                                            ChoiceCardsReveal(
                                                chapter = updated,
                                                screenTitle = (dealt?.screenTitle as String?).orEmpty(),
                                                screenSubtitle = (dealt?.screenSubtitle as String?).orEmpty(),
                                            ),
                                        ),
                                    )
                                }

                                is StreamingState.Reasoning -> emit(state)

                                is StreamingState.Error -> emit(state)
                            }
                        }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    emit(StreamingState.Error(e.message ?: "Choice cards generation failed", e))
                }
            }

        override fun synthesizeChapterEvolutionStream(chapterId: Int): Flow<StreamingState<GeneratedContentWithLore<Chapter>?>> =
            flow {
                try {
                    val (saga, chapterContent) = fetchContext(chapterId)
                    val prompt =
                        ChapterPrompts.chapterSynthesisPrompt(
                            promptService = promptService,
                            saga = saga,
                            chapter = chapterContent,
                            narrativeRules = remoteConfigService.getNarrativeRules(),
                            conversationDirective = emptyString(),
                        )
                    val actContext =
                        promptService.buildSplitBlueprint(
                            saga.getDirectiveKey(),
                            emptyMap(),
                        )

                    // See generateChapterIntroductionStream's own comment on the same swap: sync
                    // call, single-emission flow, reasoningSynthesizerService unaffected.
                    val generateFlow =
                        flow {
                            val synthesized =
                                gemmaClient.generate<GeneratedContent<UnifiedChapterUpdate>>(
                                    promptSplit =
                                        prompt.mergeInstructions(
                                            genreConfigService.conversationInstructions(saga.data.genre),
                                            actContext.renderInstructions(),
                                            artworkConceptService.artworkInstructions(ImageType.COVER),
                                            // The cards are answered before this runs, so the
                                            // chapter's own read is the latest one.
                                            PlayerSpectrumPrompts.lensInstructions(
                                                PlayerSpectrumPrompts.latestSpectrum(saga),
                                            ),
                                            PlayerSpectrumPrompts.choicesInstructions(
                                                hasChoices = !chapterContent.data.playerChoiceAnswers.isNullOrEmpty(),
                                            ),
                                        ),
                                    requirement = ModelRequirement.HIGH,
                                )
                            if (synthesized == null) {
                                emit(
                                    StreamingState.Error(
                                        message = "Chapter synthesis returned no result",
                                        throwable = IllegalStateException("Chapter synthesis generation failed"),
                                    ),
                                )
                            } else {
                                emit(StreamingState.Success(synthesized))
                            }
                        }
                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            generateFlow,
                            "Generating new chapter...",
                            genre = saga.data.genre,
                        ).collect { state ->
                            when (state) {
                                is StreamingState.Success -> {
                                    val synthesis = state.data!!.data

                                    // 1. Update Chapter details & Narrative Guide
                                    val mergedChapter = synthesis.chapter.mergeInto(chapterContent.data)
                                    val closingCheckpoint =
                                        synthesis.closingCheckpoint?.let {
                                            worldLocationUseCase.resolveCheckpoint(
                                                sagaId = saga.data.id,
                                                generated = it,
                                                originChapterId = chapterContent.data.id,
                                            )
                                        }
                                    val updatedChapter =
                                        updateChapter(
                                            mergedChapter.copy(
                                                continuitySummary =
                                                    synthesis.continuitySummary
                                                        ?: mergedChapter.continuitySummary,
                                                closingCheckpoint = closingCheckpoint ?: mergedChapter.closingCheckpoint,
                                            ),
                                        )
                                    closingCheckpoint?.locationId?.let {
                                        worldLocationUseCase.recordVisit(it, chapterId = chapterContent.data.id)
                                    }

                                    // 2. Save Landmark Wikis
                                    val persistedWikis = mutableListOf<Wiki>()
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
                                        val savedWiki =
                                            if (existingWiki != null) {
                                                wikiUseCase.updateWiki(wikiToSave)
                                            } else {
                                                wikiUseCase.saveWiki(wikiToSave)
                                            }
                                        persistedWikis.add(savedWiki)
                                    }

                                    // 3. Save Character Arcs
                                    val arcCharacters = mutableListOf<Character>()
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
                                            arcCharacters.add(it.data)
                                        }
                                    }

                                    // 4. Update Global World State
                                    synthesis.worldStateUpdate?.let {
                                        sagaRepository.updateSaga(saga.data.copy(worldState = it))
                                    }

                                    emit(
                                        StreamingState.Success(
                                            GeneratedContentWithLore(
                                                data = updatedChapter,
                                                finalMessage = state.data.finalMessage,
                                                wikis = persistedWikis,
                                                characters = arcCharacters,
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
                    if (e is kotlinx.coroutines.CancellationException) {
                        throw e
                    }
                    emit(StreamingState.Error(e.message ?: "Chapter synthesis failed", e))
                }
            }
    }

private const val PLAYER_CHOICE_CARD_COUNT = 3

private const val PLAYER_CHOICE_OPTION_COUNT = 2

/** Gson leaves omitted fields null despite the types, so every check here is null-safe. */
private fun GeneratedChoiceCard.isAnswerable(): Boolean {
    val dealtOptions = (options as List<ChoiceOption?>?).orEmpty()
    return !(choiceTitle as String?).isNullOrBlank() &&
        dealtOptions.size == PLAYER_CHOICE_OPTION_COUNT &&
        dealtOptions.all { option ->
            option != null && !(option.text as String?).isNullOrBlank() && !(option.tag as String?).isNullOrBlank()
        }
}
