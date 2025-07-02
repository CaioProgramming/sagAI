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
        val state = MutableStateFlow<CreateSagaState>(CreateSagaState.Initial)

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
            state.value = CreateSagaState.Loading

            viewModelScope.launch(Dispatchers.IO) {
                val sagaOperation =
                    newSagaUseCase.saveSaga(
                        sagaData.copy(genre = saga.value.genre),
                        saga.value.characterDescription,
                    )

                when (sagaOperation) {
                    is RequestResult.Error<Exception> -> sendErrorState(sagaOperation.value)

                    is RequestResult.Success<Pair<SagaData, Character>> -> {
                        val operationData = sagaOperation.success.value
                        val sagaUpdateOperation =
                            newSagaUseCase
                                .generateSagaIcon(
                                    operationData.first,
                                    operationData.second,
                                )
                        sagaUpdateOperation
                            .onSuccess {
                                state.value = CreateSagaState.Success(it)
                            }.onFailure {
                                state.value = CreateSagaState.Success(sagaOperation.success.value.first)
                            }
                    }
                }
            }
        }

        private fun sendErrorState(exception: Exception) {
            state.value = CreateSagaState.Error(exception)
            Log.e(javaClass.simpleName, "sendErrorState: Error saving saga ${exception.message}")
        }

        fun resetSaga() {
            viewModelScope.launch {
                delay(5.seconds)
                saga.value = SagaForm()
            }
        }

        fun resetGeneratedSaga() {
            state.value = CreateSagaState.Initial
        }

        fun generateSaga() {
            state.value = CreateSagaState.Loading
            viewModelScope.launch(Dispatchers.IO) {
                val result = newSagaUseCase.generateSaga(saga.value)
                when (result) {
                    is RequestResult.Error<Exception> -> {
                        state.value = CreateSagaState.Error(result.error.value)
                    }

                    is RequestResult.Success<*> -> {
                        state.value = CreateSagaState.GeneratedSaga(result.success.value)
                        sagaData.value = result.success.value
                    }
                }
            }
        }
    }
