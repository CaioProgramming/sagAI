package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.MessageType
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
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

@HiltViewModel
class CreateSagaViewModel
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
    ) : ViewModel() {
        val form = MutableStateFlow(SagaForm())
        val sagaData = MutableStateFlow<Saga?>(null)
        val state = MutableStateFlow(CreateSagaState())
        val effect = MutableStateFlow<Effect?>(null)
        val chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
        val formState = MutableStateFlow(FormState())
        val isGenerating = MutableStateFlow(true)
        val isError = MutableStateFlow(false)

        private fun updateGenerating(isGenerating: Boolean) {
            this.isGenerating.value = isGenerating
        }

        fun startChat() {
            updateGenerating(true)
            viewModelScope.launch(Dispatchers.IO) {
                newSagaUseCase
                    .generateIntroduction()
                    .onSuccess {
                        chatMessages.update { it + it }
                        handleGeneratedContent(it)
                    }.onFailure {
                        updateGenerating(false)
                    }
            }
        }

        fun sendChatMessage(userInput: String) {
            updateGenerating(true)
            viewModelScope.launch(Dispatchers.IO) {
                val latestMessage = chatMessages.value.lastOrNull()
                val userMessage = ChatMessage(text = userInput, isUser = true)
                chatMessages.update { it + userMessage }

                newSagaUseCase
                    .replyAiForm(
                        currentMessages = chatMessages.value,
                        latestMessage = latestMessage?.text ?: emptyString(),
                        currentFormData = form.value,
                    ).onSuccess { response ->

                        chatMessages.update { it + ChatMessage(text = response.message, isUser = false) }
                        handleGeneratedContent(response)
                        isError.value = false
                    }.onFailure {
                        Log.e(
                            javaClass.simpleName,
                            "sendChatMessage: Error getting generated content",
                        )
                        isError.value = true
                    }
                updateGenerating(false)
            }
        }

        private fun handleGeneratedContent(response: SagaCreationGen) {
            updateGenerating(false)

            formState.value =
                formState.value.copy(
                    hint = response.inputHint,
                    suggestions = response.suggestions,
                    message = response.message,
                )

            val callback = response.callback ?: return
            Log.d(
                javaClass.simpleName,
                "handleGeneratedContent: Checking new generated content $callback",
            )

            when (callback.action) {
                CallBackAction.UPDATE_DATA -> {
                    Log.d(
                        javaClass.simpleName,
                        "handleGeneratedContent: Checking new content for update data with ${callback.data}",
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
                }

                CallBackAction.SAVE_SAGA -> {
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
                    formState.value = (
                        formState.value.copy(
                            readyToSave = true,
                        )
                    )
                }
            }
        }

        fun saveSaga() {
            val saga = form.value.saga
            val character = form.value.character

            state.value = CreateSagaState(isLoading = true)

            viewModelScope.launch(Dispatchers.IO) {
                val saveOperation =
                    newSagaUseCase.saveSaga(
                        Saga(
                            title = saga.title,
                            genre = saga.genre ?: Genre.entries.random(),
                            description = saga.description,
                        ),
                        character,
                    )

                Log.i(
                    javaClass.simpleName,
                    "handleGeneratedContent: Saving saga ${form.value}",
                )
                when (saveOperation) {
                    is RequestResult.Error -> sendErrorState(saveOperation.value)

                    is RequestResult.Success<Pair<Saga, Character>> -> {
                        val operationData = saveOperation.success.value
                        newSagaUseCase
                            .generateCharacterSavedMark(
                                operationData.second,
                                operationData.first,
                            ).onSuccessAsync { message ->
                                chatMessages.update {
                                    it +
                                        ChatMessage(
                                            text = message,
                                            isUser = false,
                                            type = MessageType.CHARACTER,
                                        )
                                }
                            }

                        val sagaUpdateOperation =
                            newSagaUseCase
                                .generateSagaIcon(
                                    operationData.first,
                                    operationData.second,
                                )
                        sagaUpdateOperation
                            .onSuccess { newSaga ->
                                state.value =
                                    state.value.copy(isLoading = false, saga = newSaga)
                                viewModelScope.launch {
                                    delay(10.seconds)
                                    navigateToSaga(newSaga)
                                }
                            }.onFailure {
                                state.value =
                                    state.value.copy(
                                        isLoading = false,
                                        saga = saveOperation.success.value.first,
                                    )
                            }
                    }
                }
            }
        }

        fun generateSaga() {
            updateGenerating(true)
            viewModelScope.launch(Dispatchers.IO) {
                newSagaUseCase
                    .generateSaga(form.value, chatMessages.value)
                    .onSuccess { saga ->
                        form.update {
                            it.copy(
                                saga =
                                    form.value.saga.copy(
                                        title = saga.saga.title,
                                        description = saga.saga.description,
                                    ),
                            )
                        }
                        saveSaga()
                    }.onFailure {
                        sendErrorState(it)
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
            Log.e(
                javaClass.simpleName,
                "sendErrorState: Error saving saga ${exception.message}",
            )
        }

        fun resetSaga() {
            viewModelScope.launch {
                delay(5.seconds)
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
    }
