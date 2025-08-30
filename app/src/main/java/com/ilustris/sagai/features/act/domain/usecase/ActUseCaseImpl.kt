package com.ilustris.sagai.features.act.domain.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ActPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.narrative.ActPurpose
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
        private val textGenClient: TextGenClient,
        private val gemmaClient: GemmaClient,
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

        override suspend fun generateAct(saga: SagaContent): RequestResult<Exception, Act> =
            try {
                val titlePrompt = generateActPrompt(saga)
                gemmaClient.generate<Act>(titlePrompt)!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private fun generateActPrompt(saga: SagaContent) =
            ActPrompts.generateActConclusion(
                saga,
                saga.currentActInfo!!,
                getPurpose(saga.acts.size),
            )

        private fun getPurpose(actCount: Int) =
            when (actCount) {
                1 -> ActPurpose.FIRST_ACT_PURPOSE
                2 -> ActPurpose.SECOND_ACT_PURPOSE
                else -> ActPurpose.THIRD_ACT_PURPOSE
            }

        override fun getActContent(actId: Int): Flow<ActContent?> = actRepository.getActContent(actId)
    }
