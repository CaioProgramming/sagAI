package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
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

            val intro = gemmaClient.generate<String>(prompt, useCore = true)!!
            actRepository
                .updateAct(act.copy(introduction = intro))
        }
    }
