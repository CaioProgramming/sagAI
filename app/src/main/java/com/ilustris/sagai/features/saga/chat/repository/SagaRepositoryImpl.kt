package com.ilustris.sagai.features.saga.chat.repository

import android.icu.util.Calendar
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.Artwork
import com.ilustris.sagai.core.ai.model.MAX_COVER_CHARACTERS
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.services.ArtworkConceptService
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.GenreReferenceHelper
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.imagegeneration.ImageGenerationService
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationRequest
import com.ilustris.sagai.features.saga.datasource.SagaDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber
import javax.inject.Inject

class SagaRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
        private val genreReferenceHelper: GenreReferenceHelper,
        private val gemmaClient: GemmaClient,
        private val fileHelper: FileHelper,
        private val imageGenerationService: ImageGenerationService,
        private val backupService: BackupService,
        private val artworkConceptService: ArtworkConceptService,
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
            val sagaWithArtwork = ensureSagaArtwork(saga, characters)
            val context = generateIconContext(sagaWithArtwork, characters)
            imageGenerationService
                .enqueue(
                    ImageGenerationRequest(
                        genre = sagaWithArtwork.genre,
                        imageReference = null,
                        context = context,
                        imageType = ImageType.COVER,
                        variationId = sagaWithArtwork.variationId,
                        label = sagaWithArtwork.title,
                        showReveal = true,
                    ),
                ) { bitmap ->
                    val file =
                        fileHelper.saveFile(
                            fileName = sagaWithArtwork.title,
                            data = bitmap,
                            path = "${sagaWithArtwork.id}",
                        ) ?: error("Failed to save saga icon")
                    updateSaga(sagaWithArtwork.copy(icon = file.absolutePath))
                }.getOrThrow()
        }

        override fun generateSagaIconStream(
            saga: Saga,
            characters: List<Character>,
        ): Flow<StreamingState<Saga>> =
            kotlinx.coroutines.flow.flow {
                try {
                    val sagaWithArtwork = ensureSagaArtwork(saga, characters)
                    val context = generateIconContext(sagaWithArtwork, characters)
                    imageGenerationService
                        .enqueue(
                            ImageGenerationRequest(
                                genre = sagaWithArtwork.genre,
                                imageReference = null,
                                context = context,
                                imageType = ImageType.COVER,
                                variationId = sagaWithArtwork.variationId,
                                label = sagaWithArtwork.title,
                                showReveal = true,
                            ),
                        ) { bitmap ->
                            val file =
                                fileHelper.saveFile(
                                    fileName = sagaWithArtwork.title,
                                    data = bitmap,
                                    path = "${sagaWithArtwork.id}",
                                ) ?: error("Failed to save saga icon")
                            updateSaga(sagaWithArtwork.copy(icon = file.absolutePath))
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

        /** The cover curator for the saga: picks the concept, the characters that carry the cover and its story elements. */
        private suspend fun ensureSagaArtwork(
            saga: Saga,
            characters: List<Character>,
        ): Saga {
            if (saga.artwork?.isBlank() == false) return saga
            val curated =
                artworkConceptService
                    .ensureCoverArtwork(
                        contentType = "Saga",
                        genre = saga.genre,
                        context = sagaArtworkContext(saga, characters),
                        currentArtwork = saga.artwork,
                    ).onFailure {
                        Timber.w(
                            it,
                            "ensureSagaArtwork: failed to generate artwork for saga ${saga.id}",
                        )
                    }.getSuccess()
                    ?.takeUnless { it.isBlank() }
                    ?: return saga
            val artwork =
                curated.copy(
                    characterIds =
                        curated.characterIds
                            .distinct()
                            .filter { id -> id > 0 && characters.any { it.id == id } }
                            .take(MAX_COVER_CHARACTERS),
                )
            return updateSaga(saga.copy(artwork = artwork))
        }

        private fun sagaArtworkContext(
            saga: Saga,
            characters: List<Character>,
        ): String =
            buildString {
                appendLine("### SAGA")
                appendLine(saga.title)
                appendLine(saga.description)
                if (characters.isNotEmpty()) {
                    appendLine()
                    appendLine("### CHARACTERS (id: name, backstory)")
                    characters.forEach { appendLine("- ${it.id}: ${it.name}, ${it.backstory.take(CHARACTER_BACKSTORY_PREVIEW)}") }
                }
            }

        private fun generateIconContext(
            saga: Saga,
            characters: List<Character>,
        ): String =
            buildString {
                val artwork = saga.artwork?.takeUnless { it.isBlank() }
                // The curator names who the cover needs; sagas curated before that show everyone.
                val cover =
                    artwork
                        ?.characterIds
                        .orEmpty()
                        .let { ids -> characters.filter { it.id in ids } }
                        .ifEmpty { characters }
                appendLine("### CHARACTERS")
                appendLine(
                    cover.toAINormalize(
                        listOf(
                            "image",
                            "sagaId",
                            "joinedAt",
                            "id",
                            "emojified",
                            "artwork",
                        ),
                    ),
                )
                appendLine()

                if (artwork != null) {
                    appendLine("### KEY ART CONCEPT")
                    appendLine(artwork.concept)
                    if (artwork.elements.isNotEmpty()) {
                        appendLine()
                        appendLine("### STORY ELEMENTS TO DRAW ON")
                        artwork.elements.forEach { appendLine("- $it") }
                    }
                } else {
                    appendLine("### SAGA CONTEXT")
                    appendLine(saga.description)
                }
            }

        override suspend fun backupSaga(sagaContent: SagaContent) = backupService.backupSaga(sagaContent)
    }

private const val CHARACTER_BACKSTORY_PREVIEW = 200
