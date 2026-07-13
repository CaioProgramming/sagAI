package com.ilustris.sagai.features.imagegeneration

import android.graphics.Bitmap
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.core.globalshell.ImageGenerationWorkEffect
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationRequest
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.imagegeneration.model.IslandExpansion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageGenerationService
    @Inject
    constructor(
        private val imagenClient: ImagenClient,
        private val debugImageFallbackService: DebugImageFallbackService,
        private val globalShellService: GlobalShellService,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
        private companion object {
            // Auto-dismiss reveal after a short time so the queue can't get stuck.
            private const val RevealDismissTimeoutMs = 15_000L
        }

        private val _uiState = MutableStateFlow<ImageGenerationUiState>(ImageGenerationUiState.Idle)
        val uiState: StateFlow<ImageGenerationUiState> = _uiState.asStateFlow()

        private var islandExpansion = IslandExpansion.Compact
        private var revealDismissSignal: CompletableDeferred<Unit>? = null
        private var currentJob: Job? = null
        private var activeRequest: ImageGenerationRequest? = null
        private var activeLabel: String? = null
        private var activeImageType: com.ilustris.sagai.core.ai.model.ImageType? = null

        private var persistentWorkActive: Boolean = false
        private var persistentWorkGenre: com.ilustris.sagai.features.newsaga.data.model.Genre? = null
        private var persistentWorkMessage: String = ""

        private val queueMutex = Mutex()
        private val pendingCount = MutableStateFlow(0)

        private fun setPersistentWorkActive(
            active: Boolean,
            request: ImageGenerationRequest? = null,
        ) {
            if (active) {
                if (persistentWorkActive) return
                persistentWorkActive = true

                if (request != null) {
                    persistentWorkGenre = request.genre
                    persistentWorkMessage = request.label.orEmpty()
                }

                val genre = persistentWorkGenre ?: return
                val message = persistentWorkMessage.ifBlank { "Image generation" }
                globalShellService.post(
                    ImageGenerationWorkEffect(
                        sagaId = 0,
                        sagaTitle = "",
                        genre = genre,
                        message = message,
                        deepLink = "saga://home",
                    ),
                )
            } else {
                if (!persistentWorkActive) return
                persistentWorkActive = false
                persistentWorkGenre = null
                persistentWorkMessage = ""
                globalShellService.dismiss()
            }
        }

        private data class QueuedWork<T>(
            val request: ImageGenerationRequest,
            val onBitmap: suspend (Bitmap) -> T,
            val result: CompletableDeferred<Result<T>>,
        )

        private val workChannel = Channel<QueuedWork<*>>(Channel.UNLIMITED)

        init {
            scope.launch {
                for (work in workChannel) {
                    Timber.d("ImageGenerationService: received work from channel: label=${work.request.label}")
                    @Suppress("UNCHECKED_CAST")
                    processWork(work as QueuedWork<Any?>)
                }
            }
        }

        suspend fun <T> enqueue(
            request: ImageGenerationRequest,
            onBitmap: suspend (Bitmap) -> T,
        ): Result<T> {
            val deferred = CompletableDeferred<Result<T>>()
            pendingCount.update { it + 1 }
            Timber.d("ImageGenerationService: enqueue request label=${request.label} showReveal=${request.showReveal} pending=${pendingCount.value}")
            try {
                if (workChannel.isClosedForSend) {
                    val ex = IllegalStateException("ImageGenerationService: work channel is closed for send")
                    Timber.e(ex)
                    deferred.complete(Result.failure(ex))
                    return deferred.await()
                }
                workChannel.send(QueuedWork(request, onBitmap, deferred))
                Timber.d("ImageGenerationService: enqueued request label=${request.label}")
            } catch (e: Exception) {
                Timber.e(e, "ImageGenerationService: failed to send work to channel")
                deferred.complete(Result.failure(e))
            }

            return deferred.await()
        }

        /**
         * Non-suspending variant of enqueue. Returns a CompletableDeferred that will be
         * completed when work finishes (success/failure). Useful when callers want to
         * fire-and-forget without blocking the calling coroutine while the queue processes.
         */
        fun <T> enqueueAsync(
            request: ImageGenerationRequest,
            onBitmap: suspend (Bitmap) -> T,
        ): CompletableDeferred<Result<T>> {
            val deferred = CompletableDeferred<Result<T>>()
            pendingCount.update { it + 1 }
            Timber.d("ImageGenerationService: enqueueAsync request label=${request.label} pending=${pendingCount.value}")
            val queued = QueuedWork(request, onBitmap, deferred)
            val result = try {
                workChannel.trySend(queued)
            } catch (e: Exception) {
                Timber.e(e, "ImageGenerationService: enqueueAsync trySend failed")
                null
            }
            if (result == null || result.isFailure) {
                val ex = IllegalStateException("ImageGenerationService: failed to enqueue work (trySend)")
                Timber.e(ex)
                deferred.complete(Result.failure(ex))
            } else {
                Timber.d("ImageGenerationService: enqueueAsync queued label=${request.label}")
            }

            return deferred
        }

        suspend fun generateSimpleImage(prompt: String): Result<Bitmap> =
            runCatching {
                imagenClient.generateImage(prompt).getSuccess()
                    ?: error("Failed to generate image")
            }

        fun setIslandExpansion(expansion: IslandExpansion) {
            islandExpansion = expansion
            _uiState.update { state ->
                when (state) {
                    is ImageGenerationUiState.Generating ->
                        state.copy(expansion = expansion)

                    is ImageGenerationUiState.AwaitingManualFallback ->
                        state.copy(expansion = expansion)

                    else -> state
                }
            }
        }

        fun dismissReveal() {
            revealDismissSignal?.complete(Unit)
            revealDismissSignal = null
            _uiState.value = ImageGenerationUiState.Idle
            setPersistentWorkActive(false)
        }

        fun cancelCurrent() {
            currentJob?.cancel()
            debugImageFallbackService.cancel()
        }

        fun cancelManualFallback() {
            debugImageFallbackService.cancel()
        }

        private suspend fun <T> processWork(work: QueuedWork<T>) {
            queueMutex.withLock {
                pendingCount.update { (it - 1).coerceAtLeast(0) }
            }

            val request = work.request
            activeRequest = request
            activeLabel = request.label
            activeImageType = request.imageType

            // Auto-expand the island when the request will show the reveal overlay.
            if (request.showReveal) {
                islandExpansion = IslandExpansion.Expanded
            }

            emitGenerating(request, reasoning = null)

            val job =
                scope.launch {
                    try {
                        var resultBitmap: Bitmap? = null
                        var error: Throwable? = null

                        val fallbackObserver =
                            launch {
                                debugImageFallbackService.isAwaitingUser.collectLatest { awaiting ->
                                    if (awaiting) {
                                        val prompt = debugImageFallbackService.pendingPrompt.value.orEmpty()
                                    // Auto-expand while waiting for manual fallback.
                                    islandExpansion = IslandExpansion.Expanded
                                        _uiState.value =
                                            ImageGenerationUiState.AwaitingManualFallback(
                                                prompt = prompt,
                                                expansion = islandExpansion,
                                            )
                                        setPersistentWorkActive(true, request)
                                    } else if (_uiState.value is ImageGenerationUiState.AwaitingManualFallback) {
                                        // Manual fallback resolved; collapse back automatically.
                                        islandExpansion = IslandExpansion.Compact
                                        emitGenerating(request, reasoning = null)
                                    }
                                }
                            }

                        imagenClient
                            .generateIntegratedImageStream(
                                genre = request.genre,
                                imageReference = request.imageReference,
                                context = request.context,
                                imageType = request.imageType,
                                variationId = request.variationId,
                            ).collect { state ->
                                when (state) {
                                    is StreamingState.Reasoning -> {
                                        emitGenerating(request, reasoning = state.chunk)
                                    }

                                    is StreamingState.Success -> {
                                        resultBitmap = state.data.data
                                    }

                                    is StreamingState.Error -> {
                                        error = state.throwable ?: Exception(state.message)
                                    }
                                }
                            }

                        fallbackObserver.cancelAndJoin()

                        val bitmap =
                            resultBitmap
                                ?: throw (error ?: IllegalStateException("Image generation produced no bitmap"))

                        val persisted = work.onBitmap(bitmap)

                        if (request.showReveal) {
                            // Keep panel expanded while the reveal is visible (GlobalShellHost
                            // renders it inline, Full-expanded, in the top shell).
                            islandExpansion = IslandExpansion.Expanded
                            _uiState.value =
                                ImageGenerationUiState.Reveal(
                                    bitmap = bitmap,
                                    imageType = request.imageType,
                                    label = request.label,
                                )
                            val dismissSignal = CompletableDeferred<Unit>()
                            revealDismissSignal = dismissSignal
                            val dismissed =
                                withTimeoutOrNull(RevealDismissTimeoutMs) {
                                    dismissSignal.await()
                                }
                            // Safety: if the user never dismisses the overlay, the queue must not block forever.
                            if (dismissed == null) {
                                Timber.w(
                                    "ImageGenerationService: reveal dismiss timed out (%d ms). Auto-dismissing.",
                                    RevealDismissTimeoutMs,
                                )
                                // Unblock the queue for the current request.
                                dismissSignal.complete(Unit)
                                _uiState.value = ImageGenerationUiState.Idle
                                setPersistentWorkActive(false)
                            }
                            // Reveal finished (dismissed by user or timeout). Collapse for next work.
                            islandExpansion = IslandExpansion.Compact
                            revealDismissSignal = null
                        } else {
                            _uiState.value = ImageGenerationUiState.Idle
                            setPersistentWorkActive(false)
                        }

                        work.result.complete(Result.success(persisted))
                    } catch (e: Exception) {
                        Timber.e(e, "ImageGenerationService: job failed")
                        _uiState.value = ImageGenerationUiState.Idle
                        setPersistentWorkActive(false)
                        work.result.complete(Result.failure(e))
                    } finally {
                        activeRequest = null
                        activeLabel = null
                        activeImageType = null
                        currentJob = null
                    }
                }

            currentJob = job
            job.join()
        }

        private fun emitGenerating(
            request: ImageGenerationRequest,
            reasoning: String?,
        ) {
            _uiState.value =
                ImageGenerationUiState.Generating(
                    label = request.label,
                    reasoning = reasoning,
                    imageType = request.imageType,
                    queuePosition = pendingCount.value,
                    expansion = islandExpansion,
                )
            setPersistentWorkActive(true, request)
        }
    }
