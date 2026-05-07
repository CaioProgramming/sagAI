package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.prompts.ActPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.UnifiedActUpdate
import com.ilustris.sagai.features.act.data.repository.ActRepository
import com.ilustris.sagai.features.characters.data.model.ArcSourceType
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ActUseCaseImpl
    @Inject
    constructor(
        private val actRepository: ActRepository,
        private val wikiUseCase: WikiUseCase,
        private val characterUseCase: CharacterUseCase,
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        private val remoteConfigService: RemoteConfigService,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
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
                        blueprintKey = ActPrompts.ACT_CONCLUSION_BLUEPRINT,
                    )!!

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
        ) = flow {
            try {
                val titlePrompt = generateActPrompt(saga)
                reasoningSynthesizerService
                    .synthesizeReasoning(
                        gemmaClient
                            .generateStreaming<GeneratedContent<Act>>(
                                prompt = titlePrompt,
                                filterOutputFields =
                                    listOf(
                                        "id",
                                        "sagaId",
                                        "currentChapterId",
                                        "introduction",
                                    ),
                                useCore = true,
                            ),
                        "Generating new act...",
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
                                        GeneratedContent(
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
                gemmaClient.generate<GeneratedContent<String>>(
                    prompt,
                    useCore = true,
                    blueprintKey = ActPrompts.ACT_INTRODUCTION_BLUEPRINT,
                )!!
            val updatedAct = actRepository.updateAct(act.copy(introduction = intro.data))
            GeneratedContent(updatedAct, intro.finalMessage)
        }

        override fun generateActIntroductionStream(
            saga: SagaContent,
            act: Act,
        ) = flow {
            try {
                val prompt =
                    ActPrompts.actIntroductionPrompt(
                        promptService = promptService,
                        saga = saga,
                        narrativeRules = remoteConfigService.getNarrativeRules(),
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
                            ),
                        "Starting a new act...",
                    ).collect { state ->
                        when (state) {
                            is StreamingState.Success -> {
                                val introContent = state.data
                                val updatedAct = updateAct(act.copy(introduction = introContent.data))
                                emit(
                                    StreamingState.Success(
                                        GeneratedContent(
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

        override fun synthesizeActEvolutionStream(
            saga: SagaContent,
            actContent: ActContent,
        ): Flow<StreamingState<GeneratedContent<Act>>> =
            flow {
                try {
                    val conversationDirective =
                        genreConfigService.conversationBlueprint(saga.data.genre)
                    val prompt =
                        ActPrompts.actSynthesisPrompt(
                            promptService = promptService,
                            saga = saga,
                            act = actContent,
                            narrativeRules = remoteConfigService.getNarrativeRules(),
                            conversationDirective = conversationDirective,
                        )

                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            gemmaClient
                                .generateStreaming<GeneratedContent<UnifiedActUpdate>>(
                                    prompt = prompt,
                                    blueprintKey = ActPrompts.ACT_SYNTHESIS_BLUEPRINT,
                                    requirement = GemmaClient.ModelRequirement.HIGH,
                                ),
                            "Finishing story act",
                        ).collect { state ->
                            when (state) {
                                is StreamingState.Success -> {
                                    val synthesis = state.data.data

                                    // 1. Update Act details & Narrative Guide
                                    val updatedAct =
                                        updateAct(
                                            actContent.data.copy(
                                                title = synthesis.actTitle,
                                                introduction = synthesis.actIntroduction,
                                                content = synthesis.actContent,
                                                narrativeGuide = synthesis.narrativeGuide,
                                                emotionalReview = synthesis.emotionalReview,
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
                                                    sourceId = actContent.data.id,
                                                    sourceType = ArcSourceType.ACT,
                                                    title = arcUpdate.arcTitle,
                                                    content = arcUpdate.arcContent,
                                                ),
                                            )
                                        }
                                    }
                                    synthesis.finalWorldState?.let {
                                        sagaRepository.updateSaga(saga.data.copy(worldState = it))
                                    }

                                    emit(
                                        StreamingState.Success(
                                            GeneratedContent(
                                                updatedAct,
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
                    emit(StreamingState.Error(e.message ?: "Act synthesis failed", e))
                }
            }
    }
