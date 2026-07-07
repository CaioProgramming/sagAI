package com.ilustris.sagai.features.imagegeneration

import android.graphics.Bitmap
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
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
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private val _uiState = MutableStateFlow<ImageGenerationUiState>(ImageGenerationUiState.Idle)
        val uiState: StateFlow<ImageGenerationUiState> = _uiState.asStateFlow()

        private var islandExpansion = IslandExpansion.Compact
        private var revealDismissSignal: CompletableDeferred<Unit>? = null
        private var currentJob: Job? = null
        private var activeRequest: ImageGenerationRequest? = null
        private var activeLabel: String? = null
        private var activeImageType: com.ilustris.sagai.core.ai.model.ImageType? = null

        private val queueMutex = Mutex()
        private val pendingCount = MutableStateFlow(0)

        private data class QueuedWork<T>(
            val request: ImageGenerationRequest,
            val onBitmap: suspend (Bitmap) -> T,
            val result: CompletableDeferred<Result<T>>,
        )

        private val workChannel = Channel<QueuedWork<*>>(Channel.UNLIMITED)

        init {
            scope.launch {
                for (work in workChannel) {
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
            workChannel.send(QueuedWork(request, onBitmap, deferred))
            return deferred.await()
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

            if (!request.silent) {
                emitGenerating(request, reasoning = null)
            }

            val job =
                scope.launch {
                    try {
                        var resultBitmap: Bitmap? = null
                        var error: Throwable? = null

                        val fallbackObserver =
                            launch {
                                if (request.silent) return@launch
                                debugImageFallbackService.isAwaitingUser.collectLatest { awaiting ->
                                    if (awaiting) {
                                        val prompt = debugImageFallbackService.pendingPrompt.value.orEmpty()
                                        _uiState.value =
                                            ImageGenerationUiState.AwaitingManualFallback(
                                                prompt = prompt,
                                                expansion = islandExpansion,
                                            )
                                    } else if (_uiState.value is ImageGenerationUiState.AwaitingManualFallback) {
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
                                        if (!request.silent) {
                                            emitGenerating(request, reasoning = state.chunk)
                                        }
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

                        if (request.showReveal && !request.silent) {
                            _uiState.value =
                                ImageGenerationUiState.Reveal(
                                    bitmap = bitmap,
                                    imageType = request.imageType,
                                    label = request.label,
                                )
                            val dismissSignal = CompletableDeferred<Unit>()
                            revealDismissSignal = dismissSignal
                            dismissSignal.await()
                        } else if (!request.silent) {
                            _uiState.value = ImageGenerationUiState.Idle
                        }

                        work.result.complete(Result.success(persisted))
                    } catch (e: Exception) {
                        Timber.e(e, "ImageGenerationService: job failed")
                        if (!request.silent) {
                            _uiState.value = ImageGenerationUiState.Idle
                        }
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
        }
    }
