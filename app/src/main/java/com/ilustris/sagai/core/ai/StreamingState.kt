package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.data.isFlowCancellation

sealed class StreamingState<out T> {
    data class Reasoning(
        val chunk: String,
    ) : StreamingState<Nothing>()

    data class Success<T>(
        val data: T,
    ) : StreamingState<T>()

    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : StreamingState<Nothing>() {
        fun isFlowCancellation(): Boolean =
            throwable?.isFlowCancellation() == true || message.isFlowCancellation()
    }
}

private fun String.isFlowCancellation(): Boolean =
    contains("child flow", ignoreCase = true) ||
        contains("scoped flow was cancelled", ignoreCase = true)
