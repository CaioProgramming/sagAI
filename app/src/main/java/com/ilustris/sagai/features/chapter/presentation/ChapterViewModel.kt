package com.ilustris.sagai.features.chapter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChapterViewModel @Inject constructor(
    private val sagaHistoryUseCase: SagaHistoryUseCase
) : ViewModel() {

    val saga = MutableStateFlow<SagaContent?>(null)
    fun loadSaga(sagaId: String?) {
        if (sagaId == null) return
        viewModelScope.launch {
            sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect {
                saga.value = it
            }
        }
    }
}