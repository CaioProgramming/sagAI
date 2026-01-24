package com.ilustris.sagai.features.saga.chat.repository

import android.icu.util.Calendar
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.analytics.AnalyticsConstants
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

        override fun getSagaById(id: Int) = sagaDao.getSagaContent(id)

        override suspend fun saveChat(saga: Saga) =
            saga.copy(
                id =
                    sagaDao
                        .saveSagaData(saga.copy(createdAt = Calendar.getInstance().timeInMillis))
                        .toInt(),
            )

        override suspend fun updateChat(saga: Saga): Saga {
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
            val iconReferenceComposition =
                genreReferenceHelper
                    .getRandomCompositionReference(saga.genre)
                    .getSuccess()

            val context =
                buildString {
                    appendLine("### MANDATORY MULTI-CHARACTER ICON")
                    appendLine("The following characters are ESSENTIAL to this icon:")
                    append("[")
                    appendLine(characters.joinToString { it.name })
                    appendLine("]")
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
                    appendLine(
                        "FINAL MANDATE: Create a balanced composition where all characters are clearly visible and integrated into the ${saga.genre.name} aesthetic.")
                }
            val newIcon =
                imagenClient
                    .generateIntegratedImage(
                        genre = saga.genre,
                        imageReference = iconReferenceComposition,
                        context = context,
                        imageType = AnalyticsConstants.ImageType.ICON,
                    ).getSuccess()!!

            val file =
                fileHelper.saveFile(
                    fileName = saga.title,
                    data = newIcon,
                    path = "${saga.id}",
                )

            updateChat(saga.copy(icon = file!!.absolutePath))
        }

        override suspend fun backupSaga(sagaContent: SagaContent) = backupService.backupSaga(sagaContent)
    }
