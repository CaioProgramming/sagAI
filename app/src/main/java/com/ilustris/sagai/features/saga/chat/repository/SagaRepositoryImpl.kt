package com.ilustris.sagai.features.saga.chat.repository

import android.icu.util.Calendar
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
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
import javax.inject.Inject

class SagaRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
        private val genreReferenceHelper: GenreReferenceHelper,
        private val gemmaClient: GemmaClient,
        private val imageCropHelper: ImageCropHelper,
        private val fileHelper: FileHelper,
        private val imagenClient: ImagenClient,
        private val backupService: BackupService,
    ) : SagaRepository {
        private val sagaDao: SagaDao by lazy {
            database.sagaDao()
        }

        override fun getChats(): Flow<List<SagaContent>> = sagaDao.getSagaContent()

        override fun getSagaSummaries() = sagaDao.getSagaSummaries()

        override fun getAllSagas() = sagaDao.getAllSagas()

        override fun getPlaythroughData() = sagaDao.getPlaythroughData()

        override fun getSagaById(id: Int) = sagaDao.getSagaContent(id)

        override fun getSagaInfo(id: Int) = sagaDao.getSagaInfo(id)

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
            val newIcon =
                imagenClient
                    .generateIntegratedImage(
                        genre = saga.genre,
                        imageReference = null,
                        context = context,
                        imageType = ImageType.COVER,
                        variationId = saga.variationId,
                    )

            if (newIcon.isFailure) {
                throw newIcon.error.value
            }

            val file =
                fileHelper.saveFile(
                    fileName = saga.title,
                    data = newIcon.getSuccess(),
                    path = "${saga.id}",
                )

            updateSaga(saga.copy(icon = file!!.absolutePath))
        }

        override fun generateSagaIconStream(
            saga: Saga,
            characters: List<Character>,
        ): Flow<StreamingState<Saga>> =
            kotlinx.coroutines.flow.flow {
                try {
                    val context = generateIconContext(saga, characters)
                    imagenClient
                        .generateIntegratedImageStream(
                            genre = saga.genre,
                            imageReference = null,
                            context = context,
                            imageType = ImageType.COVER,
                            variationId = saga.variationId,
                        ).collect { state ->
                            when (state) {
                                is StreamingState.Reasoning -> {
                                    emit(StreamingState.Reasoning(state.chunk))
                                }

                                is StreamingState.Success -> {
                                    val file =
                                        fileHelper.saveFile(
                                            fileName = saga.title,
                                            data = state.data.data,
                                            path = "${saga.id}",
                                        )
                                    val updatedSaga =
                                        updateSaga(saga.copy(icon = file!!.absolutePath))
                                    emit(StreamingState.Success(updatedSaga))
                                }

                                is StreamingState.Error -> {
                                    emit(StreamingState.Error(state.message, state.throwable))
                                }
                            }
                        }
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
                appendLine("Story context: ")
                appendLine(saga.toAINormalize(ChatPrompts.sagaExclusions))
                appendLine(
                    "FINAL MANDATE: Create a balanced composition with the main character are clearly visible and integrated.",
                )
            }

        override suspend fun backupSaga(sagaContent: SagaContent) = backupService.backupSaga(sagaContent)
    }
