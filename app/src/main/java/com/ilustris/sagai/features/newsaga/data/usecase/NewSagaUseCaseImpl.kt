package com.ilustris.sagai.features.newsaga.data.usecase

import android.util.Log
import com.google.firebase.ai.type.PublicPreviewAPI
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.FileHelper
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import java.util.Calendar
import javax.inject.Inject

@OptIn(PublicPreviewAPI::class)
class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val textGenClient: TextGenClient,
        private val imageGenClient: ImagenClient,
        private val sagaRepository: SagaRepository,
        private val characterUseCase: CharacterUseCase,
        private val fileHelper: FileHelper,
    ) : NewSagaUseCase {
        override suspend fun saveSaga(
            sagaData: SagaData,
            characterDescription: String,
        ): RequestResult<Exception, Pair<SagaData, Character>> =
            try {
                val saga =
                    sagaRepository.saveChat(
                        sagaData.copy(
                            id = 0,
                            createdAt = Calendar.getInstance().timeInMillis,
                            mainCharacterId = null,
                        ),
                    )

                val genCharacter =
                    characterUseCase
                        .generateCharacter(
                            SagaContent(data = saga),
                            characterDescription,
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

        override suspend fun generateSaga(sagaForm: SagaForm): RequestResult<Exception, SagaData> =
            try {
                val saga =
                    textGenClient.generate<SagaData>(
                        generateSagaPrompt(sagaForm),
                        true,
                    )
                saga!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateSagaIcon(
            sagaForm: SagaData,
            character: Character,
        ): RequestResult<Exception, SagaData> =
            try {
                val genre = sagaForm.genre
                val prompt = generateSagaIconPrompt(sagaForm, character)

                val request = imageGenClient.generateImage(prompt)
               /* val request =
                    FreepikRequest(
                        prompt = prompt,
                        negative_prompt = GenrePrompts.negativePrompt(sagaForm.genre),
                        style = GenrePrompts.sagaWallpaperStyling(sagaForm.genre),
                    )*/
                val image =
                    imageGenClient
                        .generateImage(prompt)!!
                        .data

                val file = fileHelper.saveFile(sagaForm.title, image, path = "${sagaForm.id}")

                sagaRepository
                    .updateChat(sagaForm.copy(icon = file!!.absolutePath))
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        private fun generateSagaIconPrompt(
            sagaData: SagaData,
            mainCharacter: Character,
        ) = SagaPrompts.wallpaperGeneration(
            sagaData,
            mainCharacter,
        )

        private fun generateSagaPrompt(sagaForm: SagaForm): String =
            SagaPrompts.sagaGeneration(
                sagaForm,
            )
    }
