package com.ilustris.sagai.features.act.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.usecase.BookUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.ui.navigation.BookReaderKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
}

/**
 * Manages the Chronicle shelf (act selection + on-demand book generation).
 * When a book is ready to read it emits a [BookReaderKey] via [navigationEvent];
 * the hosting screen handles the actual navigation push.
 */
@HiltViewModel
class ChronicleViewModel
    @Inject
    constructor(
        private val bookUseCase: BookUseCase,
        private val visualConfigService: GenreVisualConfigService,
        private val sagaRepository: SagaRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow<ChronicleState>(ChronicleState.Idle)
        val state = _state.asStateFlow()

        private val _saga = MutableStateFlow<SagaContent?>(null)
        val saga = _saga.asStateFlow()

        /** Emitted once when a book is ready and the reader should be opened. */
        private val _navigationEvent = MutableSharedFlow<BookReaderKey>(extraBufferCapacity = 1)
        val navigationEvent = _navigationEvent.asSharedFlow()

        private val _visualConfig =
            MutableStateFlow<com.ilustris.sagai.core.ai.model.GenreVisualConfig?>(null)
        val visualConfig = _visualConfig.asStateFlow()

        var currentSagaContent: SagaContent? = null

        fun loadSaga(sagaId: Int) {
            viewModelScope.launch {
                sagaRepository.getSagaById(sagaId).collectLatest {
                    _saga.value = it
                    it?.let { sagaContent ->
                        start(sagaContent)
                    }
                }
            }
        }

        fun start(sagaContent: SagaContent) {
            currentSagaContent = sagaContent
            viewModelScope.launch {
                _visualConfig.value = visualConfigService.getVisualConfig(sagaContent.data.genre)
            }
        }

        /**
         * Called when the user taps a book on the shelf.
         * If the book already exists the reader is opened immediately.
         * If it's missing, generation is triggered first.
     */
        fun selectBook(act: ActContent?) {
            viewModelScope.launch {
                act ?: return@launch
                val saga = currentSagaContent ?: return@launch

                if (act.book != null) {
                    _navigationEvent.tryEmit(BookReaderKey(saga.data.id, act.data.id))
                } else {
                    generateNextVolume(act)
                }
            }
        }

        fun selectBookById(
            saga: SagaContent,
            actId: Int?,
        ) {
            currentSagaContent = saga
            if (actId == null) return
        val act = saga.findAct(actId) ?: return
        selectBook(act)
        }

        fun generateNextVolume(actContent: ActContent) {
            viewModelScope.launch {
                val saga = currentSagaContent ?: return@launch

                if (actContent.book != null) {
                    _navigationEvent.tryEmit(BookReaderKey(saga.data.id, actContent.data.id))
                    return@launch
                }

                _state.emit(ChronicleState.Generating(actContent.data.title, null))
                bookUseCase.generateBookStream(saga, actContent).collect { streamState ->
                    when (streamState) {
                        is StreamingState.Success -> {
                            _state.value = ChronicleState.Idle
                            // Emit nav event after successful generation
                            _navigationEvent.tryEmit(
                                BookReaderKey(
                                    saga.data.id,
                                    actContent.data.id,
                                )
                            )
                        }

                        is StreamingState.Error -> {
                            _state.value = ChronicleState.Error(streamState.message)
                        }

                        is StreamingState.Reasoning -> {
                            _state.value =
                                ChronicleState.Generating(actContent.data.title, streamState.chunk)
                        }
                    }
                }
            }
        }
    }
