package com.ilustris.sagai.features.saga.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SagaDetailViewModel
    @Inject
    constructor(
        private val sagaDetailUseCase: SagaDetailUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow<State>(State.Loading)
        val state: StateFlow<State> = _state.asStateFlow()

        fun fetchSagaDetails(sagaId: String) {
            viewModelScope.launch(Dispatchers.IO) {
                _state.value = State.Loading
                sagaDetailUseCase.fetchSaga(sagaId.toInt()).collect { saga ->
                    saga?.let { data ->
                        _state.value = State.Success(data)
                    }
                }
            }
        }
    }
