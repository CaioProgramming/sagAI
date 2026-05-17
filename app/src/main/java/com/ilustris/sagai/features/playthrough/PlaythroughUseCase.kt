package com.ilustris.sagai.features.playthrough

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.playthrough.data.model.PlayThroughData
import com.ilustris.sagai.features.playthrough.data.model.SagaPlaythrough
import kotlinx.coroutines.flow.Flow

interface PlaythroughUseCase {
    suspend fun invoke(): RequestResult<PlayThroughData>

    fun availableSagas(): Flow<List<SagaPlaythrough>>
}
