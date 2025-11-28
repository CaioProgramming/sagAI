package com.ilustris.sagai.features.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
    ) : ViewModel() {
        val saga = MutableStateFlow<SagaContent?>(null)

        fun getSaga(sagaId: String?) {
            val sagaKey = sagaId ?: return
            viewModelScope.launch(Dispatchers.IO) {
                sagaHistoryUseCase.getSagaById(sagaKey.toInt()).collect {
                    this@TimelineViewModel.saga.value = it
                }
            }
        }
    }
