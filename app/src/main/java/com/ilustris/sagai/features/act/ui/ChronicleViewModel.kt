package com.ilustris.sagai.features.act.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.usecase.BookUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.share.domain.SharePlayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChronicleState {
    object Idle : ChronicleState()

    object Loading : ChronicleState()

    data class Generating(
        val actTitle: String,
        val message: String?,
    ) : ChronicleState()

    data class Error(
        val message: String,
    ) : ChronicleState()

    data class PDFGenerated(
        val uri: android.net.Uri,
        val title: String,
    ) : ChronicleState()
}

@HiltViewModel
class ChronicleViewModel
    @Inject
    constructor(
        private val bookUseCase: BookUseCase,
        private val sharePlayUseCase: SharePlayUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow<ChronicleState>(ChronicleState.Idle)
        val state = _state.asStateFlow()

        var currentSagaContent: SagaContent? = null

        private val _selectedBook = MutableStateFlow<ActContent?>(null)
        val selectedBook = _selectedBook.asStateFlow()

        fun start(sagaContent: SagaContent) {
            currentSagaContent = sagaContent
            _selectedBook.value = null
        }

        fun selectBook(act: ActContent?) {
            act?.let {
                if (it.data.book != null) {
                    _selectedBook.value = it
                } else {
                    generateNextVolume(act)
                }
            } ?: run {
                _selectedBook.value = null
            }
        }

        fun shareBook(actContent: ActContent) {
            viewModelScope.launch {
                val book = actContent.data.book ?: return@launch
                val saga = currentSagaContent ?: return@launch
                _state.value = ChronicleState.Loading
                val result = sharePlayUseCase.generateBookPDF(book, saga.data.genre)
                if (result is com.ilustris.sagai.core.data.RequestResult.Success) {
                    val uriResult = sharePlayUseCase.loadWithFileProvider(result.value)
                    if (uriResult is com.ilustris.sagai.core.data.RequestResult.Success) {
                        _state.value = ChronicleState.PDFGenerated(uriResult.value, book.actTitle)
                    } else {
                        _state.value = ChronicleState.Error("Failed to get file URI")
                    }
                } else {
                    _state.value = ChronicleState.Error("Failed to generate PDF")
            }
        }
    }

        fun selectBookById(
            saga: SagaContent,
            actId: Int?,
        ) {
            currentSagaContent = saga
            if (actId == null) {
                _selectedBook.value = null
                return
            }
            val act = saga.findAct(actId)
            _selectedBook.value = act
        }

        fun generateNextVolume(actContent: ActContent) {
            viewModelScope.launch {
                val saga = currentSagaContent ?: return@launch
                if (actContent.data.book != null) {
                    _selectedBook.value = actContent
                    return@launch
                }
                _selectedBook.value = null
                bookUseCase.generateBookStream(saga, actContent).collect { state ->
                    when (state) {
                        is StreamingState.Success -> {
                            _state.value = ChronicleState.Idle
                            _selectedBook.value = actContent
                        }

                        is StreamingState.Error -> {
                            _state.value = ChronicleState.Error(state.message)
                        }

                        is StreamingState.Reasoning -> {
                            _state.value = ChronicleState.Generating(actContent.data.title, state.chunk)
                        }
                    }
                }
            }
        }
    }
