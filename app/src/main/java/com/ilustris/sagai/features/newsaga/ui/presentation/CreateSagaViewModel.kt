package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.FileHelper
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
        private val fileHelper: FileHelper,
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
            state.value = CreateSagaState.Loading

            viewModelScope.launch(Dispatchers.IO) {
                val iconGeneration = newSagaUseCase.generateSagaIcon(saga.value)
                when (iconGeneration) {
                    is RequestResult.Error<*> -> {
                        finishSaveSaga(sagaData)
                    }

                    is RequestResult.Success<ByteArray> -> {
                        val iconFile =
                            fileHelper.saveToCache(
                                sagaData.title.lowercase(),
                                iconGeneration.success.value,
                            )
                        finishSaveSaga(
                            sagaData.copy(
                                icon = iconFile?.absolutePath,
                                genre = saga.value.genre,
                            ),
                        )
                    }
                }
            }
        }

        private fun finishSaveSaga(sagaData: SagaData) {
            viewModelScope.launch {
                newSagaUseCase.saveSaga(sagaData.copy(genre = saga.value.genre)).run {
                    when (this) {
                        is RequestResult.Error<Exception> -> {
                            sendErrorState(this.error.value)
                        }

                        is RequestResult.Success<Long> -> {
                            state.value =
                                CreateSagaState.Success(sagaData.copy(id = this.success.value.toInt()))
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
            state.value = CreateSagaState.Idle
        }

        fun generateSaga() {
            state.value = CreateSagaState.Loading
            viewModelScope.launch(Dispatchers.IO) {
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
