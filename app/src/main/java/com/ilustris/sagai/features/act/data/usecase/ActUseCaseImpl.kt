package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.prompts.ActPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.repository.ActRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ActUseCaseImpl
    @Inject
    constructor(
        private val actRepository: ActRepository,
        private val gemmaClient: GemmaClient,
        private val remoteConfigService: RemoteConfigService,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
    ) : ActUseCase {
        override fun getActsBySagaId(sagaId: Int): Flow<List<Act>> = actRepository.getActsBySagaId(sagaId)

        override suspend fun saveAct(act: Act): Act = actRepository.saveAct(act)

        override suspend fun updateAct(act: Act): Act = actRepository.updateAct(act)

        override suspend fun deleteAct(act: Act) {
            actRepository.deleteAct(act)
        }

        override suspend fun deleteActsForSaga(sagaId: Int) {
            actRepository.deleteActsForSaga(sagaId)
        }

        override suspend fun generateAct(
            saga: SagaContent,
            actContent: ActContent,
        ): RequestResult<Act> =
            executeRequest {
                val titlePrompt = generateActPrompt(saga)
                val newAct =
                    gemmaClient.generate<Act>(
                        titlePrompt,
                        filterOutputFields =
                            listOf(
                                "id",
                                "sagaId",
                                "currentChapterId",
                                "introduction",
                            ),
                        useCore = true,
                    blueprintKey = ActPrompts.ACT_CONCLUSION_BLUEPRINT)!!

                updateAct(
                    actContent.data.copy(
                        sagaId = saga.data.id,
                        currentChapterId = null,
                        title = newAct.title,
                        content = newAct.content,
                        emotionalReview = newAct.emotionalReview,
                    ),
                )
            }

        override fun generateActStream(
            saga: SagaContent,
            actContent: ActContent,
        ) = kotlinx.coroutines.flow.flow {
            try {
                val titlePrompt = generateActPrompt(saga)
                gemmaClient
                    .generateStreaming<com.ilustris.sagai.core.ai.model.GeneratedContent<Act>>(
                        prompt = titlePrompt,
                        filterOutputFields =
                            listOf(
                                "id",
                                "sagaId",
                                "currentChapterId",
                                "introduction",
                            ),
                        useCore = true,
                    ).collect { state ->
                        when (state) {
                            is StreamingState.Success -> {
                                val newAct = state.data.data
                                val updatedAct =
                                    updateAct(
                                        actContent.data.copy(
                                            sagaId = saga.data.id,
                                            currentChapterId = null,
                                            title = newAct.title,
                                            content = newAct.content,
                                            emotionalReview = newAct.emotionalReview,
                                        ),
                                    )
                                emit(
                                    StreamingState.Success(
                                        com.ilustris.sagai.core.ai.model.GeneratedContent(
                                            updatedAct,
                                            state.data.finalMessage,
                                        ),
                                    ),
                                )
                            }

                            is StreamingState.Error -> {
                                emit(
                                    StreamingState.Error(
                                        state.message,
                                    ),
                                )
                            }

                            is StreamingState.Reasoning -> {
                                emit(
                                    StreamingState.Reasoning(
                                        state.chunk,
                                    ),
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                emit(StreamingState.Error(e.message ?: "Unknown error"))
            }
        }

        private suspend fun generateActPrompt(saga: SagaContent): String {
            val narrativeRules = remoteConfigService.getJson<NarrativeRules>("narrative_rules")!!

            val genreConfig = genreConfigService.getGenreConfig(saga.data.genre)
            return ActPrompts.generateActConclusion(
                promptService,
                saga,
                saga.currentActInfo!!,
                narrativeRules,
                genreConfig,
            )
        }

        override suspend fun generateActIntroduction(
            saga: SagaContent,
            act: Act,
        ) = executeRequest {
            val isFirst = saga.acts.isEmpty()
            if (isFirst) null else saga.acts.last()

            genreConfigService.getGenreConfig(saga.data.genre)
            val prompt =
                ActPrompts.actIntroductionPrompt(
                    promptService = promptService,
                    saga = saga,
                    narrativeRules = remoteConfigService.getNarrativeRules(),
                    conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                )

            val intro =
                gemmaClient.generate<com.ilustris.sagai.core.ai.model.GeneratedContent<String>>(
                    prompt,
                    useCore = true,
                blueprintKey = ActPrompts.ACT_INTRODUCTION_BLUEPRINT)!!
            val updatedAct = actRepository.updateAct(act.copy(introduction = intro.data))
            com.ilustris.sagai.core.ai.model
                .GeneratedContent(updatedAct, intro.finalMessage)
        }

        override fun generateActIntroductionStream(
            saga: SagaContent,
            act: Act,
        ) = kotlinx.coroutines.flow.flow {
            try {
                val prompt =
                    ActPrompts.actIntroductionPrompt(
                        promptService = promptService,
                        saga = saga,
                        narrativeRules = remoteConfigService.getNarrativeRules(),
                        conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                    )

                gemmaClient
                    .generateStreaming<com.ilustris.sagai.core.ai.model.GeneratedContent<String>>(
                        prompt = prompt,
                        requireTranslation = true,
                        useCore = true,
                        requirement = GemmaClient.ModelRequirement.HIGH,
                    ).collect { state ->
                        when (state) {
                            is StreamingState.Success -> {
                                val introContent = state.data
                                val updatedAct = updateAct(act.copy(introduction = introContent.data))
                                emit(
                                    StreamingState.Success(
                                        com.ilustris.sagai.core.ai.model.GeneratedContent(
                                            updatedAct,
                                            introContent.finalMessage,
                                        ),
                                    ),
                                )
                            }

                            is StreamingState.Error -> {
                                emit(StreamingState.Error(state.message))
                            }

                            is StreamingState.Reasoning -> {
                                emit(StreamingState.Reasoning(state.chunk))
                            }
                    }
                }
        } catch (e: Exception) {
            emit(StreamingState.Error(e.message ?: "Unknown error"))
        }
    }
    }
