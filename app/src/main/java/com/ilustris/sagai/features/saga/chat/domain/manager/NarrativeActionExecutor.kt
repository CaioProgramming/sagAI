package com.ilustris.sagai.features.saga.chat.domain.manager

interface NarrativeActionExecutor {
    suspend fun execute(
        action: NarrativeAction,
        environment: NarrativeExecutionEnvironment,
    ): NarrativeExecutionResult
}
