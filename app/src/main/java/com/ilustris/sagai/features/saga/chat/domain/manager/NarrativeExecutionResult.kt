package com.ilustris.sagai.features.saga.chat.domain.manager

sealed class NarrativeExecutionResult {
    data class Success(
        val value: Any?,
        val shouldEmitMilestone: Boolean = true,
    ) : NarrativeExecutionResult()

    data class Failure(
        val message: String,
        val canRetry: Boolean = true,
    ) : NarrativeExecutionResult()
}
