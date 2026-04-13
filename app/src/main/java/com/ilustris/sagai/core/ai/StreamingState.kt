package com.ilustris.sagai.core.ai

sealed class StreamingState<out T> {
    data class Reasoning(
        val chunk: String,
    ) : StreamingState<Nothing>()

    data class Success<T>(
        val data: T,
    ) : StreamingState<T>()

    data class Error(
        val message: String,
    ) : StreamingState<Nothing>()
}
