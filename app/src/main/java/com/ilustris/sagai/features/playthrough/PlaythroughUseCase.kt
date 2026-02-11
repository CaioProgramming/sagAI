package com.ilustris.sagai.features.playthrough

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.playthrough.data.model.PlayThroughData
import kotlinx.coroutines.flow.Flow

interface PlaythroughUseCase {
    suspend fun invoke(): RequestResult<PlayThroughData>

    fun availableSagas(): Flow<List<SagaContent>>
}
