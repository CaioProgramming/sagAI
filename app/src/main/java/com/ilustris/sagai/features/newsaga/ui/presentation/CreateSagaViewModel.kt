package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateSagaViewModel
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
    ) : ViewModel() {
        val saga = MutableStateFlow<SagaForm>(SagaForm())
        val state = MutableStateFlow<CreateSagaState>(CreateSagaState.Idle)

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
            viewModelScope.launch {
                state.value = CreateSagaState.Loading
                // AI Service will generate the saga based on the title, description and genre and return the ChatData to save on useCase

                newSagaUseCase.saveSaga(sagaData).also {
                    when (it) {
                        is RequestResult.Error<*> -> {
                            Log.e(
                                javaClass.simpleName,
                                "saveSaga: Error saving saga ${it.error.value.message}",
                            )
                            state.value = CreateSagaState.Error(it.error.value)
                        }
                        is RequestResult.Success<*> -> {
                            Log.d(
                                javaClass.simpleName,
                                "saveSaga: Saga saved successfully with id ${it.success.value}/n$sagaData",
                            )
                            state.value = CreateSagaState.Success(it.success.value)
                            resetSaga()
                        }
                    }
                }
            }
        }

        fun resetSaga() {
            saga.value = SagaForm()
        }

        fun resetGeneratedSaga() {
            state.value = CreateSagaState.Idle
        }

        fun generateSaga() {
            viewModelScope.launch {
                val result = newSagaUseCase.generateSaga(saga.value)
                when (result) {
                    is RequestResult.Error<*> -> {
                        Log.e(
                            javaClass.simpleName,
                            "generateSaga: Error generating saga ${result.error.value.message}",
                        )
                    }
                    is RequestResult.Success<*> -> {
                        state.value = CreateSagaState.GeneratedSaga(result.success.value)
                    }
                }
            }
        }
    }
