package com.ilustris.sagai.features.saga.chat.repository

import android.icu.util.Calendar
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.features.imagegeneration.ImageGenerationService
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationRequest
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.GenreReferenceHelper
import com.ilustris.sagai.core.file.ImageCropHelper
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.datasource.SagaDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class SagaRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
        private val genreReferenceHelper: GenreReferenceHelper,
        private val gemmaClient: GemmaClient,
        private val imageCropHelper: ImageCropHelper,
        private val fileHelper: FileHelper,
        private val imageGenerationService: ImageGenerationService,
        private val backupService: BackupService,
    ) : SagaRepository {
        private val sagaDao: SagaDao by lazy {
            database.sagaDao()
        }

        override fun getChats(): Flow<List<SagaContent>> = sagaDao.getSagaContent()

        override fun getSagaSummaries() = sagaDao.getSagaSummaries()

        override fun getAllSagas() = sagaDao.getAllSagas()

        override fun getPlaythroughData() = sagaDao.getPlaythroughData()

        override fun getSagaById(id: Int?) = if (id != null) sagaDao.getSagaContent(id) else emptyFlow()

        override fun getSagaMetadata(id: Int) = sagaDao.getSagaMetadata(id)

        override fun getSagaInfo(id: Int) = sagaDao.getSagaInfo(id)

        override fun getSaga(id: Int) = sagaDao.getSaga(id)

        override suspend fun saveChat(saga: Saga) =
            saga.copy(
                id =
                    sagaDao
                        .saveSagaData(saga.copy(createdAt = Calendar.getInstance().timeInMillis))
                        .toInt(),
            )

        override suspend fun updateSaga(saga: Saga): Saga {
            sagaDao.updateSaga(saga)
            return saga
        }

        override suspend fun deleteChat(saga: Saga) = sagaDao.deleteSagaData(saga)

        override suspend fun deleteChatById(id: String) = sagaDao.deleteSagaData(id)

        override suspend fun deleteAllChats() = sagaDao.deleteAllSagas()

        override suspend fun generateSagaIcon(
            saga: Saga,
            characters: List<Character>,
        ) = executeRequest {
            val context = generateIconContext(saga, characters)
            imageGenerationService
                .enqueue(
                    ImageGenerationRequest(
                        genre = saga.genre,
                        imageReference = null,
                        context = context,
                        imageType = ImageType.COVER,
                        variationId = saga.variationId,
                        label = saga.title,
                        showReveal = true,
                    ),
                ) { bitmap ->
                    val file =
                        fileHelper.saveFile(
                            fileName = saga.title,
                            data = bitmap,
                            path = "${saga.id}",
                        ) ?: error("Failed to save saga icon")
                    updateSaga(saga.copy(icon = file.absolutePath))
                }.getOrThrow()
        }

        override fun generateSagaIconStream(
            saga: Saga,
            characters: List<Character>,
        ): Flow<StreamingState<Saga>> =
            kotlinx.coroutines.flow.flow {
                try {
                    val context = generateIconContext(saga, characters)
                    imageGenerationService
                        .enqueue(
                            ImageGenerationRequest(
                                genre = saga.genre,
                                imageReference = null,
                                context = context,
                                imageType = ImageType.COVER,
                                variationId = saga.variationId,
                                label = saga.title,
                                showReveal = true,
                            ),
                        ) { bitmap ->
                            val file =
                                fileHelper.saveFile(
                                    fileName = saga.title,
                                    data = bitmap,
                                    path = "${saga.id}",
                                ) ?: error("Failed to save saga icon")
                            updateSaga(saga.copy(icon = file.absolutePath))
                        }.fold(
                            onSuccess = { updatedSaga ->
                                emit(StreamingState.Success(updatedSaga))
                            },
                            onFailure = { error ->
                                emit(
                                    StreamingState.Error(
                                        error.message ?: "Failed to generate saga icon stream",
                                        error,
                                    ),
                                )
                            },
                        )
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Failed to generate saga icon stream", e))
                }
            }

        private fun generateIconContext(
            saga: Saga,
            characters: List<Character>,
        ): String =
            buildString {
                appendLine("### MANDATORY CHARACTER ICON")
                appendLine("The following character are ESSENTIAL to this icon:")
                appendLine(characters.joinToString { it.name })
                appendLine("This icon represents the saga. You MUST integrate ALL provided characters into the composition.")
                appendLine()
                appendLine("#### SUBJECTS DETAILS:")
                appendLine(
                    characters.toAINormalize(
                        listOf(
                            "image",
                            "sagaId",
                            "joinedAt",
                            "id",
                            "emojified",
                            "smartZoom",
                        ),
                    ),
                )
                appendLine()

                val artwork = saga.artwork?.takeIf { it.isNotBlank() }
                if (artwork != null) {
                    appendLine("### CONCEPT ART DIRECTION")
                    appendLine(
                        "This is the saga's key-art concept. Ground the composition in this, not in the full story description:",
                    )
                    appendLine(artwork)
                } else {
                    appendLine("Story context: ")
                    appendLine(saga.description)
                }
                appendLine()
                appendLine(
                    "FINAL MANDATE: Create a balanced composition with the main character are clearly visible and integrated.",
                )
            }

        override suspend fun backupSaga(sagaContent: SagaContent) = backupService.backupSaga(sagaContent)
    }
