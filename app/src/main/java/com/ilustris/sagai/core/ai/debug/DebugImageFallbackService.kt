package com.ilustris.sagai.core.ai.debug

import android.graphics.Bitmap
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.data.SideEffect
import com.ilustris.sagai.core.services.SideEffectService
import kotlinx.coroutines.CancellableContinuation
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

        suspend fun awaitManualImage(prompt: String): Bitmap? {
            if (!BuildConfig.DEBUG) return null

            if (pendingContinuation != null) {
                Timber.w("DebugImageFallback: already waiting for manual image, returning null")
                return null
            }

            sideEffectService.emit(SideEffect.DebugImageManualFallback(prompt))

            return suspendCancellableCoroutine { continuation ->
                pendingContinuation = continuation
                continuation.invokeOnCancellation { clearPending(continuation) }
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
        }
    }
