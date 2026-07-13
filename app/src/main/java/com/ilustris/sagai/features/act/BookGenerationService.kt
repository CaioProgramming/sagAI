package com.ilustris.sagai.features.act

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.globalshell.BookGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.BookReadyEffect
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.act.data.usecase.BookUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.navigation.BookReaderKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs book (Act prose) generation in a singleton-scoped coroutine so it survives
 * navigation away from the Chronicle screen, mirroring
 * [com.ilustris.sagai.features.imagegeneration.ImageGenerationService]'s pattern of
 * streaming live reasoning into a [StateFlow] the global shell can render anywhere.
 */
@Singleton
class BookGenerationService
    @Inject
    constructor(
        private val bookUseCase: BookUseCase,
        private val globalShellService: GlobalShellService,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private val _uiState = MutableStateFlow<BookGenerationUiState>(BookGenerationUiState.Idle)
        val uiState: StateFlow<BookGenerationUiState> = _uiState.asStateFlow()

        /** Emitted once per successful generation so an alive Chronicle screen can auto-navigate. */
        private val _completed = MutableSharedFlow<BookReaderKey>(extraBufferCapacity = 1)
        val completed = _completed.asSharedFlow()

        private var currentJob: Job? = null

        fun generate(
            saga: SagaContent,
            actContent: ActContent,
        ) {
            if (currentJob?.isActive == true) return

            currentJob =
                scope.launch {
                    _uiState.value =
                        BookGenerationUiState.Generating(
                            sagaId = saga.data.id,
                            sagaTitle = saga.data.title,
                            actId = actContent.data.id,
                            actTitle = actContent.data.title,
                            genre = saga.data.genre,
                            reasoning = null,
                        )
                    globalShellService.post(
                        BookGenerationWorkEffect(
                            actId = actContent.data.id,
                            sagaId = saga.data.id,
                            sagaTitle = saga.data.title,
                            genre = saga.data.genre,
                            message = actContent.data.title,
                            deepLink = "saga://chronicle/${saga.data.id}",
                        ),
                    )

                    bookUseCase.generateBookStream(saga, actContent).collect { state ->
                        when (state) {
                            is StreamingState.Reasoning -> {
                                _uiState.update { current ->
                                    (current as? BookGenerationUiState.Generating)
                                        ?.copy(reasoning = state.chunk)
                                        ?: current
                                }
                            }

                            is StreamingState.Success -> {
                                _uiState.value = BookGenerationUiState.Idle
                                globalShellService.post(
                                    BookReadyEffect(
                                        actId = actContent.data.id,
                                        sagaId = saga.data.id,
                                        sagaTitle = saga.data.title,
                                        genre = saga.data.genre,
                                        actTitle = actContent.data.title,
                                        deepLink = "saga://book_reader/${saga.data.id}/${actContent.data.id}",
                                    ),
                                )
                                _completed.tryEmit(BookReaderKey(saga.data.id, actContent.data.id))
                            }

                            is StreamingState.Error -> {
                                _uiState.value =
                                    BookGenerationUiState.Error(
                                        sagaId = saga.data.id,
                                        actId = actContent.data.id,
                                        message = state.message,
                                    )
                                globalShellService.dismiss()
                            }
                        }
                    }
                }
        }
    }
