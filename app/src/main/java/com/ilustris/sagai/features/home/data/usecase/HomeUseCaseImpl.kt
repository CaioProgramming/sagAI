package com.ilustris.sagai.features.home.data.usecase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.HomePrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupService
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class HomeUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        private val backupService: BackupService,
        private val sagaBackupService: SagaBackupService,
        private val remoteConfig: FirebaseRemoteConfig,
        private val promptService: PromptService,
        private val sagaDetailUseCase: SagaDetailUseCase,
        private val billingService: BillingService,
    ) : HomeUseCase {
        override val billingState = billingService.state

        override fun getSagas(): Flow<List<SagaContent>> = sagaRepository.getChats().map { processSagaContent(it) }

        override suspend fun requestDynamicCall(): RequestResult<DynamicSagaPrompt> =
            executeRequest {
                Timber.d("Fetching new dynamic saga texts...")
                val result =
                    gemmaClient.generate<DynamicSagaPrompt>(
                        prompt = HomePrompts.dynamicSagaCreationPrompt(promptService),
                        blueprintKey = HomePrompts.DYNAMIC_SAGA_CREATION_BLUEPRINT,
                        temperatureRandomness = .1f,
                        requireTranslation = true,
                        requirement = GemmaClient.ModelRequirement.TINY,
                    )
                result!!
            }

        override suspend fun createFakeSaga(): RequestResult<Saga> =
            executeRequest {
                sagaRepository
                    .saveChat(
                        Saga(
                            title = "Debug Saga",
                            description = "This saga was created for debug purposes only.",
                            genre = Genre.entries.random(),
                            isDebug = true,
                        ),
                    )
            }

        override suspend fun checkDebugBuild(): Boolean = BuildConfig.DEBUG && remoteConfig.getValue("isDebugger").asBoolean()

        override suspend fun recoverSaga(sagaContent: RestorableSaga) = sagaBackupService.restoreContent(sagaContent)

        override suspend fun generateStoryBriefing(saga: SagaContent): RequestResult<StoryDailyBriefing> =
            sagaDetailUseCase.generateStoryBriefing(saga)

        private fun processSagaContent(content: List<SagaContent>): List<SagaContent> =
            content.sortedWith(
                compareBy<SagaContent> { saga ->
                    saga.data.isEnded
                }.thenByDescending { saga ->
                    if (saga.data.isEnded) {
                        saga.data.endedAt
                    } else {
                        saga
                            .flatMessages()
                            .lastOrNull()
                            ?.message
                            ?.timestamp ?: saga.data.createdAt
                    }
                },
            )
    }
