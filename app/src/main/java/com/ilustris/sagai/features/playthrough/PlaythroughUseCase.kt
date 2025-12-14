package com.ilustris.sagai.features.playthrough

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow

interface PlaythroughUseCase {
    suspend fun invoke(): RequestResult<PlayThroughData>

    fun availableSagas(): Flow<List<SagaContent>>
}

data class PlayThroughData(
    val title: String,
    val review: String,
)
