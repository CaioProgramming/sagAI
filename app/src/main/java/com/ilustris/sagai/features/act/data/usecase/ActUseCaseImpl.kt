package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.ActPrompts
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.ActPurpose
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.repository.ActRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ActUseCaseImpl
    @Inject
    constructor(
        private val actRepository: ActRepository,
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

        override suspend fun generateAct(saga: SagaContent): RequestResult<Act> =
            executeRequest {
                val titlePrompt = generateActPrompt(saga)
                gemmaClient.generate<Act>(titlePrompt)!!
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

        override suspend fun generateActIntroduction(
            saga: SagaContent,
            act: Act,
        ) = executeRequest {
            val isFirst = saga.acts.isEmpty()
            val previousAct = if (isFirst) null else saga.acts.last()

            val prompt = ActPrompts.actIntroductionPrompt(saga, previousAct)

            val intro = gemmaClient.generate<String>(prompt, requireTranslation = true)!!
            actRepository
                .updateAct(act.copy(introduction = intro))
        }
    }
