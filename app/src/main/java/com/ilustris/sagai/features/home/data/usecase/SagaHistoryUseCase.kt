package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

interface SagaHistoryUseCase {
    fun getSagas(): Flow<List<SagaContent>>

    suspend fun getSagaById(sagaId: Int): Flow<SagaContent?>

    suspend fun generateLore(
        saga: SagaData?,
        character: Character?,
        loreReference: Int,
        lastMessages: List<String>,
    ): RequestResult<Exception, String>
}
