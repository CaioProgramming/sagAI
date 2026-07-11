package com.ilustris.sagai.core.ai.debug

import android.graphics.Bitmap
import com.ilustris.sagai.BuildConfig
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class DebugImageFallbackService
    @Inject
    constructor() {
        private var pendingContinuation: CancellableContinuation<Bitmap?>? = null

        private val _isAwaitingUser = MutableStateFlow(false)
        val isAwaitingUser: StateFlow<Boolean> = _isAwaitingUser.asStateFlow()

        private val _pendingPrompt = MutableStateFlow<String?>(null)
        val pendingPrompt: StateFlow<String?> = _pendingPrompt.asStateFlow()

        suspend fun awaitManualImage(prompt: String): Bitmap? {
            if (!BuildConfig.DEBUG) return null

            if (pendingContinuation != null) {
                Timber.w("DebugImageFallback: already waiting for manual image, returning null")
                return null
            }

            _pendingPrompt.value = prompt
            _isAwaitingUser.value = true

            return try {
                suspendCancellableCoroutine { continuation ->
                    pendingContinuation = continuation
                    continuation.invokeOnCancellation { clearPending(continuation) }
                }
            } finally {
                _isAwaitingUser.value = false
                _pendingPrompt.value = null
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

        private fun clearPending(continuation: CancellableContinuation<Bitmap?>) {
            if (pendingContinuation === continuation) {
                pendingContinuation = null
            }
            _isAwaitingUser.value = false
            _pendingPrompt.value = null
        }
    }
