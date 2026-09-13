package com.ilustris.sagai.features.act

import com.ilustris.sagai.R
import com.ilustris.sagai.core.globalshell.BookGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.BookReadyEffect
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.act.data.usecase.BookUseCase
import com.ilustris.sagai.features.act.data.usecase.VolumeProgress
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.ui.navigation.BookReaderKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes books (Act volumes) in a singleton-scoped coroutine so it survives navigation.
 *
 * Books are written incrementally: [writeInBackground] is fired after every narrative step that
 * produces book material (act intro, chapter synthesis, act synthesis) and silently writes whatever
 * the saga's volumes are missing. [generate] runs the same path for one act with live progress, for
 * when the player opens a volume that is not ready yet. All writing is serialized, and every step
 * is idempotent, so failures simply leave a gap the next pass fills in.
 */
@Singleton
class BookGenerationService
    @Inject
    constructor(
        private val bookUseCase: BookUseCase,
        private val sagaRepository: SagaRepository,
        private val remoteConfigService: RemoteConfigService,
        private val globalShellService: GlobalShellService,
        private val stringResourceHelper: StringResourceHelper,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val writeMutex = Mutex()

        private val pendingSilentSagas = Collections.synchronizedSet(mutableSetOf<Int>())
        private val healedSagas = Collections.synchronizedSet(mutableSetOf<Int>())

        private val _uiState = MutableStateFlow<BookGenerationUiState>(BookGenerationUiState.Idle)
        val uiState: StateFlow<BookGenerationUiState> = _uiState.asStateFlow()

        /** Emitted once per volume opened through [generate] so an alive Chronicle screen can auto-navigate. */
        private val _completed = MutableSharedFlow<BookReaderKey>(extraBufferCapacity = 1)
        val completed = _completed.asSharedFlow()

        fun generate(
            saga: SagaContent,
            actContent: ActContent,
        ) {
            if (_uiState.value is BookGenerationUiState.Generating) return
            val generating =
                BookGenerationUiState.Generating(
                    sagaId = saga.data.id,
                    sagaTitle = saga.data.title,
                    actId = actContent.data.id,
                    actTitle = actContent.data.title,
                    genre = saga.data.genre,
                    reasoning = null,
                )
            _uiState.value = generating

            scope.launch {
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

                writeMutex.withLock {
                    bookUseCase.writeVolume(saga.data.id, actContent.data.id).collect { progress ->
                        when (progress) {
                            is VolumeProgress.Done -> {
                                if (progress.sealed) {
                                    _uiState.value = BookGenerationUiState.Idle
                                    postBookReady(saga, actContent)
                                    _completed.tryEmit(BookReaderKey(saga.data.id, actContent.data.id))
                                } else {
                                    fail(saga, actContent, stringResourceHelper.getString(R.string.book_volume_not_finished))
                                }
                            }

                            is VolumeProgress.Failed -> {
                                fail(saga, actContent, progress.message)
                            }

                            else -> {
                                _uiState.value = generating.copy(reasoning = progress.describe())
                            }
                        }
                    }
                }
            }
        }

        /**
         * Silently writes what the saga's volumes are missing. Only touches the current act and acts
         * the incremental writer already started, so older sagas' untouched volumes stay on demand.
         */
        fun writeInBackground(sagaId: Int) {
            if (!pendingSilentSagas.add(sagaId)) return
            scope.launch {
                try {
                    writeMutex.withLock {
                        pendingSilentSagas.remove(sagaId)
                        val saga = sagaRepository.getSagaById(sagaId).first() ?: return@withLock
                        val rules = remoteConfigService.getNarrativeRules()
                        val acts =
                            saga.acts.filter { act ->
                                val inScope = act.data.id == saga.data.currentActId || act.hasBookProgress()
                                inScope && !act.isVolumeReady(rules)
                            }
                        for (act in acts) {
                            var failed = false
                            bookUseCase.writeVolume(sagaId, act.data.id).collect { progress ->
                                when (progress) {
                                    is VolumeProgress.Failed -> {
                                        failed = true
                                        Timber.w("Silent book writing stopped at act ${act.data.id}: ${progress.message}")
                                    }

                                    is VolumeProgress.Done -> {
                                        if (progress.sealed) postBookReady(saga, act)
                                    }

                                    else -> {
                                        Unit
                                    }
                                }
                            }
                            if (failed) break
                        }
                    }
                } finally {
                    pendingSilentSagas.remove(sagaId)
                }
            }
        }

        /** Once per app session per saga: fills gaps left by failed or interrupted silent writes. */
        fun healOnce(sagaId: Int) {
            if (!healedSagas.add(sagaId)) return
            writeInBackground(sagaId)
        }

        private fun fail(
            saga: SagaContent,
            actContent: ActContent,
            message: String,
        ) {
            _uiState.value =
                BookGenerationUiState.Error(
                    sagaId = saga.data.id,
                    actId = actContent.data.id,
                    message = message,
                )
            globalShellService.dismiss()
        }

        private fun postBookReady(
            saga: SagaContent,
            actContent: ActContent,
        ) {
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
        }

        private fun VolumeProgress.describe(): String? =
            when (this) {
                VolumeProgress.Prologue -> stringResourceHelper.getString(R.string.book_writing_prologue)
                is VolumeProgress.Chapter -> stringResourceHelper.getString(R.string.book_writing_chapter, position, total, title)
                VolumeProgress.Closure -> stringResourceHelper.getString(R.string.book_writing_closure)
                else -> null
            }
    }
