package com.ilustris.sagai.features.playthrough

import com.ilustris.sagai.core.data.RequestResult

interface PlaythroughUseCase {
    suspend fun invoke(): RequestResult<PlayThroughData>
}

data class PlayThroughData(
    val totalPlayTime: String,
    val totalPlaytimeMs: Long,
    val playtimeReview: String,
)
