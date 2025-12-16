package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.Sender
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.features.newsaga.data.usecase.SagaProcess
import com.ilustris.sagai.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

data class FormState(
    val message: String? = null,
    val hint: String? = null,
    val suggestions: List<String> = emptyList(),
    val readyToSave: Boolean = false,
)

sealed class AudioPermissionEvent {
    object PermissionRequired : AudioPermissionEvent()
}

@HiltViewModel
class CreateSagaViewModel
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
        private val characterUseCase: CharacterUseCase,
    ) : ViewModel() {
        val form = MutableStateFlow(SagaForm())
        val state = MutableStateFlow(CreateSagaState())
        val effect = MutableStateFlow<Effect?>(null)
        val chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
        val formState = MutableStateFlow(FormState())
        val isGenerating = MutableStateFlow(true)
        val isError = MutableStateFlow(false)
        val callbackAction = MutableStateFlow<CallBackAction?>(null)
        val isSaving = MutableStateFlow(false)
        val loadingMessage = MutableStateFlow<String?>(null)
        val recordingAudio = MutableStateFlow(false)

        private fun updateGenerating(isGenerating: Boolean) {
            this.isGenerating.value = isGenerating
        }

        fun setRecordingAudio(recording: Boolean) {
            recordingAudio.value = recording
        }

        fun onAudioTranscriptionSuccess(transcribedText: String) {
            setRecordingAudio(false)
            sendChatMessage(transcribedText)
        }

        fun startChat() {
            updateGenerating(true)
            viewModelScope.launch(Dispatchers.IO) {
                newSagaUseCase
                    .generateIntroduction()
                    .onSuccess { gen ->
                        handleGeneratedContent(gen)
                        chatMessages.update {
                            it +
                                ChatMessage(
                                    text = gen.message,
                                    sender = Sender.AI,
                                    callback = gen.callback?.action,
                                )
                        }
                    }.onFailure {
                        updateGenerating(false)
                    }
            }
        }

        fun sendChatMessage(userInput: String) {
            updateGenerating(true)
            viewModelScope.launch(Dispatchers.IO) {
                val latestMessage = chatMessages.value.lastOrNull()
                val userMessage = ChatMessage(text = userInput, sender = Sender.USER)
                chatMessages.update { it + userMessage }

                newSagaUseCase
                    .replyAiForm(
                        currentMessages = chatMessages.value,
                        latestMessage = latestMessage?.text,
                        currentFormData = form.value,
                    ).onSuccess { response ->
                        chatMessages.update {
                            it +
                                ChatMessage(
                                    text = response.message,
                                    sender = Sender.AI,
                                    callback = response.callback?.action,
                                )
                        }
                        handleGeneratedContent(response)
                        isError.value = false
                    }.onFailure { e ->
                        Log.e(
                            javaClass.simpleName,
                            "sendChatMessage: Error getting generated content $e",
                        )
                        isError.value = true
                    }
                updateGenerating(false)
            }
        }

        private fun handleGeneratedContent(response: SagaCreationGen) {
            updateGenerating(false)
            callbackAction.value = response.callback?.action
            formState.value =
                formState.value.copy(
                    hint = response.inputHint,
                    suggestions = response.suggestions,
                    message = response.message,
                )

            response.callback?.let { callback ->
                Log.d(
                    javaClass.simpleName,
                    "handleGeneratedContent: Checking new generated content $callback",
                )
                val sagaForm: SagaForm? = callback.data
                sagaForm?.let {
                    Log.i(javaClass.simpleName, "handleGeneratedContent: Updating form to $it")
                    form.value =
                        it.copy(
                            saga =
                                form.value.saga.copy(
                                    title = it.saga.title,
                                    description = it.saga.description,
                                    genre = it.saga.genre,
                                ),
                            character =
                                form.value.character.copy(
                                    name = it.character.name,
                                    gender = it.character.gender,
                                    description = it.character.description,
                                ),
                        )
                }
                if (callback.action == CallBackAction.SAVE_SAGA) {
                    saveSaga()
                }
            }
        }

        private fun finalizeCreation(
            saga: Saga,
            character: Character,
        ) {
            viewModelScope.launch {
                generateProcessMessage(SagaProcess.FINALIZING, saga, character)

                newSagaUseCase
                    .generateSagaIcon(
                        saga,
                        character,
                    )

                generateProcessMessage(
                    SagaProcess.SUCCESS,
                    saga,
                    character,
                )
                isSaving.emit(false)
                loadingMessage.emit(null)
                delay(3.seconds)
                navigateToSaga(saga)
            }
        }

        fun saveSaga() {
            form.value.saga
            val characterInfo = form.value.character

            state.value = CreateSagaState(isLoading = true)
            isSaving.value = true
            viewModelScope.launch(Dispatchers.IO) {
                generateProcessMessage(SagaProcess.CREATING_SAGA)
                val generatedSaga = newSagaUseCase.generateSaga(form.value, chatMessages.value)
                val newSaga =
                    generatedSaga.getSuccess()?.let {
                        generateProcessMessage(SagaProcess.CREATING_CHARACTER)
                        newSagaUseCase.createSaga(it).getSuccess()
                    }
                val newCharacter =
                    newSaga?.let {
                        generateProcessMessage(SagaProcess.CREATING_CHARACTER)
                        characterUseCase
                            .generateCharacter(
                                SagaContent(data = it),
                                characterInfo.toAINormalize(),
                            ).getSuccess()
                    }

                val updatedSaga =
                    newCharacter?.let {
                        newSagaUseCase
                            .updateSaga(newSaga.copy(mainCharacterId = it.id))
                            .getSuccess()
                    }

                if (updatedSaga != null) {
                    finalizeCreation(
                        updatedSaga,
                        newCharacter,
                    )
                } else {
                    sendErrorState(Exception("Error saving saga"))
                }
            }
        }

        private fun navigateToSaga(saga: Saga) {
            effect.value =
                Effect.Navigate(
                    Routes.CHAT,
                    mapOf(
                        "sagaId" to saga.id.toString(),
                        "isDebug" to false.toString(),
                    ),
                )
        }

        private fun sendErrorState(exception: Exception) {
            state.value = state.value.copy(isLoading = false, errorMessage = exception.message)
            isSaving.value = false
            loadingMessage.value = null
            Log.e(
                javaClass.simpleName,
                "sendErrorState: Error saving saga ${exception.message}",
            )
        }

        fun resetSaga() {
            viewModelScope.launch {
                chatMessages.value = emptyList()
                newSagaUseCase.generateIntroduction().onSuccess { gen ->
                    handleGeneratedContent(gen)
                    chatMessages.value = listOf(ChatMessage(text = gen.message, sender = Sender.AI))
                }
                form.value = SagaForm()
            }
        }

        fun resetGeneratedSaga() {
            state.value = CreateSagaState()
        }

        fun retry() {
            sendChatMessage(chatMessages.value.last().text)
        }

        fun updateGenre(genre: Genre) {
            form.update { it.copy(saga = it.saga.copy(genre = genre)) }
        }

        private fun generateProcessMessage(
            process: SagaProcess,
            saga: Saga? = null,
            character: Character? = null,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val sagaData = saga?.toJsonFormat() ?: form.value.saga.toJsonFormat()
                val characterData = character ?: form.value.character
                newSagaUseCase
                    .generateProcessMessage(
                        process = process,
                        sagaDescription = sagaData,
                        characterDescription = characterData.toJsonFormat(),
                    ).onSuccess { message ->
                        chatMessages.update { it + ChatMessage(text = message, sender = Sender.AI) }
                        loadingMessage.value = message
                    }
            }
        }
    }
