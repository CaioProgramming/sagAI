package com.ilustris.sagai.core.ai.debug

import android.graphics.Bitmap
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.data.SideEffect
import com.ilustris.sagai.core.services.SideEffectService
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class DebugImageFallbackService
    @Inject
    constructor(
        private val sideEffectService: SideEffectService,
    ) {
        private var pendingContinuation: CancellableContinuation<Bitmap?>? = null

        private val _isAwaitingUser = MutableStateFlow(false)
        val isAwaitingUser: StateFlow<Boolean> = _isAwaitingUser.asStateFlow()

        suspend fun awaitManualImage(prompt: String): Bitmap? {
            if (!BuildConfig.DEBUG) return null

            if (pendingContinuation != null) {
                Timber.w("DebugImageFallback: already waiting for manual image, returning null")
                return null
            }

            _isAwaitingUser.value = true
            sideEffectService.emit(SideEffect.DebugImageManualFallback(prompt))

            return try {
                suspendCancellableCoroutine { continuation ->
                    pendingContinuation = continuation
                    continuation.invokeOnCancellation { clearPending(continuation) }
                }
            } finally {
                _isAwaitingUser.value = false
            }
        }

        fun submitBitmap(bitmap: Bitmap) {
            val continuation = pendingContinuation ?: return
            clearPending(continuation)
            continuation.resume(bitmap)
        }

        fun cancel() {
            val continuation = pendingContinuation ?: return
            clearPending(continuation)
            continuation.resume(null)
        }

        fun bindImageGenerationLoadingPause(
            scope: CoroutineScope,
            onPause: () -> Unit,
        ) {
            if (!BuildConfig.DEBUG) return

            scope.launch {
                isAwaitingUser.collect { awaiting ->
                    if (awaiting) onPause()
                }
        }
    }

        private fun clearPending(continuation: CancellableContinuation<Bitmap?>) {
            if (pendingContinuation === continuation) {
                pendingContinuation = null
            }
        }
    }
