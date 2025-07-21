package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
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
        val sagaData = MutableStateFlow<Saga?>(null)
        val state = MutableStateFlow<CreateSagaState>(CreateSagaState())
        val effect = MutableStateFlow<Effect?>(null)

        fun updateCharacterDescription(description: Character) {
            saga.value = saga.value.copy(character = description)
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

        fun saveSaga(saga: Saga) {
            state.value = CreateSagaState(isLoading = true)

            viewModelScope.launch(Dispatchers.IO) {
                val saveOperation =
                    newSagaUseCase.saveSaga(
                        saga.copy(genre = this@CreateSagaViewModel.saga.value.genre),
                        this@CreateSagaViewModel.saga.value.character,
                    )

                when (saveOperation) {
                    is RequestResult.Error<Exception> -> sendErrorState(saveOperation.value)

                    is RequestResult.Success<Pair<Saga, Character>> -> {
                        val operationData = saveOperation.success.value
                        this@CreateSagaViewModel.saga.value = this@CreateSagaViewModel.saga.value.copy(character = operationData.second)
                        val sagaUpdateOperation =
                            newSagaUseCase
                                .generateSagaIcon(
                                    operationData.first,
                                    operationData.second,
                                )
                        sagaUpdateOperation
                            .onSuccess { newSaga ->
                                state.value = state.value.copy(isLoading = false, saga = newSaga)
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

        private fun navigateToSaga(saga: Saga) {
            effect.value =
                Effect.Navigate(
                    Routes.CHAT,
                    mapOf(
                        "sagaId" to saga.id.toString(),
                        "isDebug" to saga.isDebug.toString(),
                    ),
                )
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
                                saga = saga.copy(icon = null),
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
