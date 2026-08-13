package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.BookGenerationArgs
import com.ilustris.sagai.core.ai.prompts.BookPrompts
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.act.data.repository.ActRepository
import com.ilustris.sagai.features.act.data.source.BookDao
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BookUseCaseImpl
    @Inject
    constructor(
        private val actRepository: ActRepository,
        private val bookDao: BookDao,
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
        private val remoteConfigService: RemoteConfigService,
    ) : BookUseCase {
        override fun generateBookStream(
            saga: SagaContent,
            actContent: ActContent,
        ): Flow<StreamingState<GeneratedContent<Book>?>> =
            flow {
                try {
                    val narrativeRules = remoteConfigService.getNarrativeRules()
                    val args =
                        BookGenerationArgs(
                            sagaContext = SagaPrompts.mainContext(saga),
                            actSummary = actContent.actSummary(true),
                            characters =
                                saga.characters
                                    .map { it.data }
                                    .normalizetoAIItems(ChatPrompts.CHARACTER_EXCLUSIONS),
                            conversationDirective = emptyString(),
                            isFinalVolume = saga.acts.size >= narrativeRules.actUpdateLimit,
                        )

                    val prompt = BookPrompts.generateBookChronicle(promptService, args)

                    val sourceFlow =
                        gemmaClient
                            .generateStreaming<GeneratedContent<Book>>(
                                promptSplit =
                                    prompt.mergeInstructions(
                                        genreConfigService.conversationInstructions(saga.data.genre),
                                    ),
                                useCore = true,
                                requirement = ModelRequirement.HIGH,
                            )

                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            sourceFlow = sourceFlow,
                            context = args.sagaContext,
                            genre = saga.data.genre,
                        ).collect { state ->
                            if (state is StreamingState.Success) {
                                val book =
                                    state.data!!.data.copy(
                                        id = 0,
                                        actId = actContent.data.id,
                                        sagaTitle = saga.data.title,
                                    )
                                bookDao.saveBook(book)
                            }
                            emit(state)
                        }
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Unknown error generating chronicle"))
                }
            }

        override fun generateSagaChronicles(saga: SagaContent): Flow<StreamingState<GeneratedContent<Book>?>> =
            flow {
                val completedActs = saga.acts.filter { it.book == null }

                for (act in completedActs) {
                    generateBookStream(saga, act).collect { state ->
                        emit(state)
                    }
                }
            }

        override suspend fun resetBook(actContent: ActContent) {
            bookDao.deleteBookForAct(actContent.data.id)
        }
    }
