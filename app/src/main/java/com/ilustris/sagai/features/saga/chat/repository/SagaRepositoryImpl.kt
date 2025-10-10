package com.ilustris.sagai.features.saga.chat.repository

import android.icu.util.Calendar
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.ai.prompts.GenrePrompts
import com.ilustris.sagai.core.ai.prompts.ImageGuidelines
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.GenreReferenceHelper
import com.ilustris.sagai.core.utils.ImageCropHelper
import com.ilustris.sagai.core.utils.emptyString
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
        private val billingService: BillingService,
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
            character: Character,
        ) = executeRequest {
            val styleReference =
                genreReferenceHelper
                    .getGenreStyleReference(saga.genre)
                    .getSuccess()
                    ?.let {
                        ImageReference(it, ImageGuidelines.styleReferenceGuidance)
                    }

            val iconReferenceComposition =
                genreReferenceHelper
                    .getIconReference(saga.genre)
                    .getSuccess()
                    ?.let {
                        ImageReference(
                            it,
                            ImageGuidelines.compositionReferenceGuidance,
                        )
                    }

            val characterIcon =
                character
                    .image
                    .let {
                        genreReferenceHelper.getFileBitmap(it).getSuccess()?.let { icon ->
                            ImageReference(
                                icon,
                                ImageGuidelines.characterVisualReferenceGuidance(character.name),
                            )
                        }
                    }

            val references =
                listOfNotNull(styleReference, iconReferenceComposition, characterIcon)
            val metaPrompt =
                gemmaClient.generate<String>(
                    prompt =
                        SagaPrompts
                            .iconDescription(saga, character),
                    references,
                    requireTranslation = false,
                )!!
            val newIcon =
                imagenClient.generateImage(
                    buildString {
                        appendLine(metaPrompt)
                    },
                    listOfNotNull(characterIcon),
                )!!

            val croppedIcon = imageCropHelper.cropToPortraitBitmap(newIcon)

            val file =
                fileHelper.saveFile(
                    fileName = saga.title,
                    data = newIcon,
                    path = "${saga.id}",
                )

            croppedIcon.recycle()
            updateChat(saga.copy(icon = file!!.absolutePath))
        }
    }
