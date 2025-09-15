package com.ilustris.sagai.features.saga.detail.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.ai.prompts.GenrePrompts
import com.ilustris.sagai.core.ai.prompts.ImageGuidelines
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.GenreReferenceHelper
import com.ilustris.sagai.core.utils.ImageCropHelper
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.domain.model.filterCharacterMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankMentions
import com.ilustris.sagai.features.saga.chat.domain.model.rankMessageTypes
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.wiki.domain.usecase.EmotionalUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject

class SagaDetailUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val fileHelper: FileHelper,
        private val imageCropHelper: ImageCropHelper,
        private val gemmaClient: GemmaClient,
        private val textGenClient: TextGenClient,
        private val imageGenClient: ImagenClient,
        private val genreReferenceHelper: GenreReferenceHelper,
        private val timelineUseCase: TimelineUseCase,
        private val characterUseCase: CharacterUseCase,
        private val emotionalUseCase: EmotionalUseCase,
    ) : SagaDetailUseCase {
        override suspend fun regenerateSagaIcon(saga: SagaContent): RequestResult<Exception, Saga> =
            executeRequest {
                val newIcon =
                    sagaRepository
                        .generateSagaIcon(saga.data, saga.mainCharacter!!.data)
                        .getSuccess()!!
                sagaRepository.updateChat(newIcon)
            }

        override suspend fun fetchSaga(sagaId: Int) = sagaRepository.getSagaById(sagaId)

        override suspend fun deleteSaga(saga: Saga) {
            fileHelper.deletePath(saga.id)
            sagaRepository.deleteChat(saga)
        }

        override suspend fun createReview(content: SagaContent): RequestResult<Exception, Saga> =
            try {
                val messages = content.flatMessages()
                val characters = content.getCharacters(true)
                val prompt =
                    SagaPrompts.reviewGeneration(
                        content,
                        messages
                            .filterCharacterMessages(
                                content.mainCharacter!!.data,
                            ).size,
                        messages.rankMessageTypes(),
                        messages
                            .rankTopCharacters(
                                characters,
                            ).take(3),
                        messages
                            .rankMentions(
                                characters,
                            ).take(3),
                    )

                val review =
                    textGenClient.generate<Review>(
                        prompt = prompt,
                        requireTranslation = true,
                    )!!
                sagaRepository
                    .updateChat(
                        content.data.copy(
                            review = review,
                        ),
                    ).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun resetReview(content: SagaContent) {
            sagaRepository.updateChat(
                content.data.copy(
                    review = null,
                ),
            )
        }

        override suspend fun createEmotionalReview(content: SagaContent): RequestResult<Exception, Saga> =
            try {
                val emotionalSummary = content.emotionalSummary()
                val prompt = SagaPrompts.emotionalGeneration(content, emotionalSummary.joinToString())

                val review =
                    textGenClient
                        .generate<String>(
                            prompt = prompt,
                            requireTranslation = true,
                        )!!

                sagaRepository
                    .updateChat(
                        content.data.copy(
                            emotionalReview = review,
                        ),
                    ).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun createTimelineReview(
            content: SagaContent,
            timelineContent: TimelineContent,
        ): RequestResult<Exception, Unit> =
            try {
                characterUseCase.generateCharactersUpdate(timelineContent.data, content)
                delay(300)
                timelineUseCase.createTimelineReview(content, timelineContent)
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun createSagaEmotionalReview(currentSaga: SagaContent) =
            executeRequest {
                val request =
                    emotionalUseCase
                        .generateEmotionalProfile(currentSaga)
                        .getSuccess()!!

                sagaRepository
                    .updateChat(
                        currentSaga.data.copy(
                            emotionalReview = request,
                        ),
                    )
            }
    }
