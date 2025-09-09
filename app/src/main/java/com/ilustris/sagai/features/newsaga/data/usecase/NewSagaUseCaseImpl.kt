package com.ilustris.sagai.features.newsaga.data.usecase

import SagaGen
import android.content.Context
import android.util.Log
import coil3.ImageLoader
import coil3.request.ImageRequest
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImageReference
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ImageGuidelines
import com.ilustris.sagai.core.ai.prompts.ImagePrompts
import com.ilustris.sagai.core.ai.prompts.ImageRules
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.core.utils.GenreReferenceHelper
import com.ilustris.sagai.core.utils.ImageCropHelper
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.CallbackContent
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.SagaFormFields
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val textGenClient: TextGenClient,
        private val imageGenClient: ImagenClient,
        private val sagaRepository: SagaRepository,
        private val characterUseCase: CharacterUseCase,
        private val gemmaClient: GemmaClient,
        private val fileHelper: FileHelper,
        private val imageCropHelper: ImageCropHelper,
        private val genreReferenceHelper: GenreReferenceHelper,
    ) : NewSagaUseCase {
        override suspend fun saveSaga(
            sagaData: Saga,
            characterDescription: CharacterInfo?,
        ): RequestResult<Exception, Pair<Saga, Character>> =
            try {
                val saga =
                    sagaRepository.saveChat(
                        sagaData.copy(
                            id = 0,
                            mainCharacterId = null,
                        ),
                    )

                val genCharacter =
                    characterUseCase
                        .generateCharacter(
                            SagaContent(data = saga),
                            characterDescription.toJsonFormat(),
                        ).success.value

                val updatedSaga =
                    sagaRepository.updateChat(
                        sagaData.copy(
                            id = saga.id,
                            mainCharacterId = genCharacter.id,
                        ),
                    )
                Log.i(javaClass.simpleName, "saveSaga: Saga operation complete -> $updatedSaga")
                (updatedSaga to genCharacter).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateSaga(
            sagaForm: SagaForm,
            miniChatContent: List<ChatMessage>,
        ): RequestResult<Exception, SagaGen> =
            try {
                val saga =
                    textGenClient.generate<SagaGen>(
                        generateSagaPrompt(sagaForm, miniChatContent),
                        true,
                    )
                saga!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateSagaIcon(
            sagaForm: Saga,
            character: Character,
        ): RequestResult<Exception, Saga> =
            try {
                val characterOperation =
                    characterUseCase.generateCharacterImage(
                        character = character,
                        saga = sagaForm,
                    )
                val characterIcon =
                    characterOperation
                        .getSuccess()
                        ?.first
                        ?.image
                        ?.let { genreReferenceHelper.getFileBitmap(it) }
                        ?.getSuccess()
                        ?.let {
                            ImageReference(
                                it,
                                ImageGuidelines.characterVisualReferenceGuidance(character.name),
                            )
                        }
                val reference =
                    genreReferenceHelper.getIconReference(sagaForm.genre).getSuccess()?.let {
                        ImageReference(
                            it,
                            ImageGuidelines.compositionReferenceGuidance,
                        )
                    }

                val style =
                    genreReferenceHelper.getGenreStyleReference(sagaForm.genre).getSuccess()?.let {
                        ImageReference(it, ImageGuidelines.styleReferenceGuidance)
                    }
                val metaPromptCover =
                    gemmaClient.generate<String>(
                        SagaPrompts.iconDescription(
                            sagaForm,
                            character,
                        ),
                        references = listOf(style, reference, characterIcon),
                        requireTranslation = false,
                    )!!

                val file =
                    fileHelper.saveFile(
                        fileName = sagaForm.title,
                        data =
                            imageGenClient.generateImage(metaPromptCover).apply {
                                imageCropHelper.cropToPortraitBitmap(this!!)
                            },
                        path = "${sagaForm.id}",
                    )

                sagaRepository
                    .updateChat(sagaForm.copy(icon = file!!.absolutePath))
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun replyAiForm(
            currentMessages: List<ChatMessage>,
            currentFormData: SagaForm,
        ): RequestResult<Exception, SagaCreationGen> =
            try {
                val delayDefaultTime = 700L

                val extractedDataPrompt =
                    gemmaClient.generate<SagaForm>(
                        NewSagaPrompts.extractDataFromUserInputPrompt(
                            currentFormData,
                            currentMessages.last().text,
                        ),
                        requireTranslation = true,
                    )!!

                delay(delayDefaultTime)

                val identifyNextFieldPrompt =
                    gemmaClient
                        .generate<String>(
                            NewSagaPrompts.identifyNextFieldPrompt(extractedDataPrompt),
                            requireTranslation = false,
                        )!!
                        .replace("\n", "")

                val field = SagaFormFields.getByKey(identifyNextFieldPrompt)!!

                delay(delayDefaultTime)

                val nextQuestion =
                    gemmaClient.generate<SagaCreationGen>(
                        NewSagaPrompts.generateCreativeQuestionPrompt(
                            field!!,
                            extractedDataPrompt,
                        ),
                    )!!

                val callBackAction =
                    if (field == SagaFormFields.ALL_FIELDS_COMPLETE) {
                        CallBackAction.SAVE_SAGA
                    } else {
                        CallBackAction.UPDATE_DATA
                    }

                nextQuestion
                    .copy(
                        callback =
                            CallbackContent(
                                action = callBackAction,
                                data = extractedDataPrompt,
                            ),
                    ).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateIntroduction(): RequestResult<Exception, SagaCreationGen> =
            try {
                val prompt = NewSagaPrompts.formIntroductionPrompt()

                val aiRequest =
                    gemmaClient.generate<SagaCreationGen>(
                        prompt,
                        requireTranslation = true,
                    )

                aiRequest!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateCharacterSavedMark(
            character: Character,
            saga: Saga,
        ): RequestResult<Exception, String> =
            try {
                val prompt =
                    NewSagaPrompts.characterCreatedPrompt(
                        character,
                        saga,
                    )

                val aiRequest =
                    gemmaClient.generate<String>(
                        prompt,
                        requireTranslation = true,
                    )

                aiRequest!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private fun generateSagaPrompt(
            sagaForm: SagaForm,
            miniChatContent: List<ChatMessage>,
        ): String =
            SagaPrompts.sagaGeneration(
                sagaForm,
                miniChatContent,
            )
    }
