package com.ilustris.sagai.features.act.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.usecase.BookUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.share.domain.SharePlayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BookReaderState {
    object Loading : BookReaderState()

    data class Generating(
        val actTitle: String,
        val message: String?,
    ) : BookReaderState()

    data class Ready(
        val saga: SagaContent,
        val currentAct: ActContent,
        val pages: List<PageItem>,
        val isLastAct: Boolean,
    ) : BookReaderState()

    data class Error(
        val message: String,
    ) : BookReaderState()

    data class PDFGenerated(
        val uri: Uri,
        val title: String,
    ) : BookReaderState()
}

@HiltViewModel
class BookReaderViewModel
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val bookUseCase: BookUseCase,
        private val sharePlayUseCase: SharePlayUseCase,
        private val fileHelper: FileHelper,
        private val pageMapper: BookPageMapper,
        private val stringResourceHelper: StringResourceHelper,
    ) : ViewModel() {
        private val _state = MutableStateFlow<BookReaderState>(BookReaderState.Loading)
        val state = _state.asStateFlow()

        private val _pdfEvent = MutableSharedFlow<BookReaderState.PDFGenerated>(extraBufferCapacity = 1)
        val pdfEvent = _pdfEvent.asSharedFlow()

        private var sagaContent: SagaContent? = null
        private var currentActIndex: Int = 0
        private var initialActId: Int = -1

        // ---------------------------------------------------------------------------
        // Public API
        // ---------------------------------------------------------------------------

        fun load(
            sagaId: Int,
            actId: Int,
        ) {
            initialActId = actId
            viewModelScope.launch {
                sagaRepository.getSagaById(sagaId).collectLatest { saga ->
                    if (saga == null) {
                        _state.value = BookReaderState.Error(stringResourceHelper.getString(R.string.saga_not_found))
                        return@collectLatest
                    }
                    sagaContent = saga

                    // Always resolve index based on initialActId or current target
                    currentActIndex =
                        saga.acts
                            .indexOfFirst { it.data.id == initialActId }
                            .coerceAtLeast(0)

                    renderCurrentAct(saga)
                }
            }
        }

        fun goToNextVolume() {
            val saga = sagaContent ?: return
            val nextIndex = currentActIndex + 1
            if (nextIndex > saga.acts.lastIndex) return

            currentActIndex = nextIndex
            val nextAct = saga.acts[nextIndex]

            if (nextAct.book != null) {
                renderCurrentAct(saga)
            } else {
                generateAndAdvance(saga, nextAct)
            }
        }

        fun regenerateBook() {
            val saga = sagaContent ?: return
            val act = saga.acts.getOrNull(currentActIndex) ?: return
            viewModelScope.launch {
                bookUseCase.resetBook(act)
                _state.value = BookReaderState.Generating(act.data.title, null)
                bookUseCase.generateBookStream(saga, act).collect { streamState ->
                    when (streamState) {
                        is StreamingState.Success -> {
                            renderCurrentAct(saga)
                        }

                        is StreamingState.Error -> {
                            _state.value = BookReaderState.Error(streamState.message)
                        }

                        is StreamingState.Reasoning -> {
                            _state.value = BookReaderState.Generating(act.data.title, streamState.chunk)
                        }
                    }
                }
            }
        }

        fun shareCurrentBook() {
            val saga = sagaContent ?: return
            val act = saga.acts.getOrNull(currentActIndex) ?: return
            val book = act.book ?: return
            viewModelScope.launch {
                _state.value =
                    BookReaderState.Generating(
                        book.actTitle,
                        stringResourceHelper.getString(R.string.book_share_preparing),
                    )

                val pages = pageMapper.buildPages(saga, act, saga.characters)
                val volumeNumber = saga.actNumber(act.data).toRoman()
                val result =
                    sharePlayUseCase.generateBookPDF(
                        book,
                        saga.data.genre,
                        volumeNumber,
                        pages,
                    )

                if (result is RequestResult.Success) {
                    val uriResult = sharePlayUseCase.loadWithFileProvider(result.value)
                    if (uriResult is RequestResult.Success) {
                        val event = BookReaderState.PDFGenerated(uriResult.value, book.actTitle)
                        _pdfEvent.tryEmit(event)
                        renderCurrentAct(saga)
                    } else {
                        _state.value = BookReaderState.Error(stringResourceHelper.getString(R.string.book_share_error_uri))
                    }
                } else {
                    _state.value = BookReaderState.Error(stringResourceHelper.getString(R.string.book_share_error_pdf))
                }
            }
        }

        // ---------------------------------------------------------------------------
        // Private helpers
        // ---------------------------------------------------------------------------

        private fun renderCurrentAct(saga: SagaContent) {
            val act =
                saga.acts.getOrNull(currentActIndex) ?: run {
                    _state.value = BookReaderState.Error(stringResourceHelper.getString(R.string.act_not_found))
                    return
                }
            viewModelScope.launch {
                pageMapper.validateImages(saga, act)
                val pages = pageMapper.buildPages(saga, act, saga.characters)
                _state.value =
                    BookReaderState.Ready(
                        saga = saga,
                        currentAct = act,
                        pages = pages,
                        isLastAct = currentActIndex == saga.acts.lastIndex,
                    )
            }
        }

        private fun generateAndAdvance(
            saga: SagaContent,
            act: ActContent,
        ) {
            viewModelScope.launch {
                _state.value = BookReaderState.Generating(act.data.title, null)
                bookUseCase.generateBookStream(saga, act).collect { streamState ->
                    when (streamState) {
                        is StreamingState.Success -> {
                            renderCurrentAct(saga)
                        }

                        is StreamingState.Error -> {
                            _state.value = BookReaderState.Error(streamState.message)
                        }

                        is StreamingState.Reasoning -> {
                            _state.value = BookReaderState.Generating(act.data.title, streamState.chunk)
                        }
                    }
                }
            }
        }
    }
