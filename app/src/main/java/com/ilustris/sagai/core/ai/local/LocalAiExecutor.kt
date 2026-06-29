package com.ilustris.sagai.core.ai.local

interface LocalAiExecutor {
    suspend fun availability(): LocalAiAvailability

    /** Fire-and-forget; idempotent while a download is already in progress. */
    fun ensureModelDownloaded()

    suspend fun generate(
        prompt: String,
        systemInstruction: String,
        maxOutputTokens: Int,
    ): Result<String>
}
