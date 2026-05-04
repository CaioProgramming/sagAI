package com.ilustris.sagai.features.act.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.usecase.BookUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
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
        private val visualConfigService: GenreVisualConfigService,
    ) : ViewModel() {
        private val _state = MutableStateFlow<ChronicleState>(ChronicleState.Idle)
        val state = _state.asStateFlow()

        var currentSagaContent: SagaContent? = null

        private val _selectedBook = MutableStateFlow<ActContent?>(null)
        val selectedBook = _selectedBook.asStateFlow()

        private val _visualConfig =
            MutableStateFlow<com.ilustris.sagai.core.ai.model.GenreVisualConfig?>(null)
        val visualConfig = _visualConfig.asStateFlow()

        fun start(sagaContent: SagaContent) {
            currentSagaContent = sagaContent
            _selectedBook.value = null
            viewModelScope.launch {
                _visualConfig.value = visualConfigService.getVisualConfig(sagaContent.data.genre)
            }
        }

        fun selectBook(act: ActContent?) {
            act?.let {
                if (it.book != null) {
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
                val book = actContent.book ?: return@launch
                val saga = currentSagaContent ?: return@launch
                val volumeNumber = saga.actNumber(actContent.data).toRoman()
                _state.value = ChronicleState.Loading
                val result = sharePlayUseCase.generateBookPDF(book, saga.data.genre, volumeNumber)
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
                if (actContent.book != null) {
                    _selectedBook.value = actContent
                    return@launch
                }
                _state.emit(ChronicleState.Loading)
                _selectedBook.value = actContent

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
                            _state.value =
                                ChronicleState.Generating(actContent.data.title, state.chunk)
                        }
                    }
                }
            }
        }

        fun regenerateBook(actContent: ActContent) {
            viewModelScope.launch {
                val saga = currentSagaContent ?: return@launch
                _selectedBook.value = null
                bookUseCase.resetBook(actContent)
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
