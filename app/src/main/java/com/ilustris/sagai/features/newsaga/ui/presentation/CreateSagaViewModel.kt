package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class CreateSagaViewModel
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
    ) : ViewModel() {
        val saga = MutableStateFlow(SagaForm())
        val sagaData = MutableStateFlow<SagaData?>(null)
        val state = MutableStateFlow<CreateSagaState>(CreateSagaState())
        val effect = MutableStateFlow<Effect?>(null)

        fun updateCharacterDescription(description: String) {
            saga.value = saga.value.copy(characterDescription = description)
            generateSaga()
        }

        fun updateTitle(title: String) {
            saga.value = saga.value.copy(title = title)
        }

        fun updateDescription(description: String) {
            saga.value = saga.value.copy(description = description)
        }

        fun updateGenre(genre: Genre) {
            saga.value = saga.value.copy(genre = genre)
        }

        fun saveSaga(sagaData: SagaData) {
            state.value = CreateSagaState(isLoading = true)

            viewModelScope.launch(Dispatchers.IO) {
                val saveOperation =
                    newSagaUseCase.saveSaga(
                        sagaData.copy(genre = saga.value.genre),
                        saga.value.characterDescription,
                    )

                when (saveOperation) {
                    is RequestResult.Error<Exception> -> sendErrorState(saveOperation.value)

                    is RequestResult.Success<Pair<SagaData, Character>> -> {
                        val operationData = saveOperation.success.value
                        val sagaUpdateOperation =
                            newSagaUseCase
                                .generateSagaIcon(
                                    operationData.first,
                                    operationData.second,
                                )
                        sagaUpdateOperation
                            .onSuccess {
                                state.value = state.value.copy(isLoading = false, saga = it)
                                viewModelScope.launch {
                                    delay(10.seconds)
                                    navigateToSaga(it)
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

        private fun navigateToSaga(sagaData: SagaData) {
            effect.value = Effect.Navigate(Routes.CHAT, mapOf("sagaId" to sagaData.id.toString()))
        }

        private fun sendErrorState(exception: Exception) {
            state.value = state.value.copy(isLoading = false, errorMessage = exception.message)
            Log.e(javaClass.simpleName, "sendErrorState: Error saving saga ${exception.message}")
        }

        fun resetSaga() {
            viewModelScope.launch {
                delay(5.seconds)
                saga.value = SagaForm()
            }
        }

        fun resetGeneratedSaga() {
            state.value = CreateSagaState()
        }

        fun generateSaga() {
            state.value = CreateSagaState(isLoading = true)
            viewModelScope.launch(Dispatchers.IO) {
                newSagaUseCase
                    .generateSaga(saga.value)
                    .onSuccess { saga ->
                        state.value =
                            state.value.copy(
                                isLoading = false,
                                saga = saga,
                                continueAction =
                                    "Salvar" to {
                                        saveSaga(saga)
                                    },
                            )
                    }.onFailure {
                        sendErrorState(it)
                    }
            }
        }
    }
