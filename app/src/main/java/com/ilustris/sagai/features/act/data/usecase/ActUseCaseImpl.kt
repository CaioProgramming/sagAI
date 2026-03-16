package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.ActPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.repository.ActRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ActUseCaseImpl
    @Inject
    constructor(
        private val actRepository: ActRepository,
        private val gemmaClient: GemmaClient,
        private val emotionalUseCase: EmotionalUseCase,
        private val remoteConfigService: com.ilustris.sagai.core.services.RemoteConfigService,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
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
                val newAct = gemmaClient.generate<Act>(titlePrompt, useCore = true)!!

                val actUpdate =
                    updateAct(
                        actContent.data.copy(
                            sagaId = saga.data.id,
                            currentChapterId = null,
                            title = newAct.title,
                            content = newAct.content,
                        ),
                    )

                generateEmotionalProfile(saga, actContent.copy(data = actUpdate)).getSuccess()!!
            }

        private suspend fun generateActPrompt(saga: SagaContent): String {
            val narrativeRules =
                NarrativeRules(
                    remoteConfigService.getJson<Map<String, Any>>("narrative_rules") ?: emptyMap(),
                )

            val genreConfig = genreConfigService.getGenreConfig(saga.data.genre)
            return ActPrompts.generateActConclusion(
                promptService,
                saga,
                saga.currentActInfo!!,
                narrativeRules,
                genreConfig,
            )
        }

        private suspend fun getPurpose(actCount: Int): String {
            val narrativeRules =
                NarrativeRules(
                    remoteConfigService.getJson<Map<String, Any>>("narrative_rules") ?: emptyMap(),
                )
            return when (actCount) {
                1 -> narrativeRules.firstActPurpose
                2 -> narrativeRules.secondActPurpose
                else -> narrativeRules.thirdActPurpose
        }
    }

        override suspend fun generateActIntroduction(
            saga: SagaContent,
            act: Act,
        ) = executeRequest {
            val isFirst = saga.acts.isEmpty()
            val previousAct = if (isFirst) null else saga.acts.last()
            val narrativeRules =
                NarrativeRules(
                    remoteConfigService.getJson<Map<String, Any>>("narrative_rules") ?: emptyMap(),
                )

            val genreConfig = genreConfigService.getGenreConfig(saga.data.genre)
            val prompt =
                ActPrompts.actIntroductionPrompt(
                    promptService,
                    narrativeRules,
                    saga,
                    genreConfig,
                    previousAct,
                )

            val intro = gemmaClient.generate<String>(prompt, useCore = true)!!
            actRepository
                .updateAct(act.copy(introduction = intro))
        }

        override suspend fun generateEmotionalProfile(
            saga: SagaContent,
            act: ActContent,
        ) = executeRequest {
            val profile =
                emotionalUseCase
                    .generateEmotionalProfile(
                        saga,
                        act.emotionalSummary(saga),
                    ).getSuccess()!!

            actRepository.updateAct(act.data.copy(emotionalReview = profile))
        }
    }
