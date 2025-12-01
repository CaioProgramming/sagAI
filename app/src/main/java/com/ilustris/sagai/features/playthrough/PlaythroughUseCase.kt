package com.ilustris.sagai.features.playthrough

import com.ilustris.sagai.core.ai.model.PlaythroughGen
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.newsaga.data.model.Genre

interface PlaythroughUseCase {
    suspend fun invoke(): RequestResult<PlayThroughData>
}

data class PlayThroughData(
    val totalPlayTime: String,
    val totalPlaytimeMs: Long,
    val playtimeReview: PlaythroughGen,
    val genres: List<Genre> = emptyList()
)
