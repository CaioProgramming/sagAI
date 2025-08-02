package com.ilustris.sagai.features.saga.detail.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.filterCharacterMessages
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.filterMention
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.filterMessageType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.rankMentions
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.rankMessageTypes
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.rankTopCharacters
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.detail.data.model.Review
import javax.inject.Inject

class SagaDetailUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val fileHelper: FileHelper,
        private val gemmaClient: GemmaClient,
        private val textGenClient: TextGenClient,
        private val imageGenClient: ImagenClient,
        private val characterUseCase: CharacterUseCase,
        private val chapterUseCase: ChapterUseCase,
    ) : SagaDetailUseCase {
        override suspend fun regenerateSagaIcon(saga: SagaContent): RequestResult<Exception, Saga> =
            try {
                val metaPrompt =
                    gemmaClient.generate<String>(
                        prompt = SagaPrompts.iconDescription(saga.data, saga.mainCharacter!!),
                        false,
                    )!!
                val newIcon =
                    imageGenClient.generateImage(
                        ImagePrompts.wallpaperGeneration(
                            saga.data,
                            metaPrompt,
                        ),
                    )!!

                val file =
                    fileHelper.saveFile(
                        fileName = saga.data.title,
                        data = newIcon,
                        path = "${saga.data.id}",
                    )

                sagaRepository
                    .updateChat(saga.data.copy(icon = file!!.absolutePath))
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun fetchSaga(sagaId: Int) = sagaRepository.getSagaById(sagaId)

        override suspend fun deleteSaga(saga: Saga) {
            fileHelper.deletePath(saga.id)
            sagaRepository.deleteChat(saga)
        }

        override suspend fun createReview(content: SagaContent): RequestResult<Exception, Saga> =
            try {
                val prompt =
                    SagaPrompts.reviewGeneration(
                        content,
                        content.messages
                            .filterCharacterMessages(
                                content.mainCharacter!!,
                            ).size,
                        content.messages.rankMessageTypes(),
                        content.messages
                            .rankTopCharacters(
                                content.characters.filter { it.id != content.mainCharacter.id },
                            ).take(3),
                        content.messages
                            .rankMentions(
                                content.characters.filter { it.id != content.mainCharacter.id },
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
    }
