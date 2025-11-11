package com.ilustris.sagai.features.home.data.usecase

import android.net.Uri
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.HomePrompts // Added import
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        private val backupService: BackupService,
        private val sagaBackupService: SagaBackupService,
        private val remoteConfig: FirebaseRemoteConfig,
        billingService: BillingService,
    ) : HomeUseCase {
        override val billingState = billingService.state

        override fun getSagas(): Flow<List<SagaContent>> =
            sagaRepository.getChats().map { content ->
                processSagaContent(content)
            }

        override suspend fun requestDynamicCall(): RequestResult<DynamicSagaPrompt> =
            executeRequest {
                Log.d("HomeUseCaseImpl", "Fetching new dynamic saga texts...")
                val prompt = HomePrompts.dynamicSagaCreationPrompt()

                val result =
                    gemmaClient.generate<DynamicSagaPrompt>(
                        prompt,
                        temperatureRandomness = 0.7f,
                        requireTranslation = true,
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

        private fun processSagaContent(content: List<SagaContent>): List<SagaContent> =
            content.sortedByDescending { saga ->
                saga
                    .flatMessages()
                    .firstOrNull()
                    ?.message
                    ?.timestamp ?: 0L
            }
    }
