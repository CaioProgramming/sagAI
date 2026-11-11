package com.ilustris.sagai.features.act.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.act.BookGenerationService
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.act.data.usecase.BookUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.share.domain.SharePlayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
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
}

enum class BookShareFormat(
    val mimeType: String,
) {
    PDF("application/pdf"),
    EPUB("application/epub+zip"),
}

enum class BookSaveStatus { Idle, Saved, Failed }

/** The export sheet's steps; no state at all means the sheet is closed. */
sealed interface BookExportState {
    data object Choosing : BookExportState

    data class Generating(
        val format: BookShareFormat,
    ) : BookExportState

    data class Ready(
        val file: File,
        val uri: Uri,
        val title: String,
        val format: BookShareFormat,
        val saveStatus: BookSaveStatus = BookSaveStatus.Idle,
    ) : BookExportState

    data class Failed(
        val format: BookShareFormat,
        val message: String,
    ) : BookExportState
}

@HiltViewModel
class BookReaderViewModel
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val bookUseCase: BookUseCase,
        private val bookGenerationService: BookGenerationService,
        private val sharePlayUseCase: SharePlayUseCase,
        private val fileHelper: FileHelper,
        private val pageMapper: BookPageMapper,
        private val stringResourceHelper: StringResourceHelper,
    ) : ViewModel() {
        private val _state = MutableStateFlow<BookReaderState>(BookReaderState.Loading)
        val state = _state.asStateFlow()

        private val _exportState = MutableStateFlow<BookExportState?>(null)
        val exportState = _exportState.asStateFlow()

        private var exportJob: Job? = null

        private var sagaContent: SagaContent? = null
        private var currentActIndex: Int = 0
        private var initialActId: Int = -1

        init {
            viewModelScope.launch {
                bookGenerationService.uiState.collectLatest { genState ->
                    val act = sagaContent?.acts?.getOrNull(currentActIndex) ?: return@collectLatest
                    when {
                        genState is BookGenerationUiState.Generating && genState.actId == act.data.id -> {
                            _state.value = BookReaderState.Generating(act.data.title, genState.reasoning)
                        }

                        genState is BookGenerationUiState.Error && genState.actId == act.data.id -> {
                            _state.value = BookReaderState.Error(genState.message)
                        }
                    }
                }
            }

            viewModelScope.launch {
                bookGenerationService.completed.collect { key ->
                    val saga = sagaContent ?: return@collect
                    if (saga.acts.getOrNull(currentActIndex)?.data?.id != key.initialActId) return@collect
                    sagaRepository.getSagaById(key.sagaId).first()?.let {
                        sagaContent = it
                        renderCurrentAct(it)
                    }
                }
            }
        }

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

            if (nextAct.hasReadableBook()) {
                renderCurrentAct(saga)
            } else {
                generateAndAdvance(saga, nextAct)
            }
        }

        fun regenerateBook() {
            val saga = sagaContent ?: return
            val act = saga.acts.getOrNull(currentActIndex) ?: return
            viewModelScope.launch {
                _state.value = BookReaderState.Generating(act.data.title, null)
                bookUseCase.resetVolume(act.data.id)
                bookGenerationService.generate(saga, act)
            }
        }

        fun openExport() {
            _exportState.value = BookExportState.Choosing
        }

        /** Closing the sheet also abandons a file that is still being written. */
        fun dismissExport() {
            exportJob?.cancel()
            exportJob = null
            _exportState.value = null
        }

        fun exportBook(format: BookShareFormat) {
            val saga = sagaContent ?: return
            val act = saga.acts.getOrNull(currentActIndex) ?: return
            val book = act.book?.takeIf { it.isSealed() }
            exportJob?.cancel()
            exportJob =
                viewModelScope.launch {
                    if (book == null) {
                        _exportState.value = BookExportState.Failed(format, exportError(format))
                        return@launch
                    }
                    _exportState.value = BookExportState.Generating(format)

                    val pages = pageMapper.buildPages(saga, act, saga.characters)
                    val volumeNumber = saga.actNumber(act.data).toRoman()
                    val fileResult =
                        when (format) {
                            BookShareFormat.PDF ->
                                sharePlayUseCase.generateBookPDF(book, saga.data.genre, volumeNumber, pages)
                            BookShareFormat.EPUB ->
                                sharePlayUseCase.generateBookEpub(book, saga.data.genre, volumeNumber, pages)
                        }
                    // The sheet may have been closed while the file was being written.
                    ensureActive()

                    _exportState.value =
                        when (fileResult) {
                            is RequestResult.Success -> {
                                val uriResult = sharePlayUseCase.loadWithFileProvider(fileResult.value)
                                if (uriResult is RequestResult.Success) {
                                    BookExportState.Ready(fileResult.value, uriResult.value, book.actTitle, format)
                                } else {
                                    BookExportState.Failed(
                                        format,
                                        stringResourceHelper.getString(R.string.book_share_error_uri),
                                    )
                                }
                            }

                            else -> BookExportState.Failed(format, exportError(format))
                        }
                }
        }

        fun saveExport(destination: Uri) {
            val ready = _exportState.value as? BookExportState.Ready ?: return
            viewModelScope.launch {
                val saved = sharePlayUseCase.saveToDocument(ready.file, destination) is RequestResult.Success
                _exportState.update { current ->
                    (current as? BookExportState.Ready)?.copy(
                        saveStatus = if (saved) BookSaveStatus.Saved else BookSaveStatus.Failed,
                    ) ?: current
                }
            }
        }

        private fun exportError(format: BookShareFormat): String =
            stringResourceHelper.getString(
                when (format) {
                    BookShareFormat.PDF -> R.string.book_share_error_pdf
                    BookShareFormat.EPUB -> R.string.book_share_error_epub
                },
            )

        // ---------------------------------------------------------------------------
        // Private helpers
        // ---------------------------------------------------------------------------

        private fun renderCurrentAct(saga: SagaContent) {
            val act =
                saga.acts.getOrNull(currentActIndex) ?: run {
                    _state.value = BookReaderState.Error(stringResourceHelper.getString(R.string.act_not_found))
                    return
                }
            if (!act.hasReadableBook()) {
                // Chapter pages land one by one while a volume is written; keep showing progress
                // (or the last error) instead of an empty book, and start writing only once.
                val current = _state.value
                if (current !is BookReaderState.Generating && current !is BookReaderState.Error) {
                    generateAndAdvance(saga, act)
                }
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
            _state.value = BookReaderState.Generating(act.data.title, null)
            bookGenerationService.generate(saga, act)
        }
    }
